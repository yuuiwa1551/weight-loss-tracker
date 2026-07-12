package com.example.weightloss.dto;

import com.example.weightloss.entity.RecordSource;

import java.time.LocalDate;

public record ExerciseRecordPreviewResponse(
	LocalDate recordDate,
	String exerciseType,
	String exerciseName,
	Integer durationMinutes,
	Integer caloriesBurned,
	String note,
	RecordSource source,
	EnergyBudgetResponse projectedEnergyBudget,
	String previewFingerprint
) {
}
