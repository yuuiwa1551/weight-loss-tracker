package com.example.weightloss.service;

import com.example.weightloss.dto.EnergyBudgetResponse;
import com.example.weightloss.entity.CalorieGoalMode;
import com.example.weightloss.entity.EnergyPlan;
import com.example.weightloss.entity.ExerciseRecord;
import com.example.weightloss.entity.FoodRecord;
import com.example.weightloss.entity.UserProfile;
import com.example.weightloss.repository.ExerciseRecordRepository;
import com.example.weightloss.repository.FoodRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class EnergyBudgetService {

	private final ProfileService profileService;
	private final EnergyPlanService energyPlanService;
	private final FoodRecordRepository foodRecordRepository;
	private final ExerciseRecordRepository exerciseRecordRepository;

	public EnergyBudgetService(
		ProfileService profileService,
		EnergyPlanService energyPlanService,
		FoodRecordRepository foodRecordRepository,
		ExerciseRecordRepository exerciseRecordRepository
	) {
		this.profileService = profileService;
		this.energyPlanService = energyPlanService;
		this.foodRecordRepository = foodRecordRepository;
		this.exerciseRecordRepository = exerciseRecordRepository;
	}

	@Transactional(readOnly = true)
	public EnergyBudgetResponse getDailyBudget(Long userId, LocalDate date) {
		UserProfile profile = profileService.getProfileEntity(userId);
		int caloriesConsumed = foodRecordRepository
			.findByUserIdAndRecordDateOrderByCreatedAtAscIdAsc(userId, date)
			.stream().mapToInt(FoodRecord::getCalories).sum();
		int exerciseCalories = exerciseRecordRepository
			.findByUserIdAndRecordDateOrderByCreatedAtAscIdAsc(userId, date)
			.stream().mapToInt(ExerciseRecord::getCaloriesBurned).sum();
		EnergyPlan activePlan = energyPlanService.findActiveEntity(userId)
			.filter(plan -> !date.isBefore(plan.getEffectiveFrom()))
			.orElse(null);

		if (profile.effectiveCalorieGoalMode() == CalorieGoalMode.AUTO && activePlan != null) {
			return automaticBudget(date, activePlan, exerciseCalories, caloriesConsumed);
		}
		if (profile.effectiveCalorieGoalMode() == CalorieGoalMode.MANUAL) {
			return manualBudget(date, profile.getDailyCalorieGoal(), exerciseCalories, caloriesConsumed);
		}
		return unsetBudget(date, exerciseCalories, caloriesConsumed);
	}

	private EnergyBudgetResponse automaticBudget(
		LocalDate date,
		EnergyPlan plan,
		int exerciseCalories,
		int caloriesConsumed
	) {
		int estimatedTotalExpenditure = plan.getBaselineExpenditureCalories() + exerciseCalories;
		int todayIntakeBudget = plan.getBaseIntakeTargetCalories() + exerciseCalories;
		return new EnergyBudgetResponse(
			date,
			plan.getRestingEnergyCalories(),
			plan.getBaselineExpenditureCalories(),
			exerciseCalories,
			estimatedTotalExpenditure,
			plan.getBaseIntakeTargetCalories(),
			todayIntakeBudget,
			caloriesConsumed,
			todayIntakeBudget - caloriesConsumed,
			estimatedTotalExpenditure - caloriesConsumed,
			CalorieGoalMode.AUTO,
			plan.getCalculationMethod(),
			plan.getCalculationVersion()
		);
	}

	private EnergyBudgetResponse manualBudget(
		LocalDate date,
		int dailyGoal,
		int exerciseCalories,
		int caloriesConsumed
	) {
		int todayIntakeBudget = dailyGoal + exerciseCalories;
		return new EnergyBudgetResponse(
			date,
			null,
			null,
			exerciseCalories,
			null,
			dailyGoal,
			todayIntakeBudget,
			caloriesConsumed,
			todayIntakeBudget - caloriesConsumed,
			null,
			CalorieGoalMode.MANUAL,
			null,
			null
		);
	}

	private EnergyBudgetResponse unsetBudget(LocalDate date, int exerciseCalories, int caloriesConsumed) {
		return new EnergyBudgetResponse(
			date,
			null,
			null,
			exerciseCalories,
			null,
			null,
			null,
			caloriesConsumed,
			null,
			null,
			CalorieGoalMode.UNSET,
			null,
			null
		);
	}
}
