package com.example.weightloss;

import com.example.weightloss.dto.ConfirmEnergyPlanRequest;
import com.example.weightloss.dto.EnergyPlanPreviewRequest;
import com.example.weightloss.entity.CalorieGoalMode;
import com.example.weightloss.entity.EnergyCalculationMethod;
import com.example.weightloss.entity.EnergyPlanDeficitMode;
import com.example.weightloss.entity.EnergyPlanStatus;
import com.example.weightloss.entity.FormulaSex;
import com.example.weightloss.entity.NonExerciseActivityLevel;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnergyContractTests {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void exposesStableDomainEnumValues() {
		assertThat(FormulaSex.values()).containsExactly(FormulaSex.MALE, FormulaSex.FEMALE);
		assertThat(NonExerciseActivityLevel.values()).containsExactly(
			NonExerciseActivityLevel.SEDENTARY,
			NonExerciseActivityLevel.LIGHT,
			NonExerciseActivityLevel.MODERATE,
			NonExerciseActivityLevel.HIGH
		);
		assertThat(CalorieGoalMode.values()).containsExactly(
			CalorieGoalMode.UNSET,
			CalorieGoalMode.MANUAL,
			CalorieGoalMode.AUTO
		);
		assertThat(EnergyCalculationMethod.values()).containsExactly(EnergyCalculationMethod.MIFFLIN_ST_JEOR);
		assertThat(EnergyPlanDeficitMode.values()).containsExactly(
			EnergyPlanDeficitMode.EXPLICIT,
			EnergyPlanDeficitMode.TARGET_PERIOD,
			EnergyPlanDeficitMode.DEFAULT_RATE
		);
		assertThat(EnergyPlanStatus.values()).containsExactly(
			EnergyPlanStatus.ACTIVE,
			EnergyPlanStatus.SUPERSEDED
		);
	}

	@Test
	void previewAcceptsAtMostOneDeficitStrategy() {
		assertThat(validator.validate(new EnergyPlanPreviewRequest(null, null))).isEmpty();
		assertThat(validator.validate(new EnergyPlanPreviewRequest(500, null))).isEmpty();
		assertThat(validator.validate(new EnergyPlanPreviewRequest(null, 90))).isEmpty();
		assertThat(validator.validate(new EnergyPlanPreviewRequest(500, 90))).isNotEmpty();
	}

	@Test
	void confirmationRequiresPreviewFingerprintAndClientRequestId() {
		ConfirmEnergyPlanRequest request = new ConfirmEnergyPlanRequest(
			new EnergyPlanPreviewRequest(null, null),
			"",
			""
		);
		assertThat(validator.validate(request))
			.extracting(violation -> violation.getPropertyPath().toString())
			.containsExactlyInAnyOrder("previewFingerprint", "clientRequestId");
	}
}
