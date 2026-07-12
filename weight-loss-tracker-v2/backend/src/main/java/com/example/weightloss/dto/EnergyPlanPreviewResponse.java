package com.example.weightloss.dto;

public record EnergyPlanPreviewResponse(
	EnergyPlanCalculationResponse calculation,
	String previewFingerprint
) {
}
