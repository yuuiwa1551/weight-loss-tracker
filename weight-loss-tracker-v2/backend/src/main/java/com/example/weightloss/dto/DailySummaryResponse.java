package com.example.weightloss.dto;

import com.example.weightloss.entity.GoalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailySummaryResponse(
	LocalDate date,
	Integer totalCaloriesConsumed,
	Integer totalCaloriesBurned,
	Integer netCalories,
	Integer dailyCalorieGoal,
	Integer calorieDifference,
	GoalStatus goalStatus,
	BigDecimal totalProtein,
	BigDecimal totalFat,
	BigDecimal totalCarbohydrate,
	List<FoodRecordResponse> foodRecords,
	List<ExerciseRecordResponse> exerciseRecords
) {
}