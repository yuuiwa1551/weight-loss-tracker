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
import java.util.List;

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
		return project(profile, date, 0, 0).budget();
	}

	EnergyBudgetProjection project(
		UserProfile profile,
		LocalDate date,
		int additionalCaloriesConsumed,
		int additionalExerciseCalories
	) {
		Long userId = profile.getUser().getId();
		List<FoodRecord> foodRecords = foodRecordRepository
			.findByUserIdAndRecordDateOrderByCreatedAtAscIdAsc(userId, date);
		List<ExerciseRecord> exerciseRecords = exerciseRecordRepository
			.findByUserIdAndRecordDateOrderByCreatedAtAscIdAsc(userId, date);
		EnergyPlan activePlan = energyPlanService.findActiveEntity(userId).orElse(null);
		return project(
			profile,
			activePlan,
			date,
			foodRecords,
			exerciseRecords,
			additionalCaloriesConsumed,
			additionalExerciseCalories
		);
	}

	EnergyBudgetProjection project(
		UserProfile profile,
		EnergyPlan activePlan,
		LocalDate date,
		List<FoodRecord> foodRecords,
		List<ExerciseRecord> exerciseRecords,
		int additionalCaloriesConsumed,
		int additionalExerciseCalories
	) {
		int caloriesConsumed = foodRecords.stream().mapToInt(FoodRecord::getCalories).sum()
			+ additionalCaloriesConsumed;
		int exerciseCalories = exerciseRecords.stream().mapToInt(ExerciseRecord::getCaloriesBurned).sum()
			+ additionalExerciseCalories;
		EnergyPlan applicablePlan = activePlan != null && !date.isBefore(activePlan.getEffectiveFrom())
			? activePlan
			: null;
		EnergyBudgetResponse budget;

		if (profile.effectiveCalorieGoalMode() == CalorieGoalMode.AUTO && applicablePlan != null) {
			budget = automaticBudget(date, applicablePlan, exerciseCalories, caloriesConsumed);
		} else if (profile.effectiveCalorieGoalMode() == CalorieGoalMode.MANUAL) {
			budget = manualBudget(date, profile.getDailyCalorieGoal(), exerciseCalories, caloriesConsumed);
		} else {
			budget = unsetBudget(date, exerciseCalories, caloriesConsumed);
		}
		return new EnergyBudgetProjection(
			budget,
			profile,
			applicablePlan,
			List.copyOf(foodRecords),
			List.copyOf(exerciseRecords)
		);
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
