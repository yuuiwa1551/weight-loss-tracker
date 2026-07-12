package com.example.weightloss.dto;

import com.example.weightloss.entity.EnergyCalculationMethod;
import com.example.weightloss.entity.EnergyPlanDeficitMode;
import com.example.weightloss.entity.FormulaSex;
import com.example.weightloss.entity.NonExerciseActivityLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EnergyPlanCalculationResponse(
	EnergyCalculationMethod calculationMethod,
	String calculationVersion,
	EnergyPlanDeficitMode deficitMode,
	Integer ageYears,
	FormulaSex formulaSex,
	BigDecimal heightCm,
	BigDecimal weightKg,
	BigDecimal targetWeightKg,
	NonExerciseActivityLevel nonExerciseActivityLevel,
	Integer targetPeriodDays,
	Integer restingEnergyCalories,
	Integer baselineExpenditureCalories,
	Integer dailyDeficitCalories,
	Integer baseIntakeTargetCalories,
	LocalDateTime profileUpdatedAt
) {
}
