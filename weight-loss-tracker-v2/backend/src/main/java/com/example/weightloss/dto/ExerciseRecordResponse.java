package com.example.weightloss.dto;

import com.example.weightloss.entity.ExerciseRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExerciseRecordResponse(
	Long id,
	LocalDate recordDate,
	String exerciseType,
	String exerciseName,
	Integer durationMinutes,
	Integer caloriesBurned,
	String note,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static ExerciseRecordResponse from(ExerciseRecord record) {
		return new ExerciseRecordResponse(
			record.getId(),
			record.getRecordDate(),
			record.getExerciseType(),
			record.getExerciseName(),
			record.getDurationMinutes(),
			record.getCaloriesBurned(),
			record.getNote(),
			record.getCreatedAt(),
			record.getUpdatedAt()
		);
	}
}