package com.example.weightloss.service;

import com.example.weightloss.dto.CreateExerciseRecordRequest;
import com.example.weightloss.dto.ExerciseRecordResponse;
import com.example.weightloss.entity.ExerciseRecord;
import com.example.weightloss.repository.ExerciseRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExerciseRecordService {

	private final ExerciseRecordRepository exerciseRecordRepository;

	public ExerciseRecordService(ExerciseRecordRepository exerciseRecordRepository) {
		this.exerciseRecordRepository = exerciseRecordRepository;
	}

	@Transactional
	public ExerciseRecordResponse create(CreateExerciseRecordRequest request) {
		ExerciseRecord record = new ExerciseRecord(
			request.recordDate(),
			request.exerciseType().trim(),
			request.exerciseName().trim(),
			request.durationMinutes(),
			request.caloriesBurned(),
			normalizeNote(request.note())
		);
		return ExerciseRecordResponse.from(exerciseRecordRepository.save(record));
	}

	@Transactional(readOnly = true)
	public List<ExerciseRecordResponse> listByDate(LocalDate date) {
		return exerciseRecordRepository.findByRecordDateOrderByCreatedAtAscIdAsc(date).stream()
			.map(ExerciseRecordResponse::from)
			.toList();
	}

	@Transactional
	public void delete(Long id) {
		if (!exerciseRecordRepository.existsById(id)) {
			throw new ResourceNotFoundException("Exercise record not found: " + id);
		}
		exerciseRecordRepository.deleteById(id);
	}

	private String normalizeNote(String note) {
		return note == null || note.isBlank() ? null : note.trim();
	}
}