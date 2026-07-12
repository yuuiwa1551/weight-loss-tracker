package com.example.weightloss.service;

import com.example.weightloss.dto.EnergyBudgetResponse;
import com.example.weightloss.entity.EnergyPlan;
import com.example.weightloss.entity.ExerciseRecord;
import com.example.weightloss.entity.FoodRecord;
import com.example.weightloss.entity.UserProfile;

import java.util.List;

record EnergyBudgetProjection(
	EnergyBudgetResponse budget,
	UserProfile profile,
	EnergyPlan activePlan,
	List<FoodRecord> foodRecords,
	List<ExerciseRecord> exerciseRecords
) {
}
