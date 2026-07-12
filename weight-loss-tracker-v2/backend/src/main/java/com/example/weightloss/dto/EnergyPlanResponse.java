package com.example.weightloss.dto;

import com.example.weightloss.entity.EnergyPlan;
import com.example.weightloss.entity.EnergyPlanStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EnergyPlanResponse(
	Long id,
	EnergyPlanCalculationResponse calculation,
	EnergyPlanStatus status,
	LocalDate effectiveFrom,
	String clientRequestId,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static EnergyPlanResponse from(EnergyPlan plan) {
		return new EnergyPlanResponse(
			plan.getId(),
			new EnergyPlanCalculationResponse(
				plan.getCalculationMethod(),
				plan.getCalculationVersion(),
				plan.getDeficitMode(),
				plan.getAgeYears(),
				plan.getFormulaSex(),
				plan.getHeightCm(),
				plan.getWeightKg(),
				plan.getTargetWeightKg(),
				plan.getNonExerciseActivityLevel(),
				plan.getTargetPeriodDays(),
				plan.getRestingEnergyCalories(),
				plan.getBaselineExpenditureCalories(),
				plan.getDailyDeficitCalories(),
				plan.getBaseIntakeTargetCalories(),
				plan.getProfileUpdatedAt()
			),
			plan.getStatus(),
			plan.getEffectiveFrom(),
			plan.getClientRequestId(),
			plan.getCreatedAt(),
			plan.getUpdatedAt()
		);
	}
}
