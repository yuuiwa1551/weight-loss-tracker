package com.example.weightloss.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PeriodReportResponse(
	LocalDate startDate,
	LocalDate endDate,
	Integer days,
	Integer totalCaloriesConsumed,
	Integer totalCaloriesBurned,
	Integer netCalories,
	BigDecimal averageCaloriesConsumed,
	BigDecimal averageCaloriesBurned,
	BigDecimal averageNetCalories,
	BigDecimal averageProtein,
	BigDecimal averageFat,
	BigDecimal averageCarbohydrate,
	Integer dailyCalorieGoal,
	Long daysUnderGoal,
	Long daysMeetGoal,
	Long daysOverGoal,
	BigDecimal startWeightKg,
	BigDecimal endWeightKg,
	BigDecimal weightChangeKg,
	List<RecentSummaryResponse> dailySummaries
) {
}