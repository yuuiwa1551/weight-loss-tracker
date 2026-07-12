package com.example.weightloss.dto;

import com.example.weightloss.entity.CalorieGoalMode;
import com.example.weightloss.entity.FormulaSex;
import com.example.weightloss.entity.NonExerciseActivityLevel;
import com.example.weightloss.entity.UserProfile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record UserProfileResponse(
	Long id,
	String nickname,
	BigDecimal heightCm,
	BigDecimal currentWeightKg,
	BigDecimal targetWeightKg,
	Integer dailyCalorieGoal,
	Integer ageYears,
	FormulaSex formulaSex,
	NonExerciseActivityLevel nonExerciseActivityLevel,
	CalorieGoalMode calorieGoalMode,
	BigDecimal bmi,
	BigDecimal weightToLoseKg,
	Boolean profileComplete,
	List<String> missingFields,
	Boolean energyProfileComplete,
	List<String> energyMissingFields,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static UserProfileResponse from(UserProfile profile) {
		BigDecimal bmi = null;
		if (profile.getHeightCm() != null && profile.getCurrentWeightKg() != null) {
			BigDecimal heightMeters = profile.getHeightCm().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
			bmi = profile.getCurrentWeightKg()
				.divide(heightMeters.multiply(heightMeters), 1, RoundingMode.HALF_UP);
		}
		BigDecimal weightToLose = profile.getCurrentWeightKg() == null || profile.getTargetWeightKg() == null
			? null
			: profile.getCurrentWeightKg().subtract(profile.getTargetWeightKg()).max(BigDecimal.ZERO);
		List<String> missingFields = new ArrayList<>();
		if (profile.getHeightCm() == null) missingFields.add("heightCm");
		if (profile.getCurrentWeightKg() == null) missingFields.add("currentWeightKg");
		if (profile.getTargetWeightKg() == null) missingFields.add("targetWeightKg");
		if (profile.getDailyCalorieGoal() == null) missingFields.add("dailyCalorieGoal");
		List<String> energyMissingFields = new ArrayList<>();
		if (profile.getAgeYears() == null) energyMissingFields.add("ageYears");
		if (profile.getFormulaSex() == null) energyMissingFields.add("formulaSex");
		if (profile.getHeightCm() == null) energyMissingFields.add("heightCm");
		if (profile.getCurrentWeightKg() == null) energyMissingFields.add("currentWeightKg");
		if (profile.getNonExerciseActivityLevel() == null) energyMissingFields.add("nonExerciseActivityLevel");

		return new UserProfileResponse(
			profile.getId(),
			profile.getNickname(),
			profile.getHeightCm(),
			profile.getCurrentWeightKg(),
			profile.getTargetWeightKg(),
			profile.getDailyCalorieGoal(),
			profile.getAgeYears(),
			profile.getFormulaSex(),
			profile.getNonExerciseActivityLevel(),
			profile.effectiveCalorieGoalMode(),
			bmi,
			weightToLose,
			missingFields.isEmpty(),
			List.copyOf(missingFields),
			energyMissingFields.isEmpty(),
			List.copyOf(energyMissingFields),
			profile.getCreatedAt(),
			profile.getUpdatedAt()
		);
	}
}
