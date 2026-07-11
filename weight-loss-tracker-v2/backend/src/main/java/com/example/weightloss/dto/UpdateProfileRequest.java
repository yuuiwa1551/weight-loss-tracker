package com.example.weightloss.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProfileRequest(
	@Size(max = 60) String nickname,
	@DecimalMin("50.0") @DecimalMax("250.0") BigDecimal heightCm,
	@DecimalMin("20.0") @DecimalMax("500.0") BigDecimal currentWeightKg,
	@DecimalMin("20.0") @DecimalMax("500.0") BigDecimal targetWeightKg,
	@Min(500) @Max(10000) Integer dailyCalorieGoal
) {
}
