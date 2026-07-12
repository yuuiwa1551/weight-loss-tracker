package com.example.weightloss.dto;

import com.example.weightloss.entity.CalorieGoalMode;
import com.example.weightloss.entity.EnergyCalculationMethod;

import java.time.LocalDate;

public record EnergyBudgetResponse(
	LocalDate date,
	Integer restingEnergyCalories,
	Integer baselineExpenditureCalories,
	Integer exerciseCaloriesBurned,
	Integer estimatedTotalExpenditureCalories,
	Integer baseIntakeTargetCalories,
	Integer todayIntakeBudgetCalories,
	Integer caloriesConsumed,
	Integer remainingIntakeCalories,
	Integer projectedDeficitCalories,
	CalorieGoalMode goalMode,
	EnergyCalculationMethod calculationMethod,
	String calculationVersion
) {
}
