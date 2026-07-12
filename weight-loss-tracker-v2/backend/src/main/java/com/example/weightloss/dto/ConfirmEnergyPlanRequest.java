package com.example.weightloss.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ConfirmEnergyPlanRequest(
	@NotNull @Valid EnergyPlanPreviewRequest calculation,
	@NotBlank @Size(max = 128) String previewFingerprint,
	@NotBlank @Size(max = 160) String clientRequestId
) {
}
