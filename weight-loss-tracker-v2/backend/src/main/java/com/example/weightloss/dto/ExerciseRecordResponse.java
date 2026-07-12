package com.example.weightloss.dto;

import com.example.weightloss.entity.ExerciseRecord;
import com.example.weightloss.entity.RecordSource;

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
	RecordSource source,
	String clientRequestId,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	EnergyBudgetResponse energyBudget
) {
	public static ExerciseRecordResponse from(ExerciseRecord record) {
		return from(record, null);
	}

	public static ExerciseRecordResponse from(ExerciseRecord record, EnergyBudgetResponse energyBudget) {
		return new ExerciseRecordResponse(
			record.getId(),
			record.getRecordDate(),
			record.getExerciseType(),
			record.getExerciseName(),
			record.getDurationMinutes(),
			record.getCaloriesBurned(),
			record.getNote(),
			record.getSource(),
			record.getClientRequestId(),
			record.getCreatedAt(),
			record.getUpdatedAt(),
			energyBudget
		);
	}
}
