package com.example.weightloss.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProfileRequest(
	@NotBlank @Size(max = 60) String nickname,
	@NotNull @Positive BigDecimal heightCm,
	@NotNull @Positive BigDecimal currentWeightKg,
	@NotNull @Positive BigDecimal targetWeightKg,
	@NotNull @Positive Integer dailyCalorieGoal
) {
}