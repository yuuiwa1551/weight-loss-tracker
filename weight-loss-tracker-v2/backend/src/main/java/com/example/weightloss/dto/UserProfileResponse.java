package com.example.weightloss.dto;

import com.example.weightloss.entity.UserProfile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public record UserProfileResponse(
	Long id,
	String nickname,
	BigDecimal heightCm,
	BigDecimal currentWeightKg,
	BigDecimal targetWeightKg,
	Integer dailyCalorieGoal,
	BigDecimal bmi,
	BigDecimal weightToLoseKg,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static UserProfileResponse from(UserProfile profile) {
		BigDecimal heightMeters = profile.getHeightCm().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
		BigDecimal bmi = profile.getCurrentWeightKg()
			.divide(heightMeters.multiply(heightMeters), 1, RoundingMode.HALF_UP);
		BigDecimal weightToLose = profile.getCurrentWeightKg().subtract(profile.getTargetWeightKg()).max(BigDecimal.ZERO);

		return new UserProfileResponse(
			profile.getId(),
			profile.getNickname(),
			profile.getHeightCm(),
			profile.getCurrentWeightKg(),
			profile.getTargetWeightKg(),
			profile.getDailyCalorieGoal(),
			bmi,
			weightToLose,
			profile.getCreatedAt(),
			profile.getUpdatedAt()
		);
	}
}