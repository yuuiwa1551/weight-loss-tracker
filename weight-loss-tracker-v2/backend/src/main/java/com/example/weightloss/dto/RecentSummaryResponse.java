package com.example.weightloss.dto;

import com.example.weightloss.entity.GoalStatus;

import java.time.LocalDate;

public record RecentSummaryResponse(
	LocalDate date,
	Integer totalCaloriesConsumed,
	Integer totalCaloriesBurned,
	Integer netCalories,
	Integer dailyCalorieGoal,
	Integer calorieDifference,
	GoalStatus goalStatus
) {
	public static RecentSummaryResponse fromDaily(DailySummaryResponse summary) {
		return new RecentSummaryResponse(
			summary.date(),
			summary.totalCaloriesConsumed(),
			summary.totalCaloriesBurned(),
			summary.netCalories(),
			summary.dailyCalorieGoal(),
			summary.calorieDifference(),
			summary.goalStatus()
		);
	}
}