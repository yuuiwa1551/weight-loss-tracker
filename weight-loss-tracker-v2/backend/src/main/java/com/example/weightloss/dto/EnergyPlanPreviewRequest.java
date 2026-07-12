package com.example.weightloss.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record EnergyPlanPreviewRequest(
	@PositiveOrZero Integer dailyDeficitCalories,
	@Positive Integer targetPeriodDays
) {
	@AssertTrue(message = "dailyDeficitCalories and targetPeriodDays cannot both be set")
	public boolean isSingleDeficitStrategy() {
		return dailyDeficitCalories == null || targetPeriodDays == null;
	}
}
