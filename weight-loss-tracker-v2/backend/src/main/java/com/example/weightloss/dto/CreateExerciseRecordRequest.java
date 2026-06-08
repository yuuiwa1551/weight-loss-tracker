package com.example.weightloss.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateExerciseRecordRequest(
	@NotNull @PastOrPresent LocalDate recordDate,
	@NotBlank @Size(max = 60) String exerciseType,
	@NotBlank @Size(max = 120) String exerciseName,
	@NotNull @Min(1) @Max(1440) Integer durationMinutes,
	@NotNull @PositiveOrZero Integer caloriesBurned,
	@Size(max = 500) String note
) {
}