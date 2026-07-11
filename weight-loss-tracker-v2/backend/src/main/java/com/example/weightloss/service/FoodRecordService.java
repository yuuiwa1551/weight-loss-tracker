package com.example.weightloss.service;

import com.example.weightloss.dto.CreateFoodRecordRequest;
import com.example.weightloss.dto.FoodRecordResponse;
import com.example.weightloss.entity.AppUser;
import com.example.weightloss.entity.FoodRecord;
import com.example.weightloss.entity.NutritionSource;
import com.example.weightloss.entity.RecordSource;
import com.example.weightloss.repository.FoodRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class FoodRecordService {

	private final FoodRecordRepository foodRecordRepository;
	private final UserService userService;

	public FoodRecordService(FoodRecordRepository foodRecordRepository, UserService userService) {
		this.foodRecordRepository = foodRecordRepository;
		this.userService = userService;
	}

	@Transactional
	public FoodRecordResponse create(Long userId, CreateFoodRecordRequest request) {
		AppUser user = userService.getEntity(userId);
		String clientRequestId = normalize(request.clientRequestId());
		if (clientRequestId != null) {
			var existing = foodRecordRepository.findByUserIdAndClientRequestId(userId, clientRequestId);
			if (existing.isPresent()) {
				return FoodRecordResponse.from(existing.get());
			}
		}
		FoodRecord record = new FoodRecord(
			user,
			request.recordDate(),
			request.mealType(),
			request.foodName().trim(),
			request.calories(),
			zeroIfNull(request.protein()),
			zeroIfNull(request.fat()),
			zeroIfNull(request.carbohydrate()),
			normalize(request.note()),
			request.source() == null ? RecordSource.WEB : request.source(),
			clientRequestId,
			request.nutritionSource() == null ? NutritionSource.USER_PROVIDED : request.nutritionSource(),
			normalize(request.estimationNote())
		);
		return FoodRecordResponse.from(foodRecordRepository.save(record));
	}

	@Transactional(readOnly = true)
	public List<FoodRecordResponse> listByDate(Long userId, LocalDate date) {
		userService.getEntity(userId);
		return foodRecordRepository.findByUserIdAndRecordDateOrderByCreatedAtAscIdAsc(userId, date).stream()
			.map(FoodRecordResponse::from)
			.toList();
	}

	@Transactional
	public void delete(Long userId, Long id) {
		FoodRecord record = foodRecordRepository.findByIdAndUserId(id, userId)
			.orElseThrow(() -> new ResourceNotFoundException("Food record not found: " + id));
		foodRecordRepository.delete(record);
	}

	private BigDecimal zeroIfNull(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private String normalize(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
