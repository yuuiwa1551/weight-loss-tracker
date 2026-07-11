package com.example.weightloss.service;

import com.example.weightloss.dto.CreateExerciseRecordRequest;
import com.example.weightloss.dto.ExerciseRecordResponse;
import com.example.weightloss.entity.AppUser;
import com.example.weightloss.entity.ExerciseRecord;
import com.example.weightloss.entity.RecordSource;
import com.example.weightloss.repository.ExerciseRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExerciseRecordService {

	private final ExerciseRecordRepository exerciseRecordRepository;
	private final UserService userService;

	public ExerciseRecordService(ExerciseRecordRepository exerciseRecordRepository, UserService userService) {
		this.exerciseRecordRepository = exerciseRecordRepository;
		this.userService = userService;
	}

	@Transactional
	public ExerciseRecordResponse create(Long userId, CreateExerciseRecordRequest request) {
		AppUser user = userService.getEntity(userId);
		String clientRequestId = normalize(request.clientRequestId());
		if (clientRequestId != null) {
			var existing = exerciseRecordRepository.findByUserIdAndClientRequestId(userId, clientRequestId);
			if (existing.isPresent()) {
				return ExerciseRecordResponse.from(existing.get());
			}
		}
		ExerciseRecord record = new ExerciseRecord(
			user,
			request.recordDate(),
			request.exerciseType().trim(),
			request.exerciseName().trim(),
			request.durationMinutes(),
			request.caloriesBurned(),
			normalize(request.note()),
			request.source() == null ? RecordSource.WEB : request.source(),
			clientRequestId
		);
		return ExerciseRecordResponse.from(exerciseRecordRepository.save(record));
	}

	@Transactional(readOnly = true)
	public List<ExerciseRecordResponse> listByDate(Long userId, LocalDate date) {
		userService.getEntity(userId);
		return exerciseRecordRepository.findByUserIdAndRecordDateOrderByCreatedAtAscIdAsc(userId, date).stream()
			.map(ExerciseRecordResponse::from)
			.toList();
	}

	@Transactional
	public void delete(Long userId, Long id) {
		ExerciseRecord record = exerciseRecordRepository.findByIdAndUserId(id, userId)
			.orElseThrow(() -> new ResourceNotFoundException("Exercise record not found: " + id));
		exerciseRecordRepository.delete(record);
	}

	private String normalize(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
