package com.example.weightloss.service;

import com.example.weightloss.config.EnergyCalculationProperties;
import com.example.weightloss.dto.EnergyPlanCalculationResponse;
import com.example.weightloss.dto.EnergyPlanPreviewRequest;
import com.example.weightloss.entity.EnergyCalculationMethod;
import com.example.weightloss.entity.EnergyPlanDeficitMode;
import com.example.weightloss.entity.FormulaSex;
import com.example.weightloss.entity.NonExerciseActivityLevel;
import com.example.weightloss.entity.UserProfile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class EnergyCalculationService {

	private static final BigDecimal TEN = new BigDecimal("10");
	private static final BigDecimal HEIGHT_COEFFICIENT = new BigDecimal("6.25");
	private static final BigDecimal AGE_COEFFICIENT = new BigDecimal("5");
	private static final BigDecimal MALE_ADJUSTMENT = new BigDecimal("5");
	private static final BigDecimal FEMALE_ADJUSTMENT = new BigDecimal("-161");

	private final EnergyCalculationProperties properties;

	public EnergyCalculationService(EnergyCalculationProperties properties) {
		this.properties = properties;
	}

	public EnergyPlanCalculationResponse calculate(UserProfile profile, EnergyPlanPreviewRequest request) {
		List<String> missingFields = missingEnergyFields(profile);
		if (!missingFields.isEmpty()) {
			throw new IllegalArgumentException("Energy profile is incomplete: " + String.join(", ", missingFields));
		}
		if (request.targetPeriodDays() != null && profile.getTargetWeightKg() == null) {
			throw new IllegalArgumentException("targetWeightKg is required for targetPeriodDays");
		}

		BigDecimal restingEnergy = calculateRestingEnergy(profile);
		BigDecimal baselineExpenditure = restingEnergy.multiply(activityFactor(profile.getNonExerciseActivityLevel()));
		EnergyPlanDeficitMode deficitMode = resolveDeficitMode(request);
		BigDecimal dailyDeficit = calculateDailyDeficit(profile, request, baselineExpenditure, deficitMode);
		BigDecimal baseIntakeTarget = baselineExpenditure.subtract(dailyDeficit);

		return new EnergyPlanCalculationResponse(
			EnergyCalculationMethod.MIFFLIN_ST_JEOR,
			properties.getCalculationVersion(),
			deficitMode,
			profile.getAgeYears(),
			profile.getFormulaSex(),
			profile.getHeightCm(),
			profile.getCurrentWeightKg(),
			profile.getTargetWeightKg(),
			profile.getNonExerciseActivityLevel(),
			request.targetPeriodDays(),
			toCalories(restingEnergy),
			toCalories(baselineExpenditure),
			toCalories(dailyDeficit),
			toCalories(baseIntakeTarget),
			profile.getUpdatedAt()
		);
	}

	public List<String> missingEnergyFields(UserProfile profile) {
		List<String> missing = new ArrayList<>();
		if (profile.getAgeYears() == null) missing.add("ageYears");
		if (profile.getFormulaSex() == null) missing.add("formulaSex");
		if (profile.getHeightCm() == null) missing.add("heightCm");
		if (profile.getCurrentWeightKg() == null) missing.add("currentWeightKg");
		if (profile.getNonExerciseActivityLevel() == null) missing.add("nonExerciseActivityLevel");
		return List.copyOf(missing);
	}

	BigDecimal activityFactor(NonExerciseActivityLevel level) {
		return switch (level) {
			case SEDENTARY -> new BigDecimal("1.20");
			case LIGHT -> new BigDecimal("1.30");
			case MODERATE -> new BigDecimal("1.45");
			case HIGH -> new BigDecimal("1.60");
		};
	}

	private BigDecimal calculateRestingEnergy(UserProfile profile) {
		BigDecimal adjustment = profile.getFormulaSex() == FormulaSex.MALE
			? MALE_ADJUSTMENT
			: FEMALE_ADJUSTMENT;
		return profile.getCurrentWeightKg().multiply(TEN)
			.add(profile.getHeightCm().multiply(HEIGHT_COEFFICIENT))
			.subtract(BigDecimal.valueOf(profile.getAgeYears()).multiply(AGE_COEFFICIENT))
			.add(adjustment);
	}

	private EnergyPlanDeficitMode resolveDeficitMode(EnergyPlanPreviewRequest request) {
		if (request.dailyDeficitCalories() != null) return EnergyPlanDeficitMode.EXPLICIT;
		if (request.targetPeriodDays() != null) return EnergyPlanDeficitMode.TARGET_PERIOD;
		return EnergyPlanDeficitMode.DEFAULT_RATE;
	}

	private BigDecimal calculateDailyDeficit(
		UserProfile profile,
		EnergyPlanPreviewRequest request,
		BigDecimal baselineExpenditure,
		EnergyPlanDeficitMode mode
	) {
		return switch (mode) {
			case EXPLICIT -> BigDecimal.valueOf(request.dailyDeficitCalories());
			case DEFAULT_RATE -> baselineExpenditure.multiply(properties.getDefaultDeficitRate());
			case TARGET_PERIOD -> targetPeriodDeficit(profile, request.targetPeriodDays());
		};
	}

	private BigDecimal targetPeriodDeficit(UserProfile profile, int targetPeriodDays) {
		BigDecimal weightDifference = profile.getCurrentWeightKg().subtract(profile.getTargetWeightKg());
		if (weightDifference.signum() < 0) {
			throw new IllegalArgumentException("targetWeightKg cannot exceed currentWeightKg for a deficit plan");
		}
		return weightDifference.multiply(properties.getKcalPerKilogram())
			.divide(BigDecimal.valueOf(targetPeriodDays), 8, RoundingMode.HALF_UP);
	}

	private int toCalories(BigDecimal value) {
		return value.setScale(0, RoundingMode.HALF_UP).intValueExact();
	}
}
