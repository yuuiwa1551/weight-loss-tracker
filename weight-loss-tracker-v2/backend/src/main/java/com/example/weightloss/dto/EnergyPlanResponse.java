package com.example.weightloss.dto;

import com.example.weightloss.entity.EnergyPlanStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EnergyPlanResponse(
	Long id,
	EnergyPlanCalculationResponse calculation,
	EnergyPlanStatus status,
	LocalDate effectiveFrom,
	String clientRequestId,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
}
