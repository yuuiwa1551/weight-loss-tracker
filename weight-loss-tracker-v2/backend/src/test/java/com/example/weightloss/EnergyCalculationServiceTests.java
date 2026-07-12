package com.example.weightloss;

import com.example.weightloss.config.EnergyCalculationProperties;
import com.example.weightloss.dto.EnergyPlanCalculationResponse;
import com.example.weightloss.dto.EnergyPlanPreviewRequest;
import com.example.weightloss.entity.AppUser;
import com.example.weightloss.entity.EnergyPlanDeficitMode;
import com.example.weightloss.entity.FormulaSex;
import com.example.weightloss.entity.NonExerciseActivityLevel;
import com.example.weightloss.entity.UserProfile;
import com.example.weightloss.service.EnergyCalculationService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnergyCalculationServiceTests {

	private final EnergyCalculationService service = new EnergyCalculationService(new EnergyCalculationProperties());

	@Test
	void calculatesFrozenFemaleExplicitDeficitExample() {
		UserProfile profile = completeProfile(24, FormulaSex.FEMALE, "165.0", "52.0", "48.0", NonExerciseActivityLevel.LIGHT);

		EnergyPlanCalculationResponse result = service.calculate(profile, new EnergyPlanPreviewRequest(450, null));

		assertThat(result.deficitMode()).isEqualTo(EnergyPlanDeficitMode.EXPLICIT);
		assertThat(result.restingEnergyCalories()).isEqualTo(1270);
		assertThat(result.baselineExpenditureCalories()).isEqualTo(1651);
		assertThat(result.dailyDeficitCalories()).isEqualTo(450);
		assertThat(result.baseIntakeTargetCalories()).isEqualTo(1201);
		assertThat(result.calculationVersion()).isEqualTo("P6_V1");
	}

	@Test
	void appliesEveryFrozenActivityFactor() {
		assertBaselineFor(NonExerciseActivityLevel.SEDENTARY, 1524);
		assertBaselineFor(NonExerciseActivityLevel.LIGHT, 1651);
		assertBaselineFor(NonExerciseActivityLevel.MODERATE, 1842);
		assertBaselineFor(NonExerciseActivityLevel.HIGH, 2032);
	}

	@Test
	void calculatesTargetPeriodAndDefaultRateDeficits() {
		UserProfile profile = completeProfile(24, FormulaSex.FEMALE, "165.0", "52.0", "48.0", NonExerciseActivityLevel.LIGHT);

		EnergyPlanCalculationResponse targetPeriod = service.calculate(profile, new EnergyPlanPreviewRequest(null, 100));
		EnergyPlanCalculationResponse defaultRate = service.calculate(profile, new EnergyPlanPreviewRequest(null, null));

		assertThat(targetPeriod.deficitMode()).isEqualTo(EnergyPlanDeficitMode.TARGET_PERIOD);
		assertThat(targetPeriod.dailyDeficitCalories()).isEqualTo(308);
		assertThat(defaultRate.deficitMode()).isEqualTo(EnergyPlanDeficitMode.DEFAULT_RATE);
		assertThat(defaultRate.dailyDeficitCalories()).isEqualTo(248);
	}

	@Test
	void reportsMissingFieldsAndRejectsInvalidTargetPeriodInputs() {
		UserProfile incomplete = new UserProfile(new AppUser("aiocqhttp", "1", "Incomplete"), "Incomplete");
		assertThat(service.missingEnergyFields(incomplete)).containsExactly(
			"ageYears", "formulaSex", "heightCm", "currentWeightKg", "nonExerciseActivityLevel"
		);
		assertThatThrownBy(() -> service.calculate(incomplete, new EnergyPlanPreviewRequest(null, null)))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("ageYears");

		UserProfile missingTarget = completeProfile(24, FormulaSex.FEMALE, "165.0", "52.0", null, NonExerciseActivityLevel.LIGHT);
		assertThatThrownBy(() -> service.calculate(missingTarget, new EnergyPlanPreviewRequest(null, 90)))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("targetWeightKg");
	}

	private void assertBaselineFor(NonExerciseActivityLevel level, int expected) {
		UserProfile profile = completeProfile(24, FormulaSex.FEMALE, "165.0", "52.0", "48.0", level);
		assertThat(service.calculate(profile, new EnergyPlanPreviewRequest(0, null)).baselineExpenditureCalories())
			.isEqualTo(expected);
	}

	private UserProfile completeProfile(
		int age,
		FormulaSex formulaSex,
		String height,
		String weight,
		String targetWeight,
		NonExerciseActivityLevel activityLevel
	) {
		UserProfile profile = new UserProfile(new AppUser("aiocqhttp", "1", "Test"), "Test");
		profile.setAgeYears(age);
		profile.setFormulaSex(formulaSex);
		profile.setHeightCm(new BigDecimal(height));
		profile.setCurrentWeightKg(new BigDecimal(weight));
		profile.setTargetWeightKg(targetWeight == null ? null : new BigDecimal(targetWeight));
		profile.setNonExerciseActivityLevel(activityLevel);
		return profile;
	}
}
