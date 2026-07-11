package com.example.weightloss.service;

import com.example.weightloss.dto.CreateWeightRecordRequest;
import com.example.weightloss.dto.WeightRecordResponse;
import com.example.weightloss.entity.AppUser;
import com.example.weightloss.entity.RecordSource;
import com.example.weightloss.entity.WeightRecord;
import com.example.weightloss.repository.WeightRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class WeightRecordService {

	private final WeightRecordRepository weightRecordRepository;
	private final UserService userService;
	private final ProfileService profileService;

	public WeightRecordService(
		WeightRecordRepository weightRecordRepository,
		UserService userService,
		ProfileService profileService
	) {
		this.weightRecordRepository = weightRecordRepository;
		this.userService = userService;
		this.profileService = profileService;
	}

	@Transactional
	public WeightRecordResponse create(Long userId, CreateWeightRecordRequest request) {
		AppUser user = userService.getEntity(userId);
		String clientRequestId = normalize(request.clientRequestId());
		if (clientRequestId != null) {
			var existing = weightRecordRepository.findByUserIdAndClientRequestId(userId, clientRequestId);
			if (existing.isPresent()) {
				return WeightRecordResponse.from(existing.get());
			}
		}
		WeightRecord record = new WeightRecord(
			user,
			request.recordDate(),
			request.weightKg(),
			request.bodyFatPercentage(),
			normalize(request.note()),
			request.source() == null ? RecordSource.WEB : request.source(),
			clientRequestId
		);
		WeightRecord saved = weightRecordRepository.save(record);
		refreshCurrentWeight(userId);
		return WeightRecordResponse.from(saved);
	}

	@Transactional(readOnly = true)
	public List<WeightRecordResponse> listRecent(Long userId, int days) {
		userService.getEntity(userId);
		LocalDate endDate = LocalDate.now();
		LocalDate startDate = endDate.minusDays(days - 1L);
		return weightRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAscCreatedAtAscIdAsc(userId, startDate, endDate).stream()
			.map(WeightRecordResponse::from)
			.toList();
	}

	@Transactional
	public void delete(Long userId, Long id) {
		WeightRecord record = weightRecordRepository.findByIdAndUserId(id, userId)
			.orElseThrow(() -> new ResourceNotFoundException("Weight record not found: " + id));
		weightRecordRepository.delete(record);
		weightRecordRepository.flush();
		refreshCurrentWeight(userId);
	}

	private void refreshCurrentWeight(Long userId) {
		profileService.updateCurrentWeight(
			userId,
			weightRecordRepository.findFirstByUserIdOrderByRecordDateDescCreatedAtDescIdDesc(userId)
				.map(WeightRecord::getWeightKg)
				.orElse(null)
		);
	}

	private String normalize(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
