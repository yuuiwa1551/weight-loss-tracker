package com.example.weightloss.service;

import com.example.weightloss.dto.EnergyPlanCalculationResponse;
import com.example.weightloss.dto.EnergyPlanPreviewRequest;
import com.example.weightloss.entity.EnergyPlan;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

@Service
public class PreviewFingerprintService {

	private final ObjectMapper objectMapper;

	public PreviewFingerprintService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String energyPlanFingerprint(
		Long userId,
		EnergyPlanPreviewRequest request,
		EnergyPlanCalculationResponse calculation,
		EnergyPlan activePlan
	) {
		Map<String, Object> canonical = new TreeMap<>();
		canonical.put("activePlan", activePlanSnapshot(activePlan));
		canonical.put("calculation", calculationSnapshot(calculation));
		canonical.put("contractVersion", calculation.calculationVersion());
		canonical.put("operationType", "ENERGY_PLAN");
		canonical.put("request", requestSnapshot(request));
		canonical.put("userId", userId);
		return sha256(writeCanonicalJson(canonical));
	}

	public boolean matches(String expected, String actual) {
		if (expected == null || actual == null) return false;
		return MessageDigest.isEqual(
			expected.getBytes(StandardCharsets.US_ASCII),
			actual.getBytes(StandardCharsets.US_ASCII)
		);
	}

	private Map<String, Object> activePlanSnapshot(EnergyPlan activePlan) {
		Map<String, Object> snapshot = new TreeMap<>();
		snapshot.put("id", activePlan == null ? null : activePlan.getId());
		snapshot.put("updatedAt", activePlan == null ? null : activePlan.getUpdatedAt().toString());
		return snapshot;
	}

	private Map<String, Object> requestSnapshot(EnergyPlanPreviewRequest request) {
		Map<String, Object> snapshot = new TreeMap<>();
		snapshot.put("dailyDeficitCalories", request.dailyDeficitCalories());
		snapshot.put("targetPeriodDays", request.targetPeriodDays());
		return snapshot;
	}

	private Map<String, Object> calculationSnapshot(EnergyPlanCalculationResponse calculation) {
		Map<String, Object> snapshot = new TreeMap<>();
		snapshot.put("ageYears", calculation.ageYears());
		snapshot.put("baseIntakeTargetCalories", calculation.baseIntakeTargetCalories());
		snapshot.put("baselineExpenditureCalories", calculation.baselineExpenditureCalories());
		snapshot.put("calculationMethod", calculation.calculationMethod().name());
		snapshot.put("calculationVersion", calculation.calculationVersion());
		snapshot.put("dailyDeficitCalories", calculation.dailyDeficitCalories());
		snapshot.put("deficitMode", calculation.deficitMode().name());
		snapshot.put("formulaSex", calculation.formulaSex().name());
		snapshot.put("heightCm", calculation.heightCm());
		snapshot.put("nonExerciseActivityLevel", calculation.nonExerciseActivityLevel().name());
		snapshot.put("profileUpdatedAt", calculation.profileUpdatedAt().toString());
		snapshot.put("restingEnergyCalories", calculation.restingEnergyCalories());
		snapshot.put("targetPeriodDays", calculation.targetPeriodDays());
		snapshot.put("targetWeightKg", calculation.targetWeightKg());
		snapshot.put("weightKg", calculation.weightKg());
		return snapshot;
	}

	private byte[] writeCanonicalJson(Map<String, Object> canonical) {
		try {
			return objectMapper.writeValueAsBytes(canonical);
		} catch (Exception exception) {
			throw new IllegalStateException("Could not serialize preview fingerprint input", exception);
		}
	}

	private String sha256(byte[] input) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(input));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available", exception);
		}
	}
}
