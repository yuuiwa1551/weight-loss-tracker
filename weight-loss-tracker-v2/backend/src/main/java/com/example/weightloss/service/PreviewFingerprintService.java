package com.example.weightloss.service;

import com.example.weightloss.config.EnergyCalculationProperties;
import com.example.weightloss.dto.CreateExerciseRecordRequest;
import com.example.weightloss.dto.CreateFoodRecordRequest;
import com.example.weightloss.dto.EnergyBudgetResponse;
import com.example.weightloss.dto.EnergyPlanCalculationResponse;
import com.example.weightloss.dto.EnergyPlanPreviewRequest;
import com.example.weightloss.entity.EnergyPlan;
import com.example.weightloss.entity.ExerciseRecord;
import com.example.weightloss.entity.FoodRecord;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class PreviewFingerprintService {

	private final ObjectMapper objectMapper;
	private final EnergyCalculationProperties properties;

	public PreviewFingerprintService(ObjectMapper objectMapper, EnergyCalculationProperties properties) {
		this.objectMapper = objectMapper;
		this.properties = properties;
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

	public String foodRecordFingerprint(
		Long userId,
		CreateFoodRecordRequest request,
		EnergyBudgetProjection projection
	) {
		Map<String, Object> snapshot = new TreeMap<>();
		snapshot.put("calories", request.calories());
		snapshot.put("carbohydrate", request.carbohydrate());
		snapshot.put("clientRequestId", request.clientRequestId());
		snapshot.put("estimationNote", request.estimationNote());
		snapshot.put("fat", request.fat());
		snapshot.put("foodName", request.foodName());
		snapshot.put("mealType", request.mealType().name());
		snapshot.put("note", request.note());
		snapshot.put("nutritionSource", request.nutritionSource().name());
		snapshot.put("protein", request.protein());
		snapshot.put("recordDate", request.recordDate().toString());
		snapshot.put("source", request.source().name());
		return recordFingerprint(userId, "FOOD_RECORD", snapshot, projection);
	}

	public String exerciseRecordFingerprint(
		Long userId,
		CreateExerciseRecordRequest request,
		EnergyBudgetProjection projection
	) {
		Map<String, Object> snapshot = new TreeMap<>();
		snapshot.put("caloriesBurned", request.caloriesBurned());
		snapshot.put("clientRequestId", request.clientRequestId());
		snapshot.put("durationMinutes", request.durationMinutes());
		snapshot.put("exerciseName", request.exerciseName());
		snapshot.put("exerciseType", request.exerciseType());
		snapshot.put("note", request.note());
		snapshot.put("recordDate", request.recordDate().toString());
		snapshot.put("source", request.source().name());
		return recordFingerprint(userId, "EXERCISE_RECORD", snapshot, projection);
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

	private String recordFingerprint(
		Long userId,
		String operationType,
		Map<String, Object> request,
		EnergyBudgetProjection projection
	) {
		Map<String, Object> canonical = new TreeMap<>();
		canonical.put("activePlan", activePlanSnapshot(projection.activePlan()));
		canonical.put("contractVersion", properties.getCalculationVersion());
		canonical.put("operationType", operationType);
		canonical.put("profileUpdatedAt", projection.profile().getUpdatedAt().toString());
		canonical.put("projectedEnergyBudget", budgetSnapshot(projection.budget()));
		canonical.put("recordState", recordStateSnapshot(projection));
		canonical.put("request", request);
		canonical.put("userId", userId);
		return sha256(writeCanonicalJson(canonical));
	}

	private Map<String, Object> budgetSnapshot(EnergyBudgetResponse budget) {
		Map<String, Object> snapshot = new TreeMap<>();
		snapshot.put("baseIntakeTargetCalories", budget.baseIntakeTargetCalories());
		snapshot.put("baselineExpenditureCalories", budget.baselineExpenditureCalories());
		snapshot.put("calculationMethod", budget.calculationMethod() == null ? null : budget.calculationMethod().name());
		snapshot.put("calculationVersion", budget.calculationVersion());
		snapshot.put("caloriesConsumed", budget.caloriesConsumed());
		snapshot.put("date", budget.date().toString());
		snapshot.put("estimatedTotalExpenditureCalories", budget.estimatedTotalExpenditureCalories());
		snapshot.put("exerciseCaloriesBurned", budget.exerciseCaloriesBurned());
		snapshot.put("goalMode", budget.goalMode().name());
		snapshot.put("projectedDeficitCalories", budget.projectedDeficitCalories());
		snapshot.put("remainingIntakeCalories", budget.remainingIntakeCalories());
		snapshot.put("restingEnergyCalories", budget.restingEnergyCalories());
		snapshot.put("todayIntakeBudgetCalories", budget.todayIntakeBudgetCalories());
		return snapshot;
	}

	private Map<String, Object> recordStateSnapshot(EnergyBudgetProjection projection) {
		Map<String, Object> snapshot = new TreeMap<>();
		snapshot.put("exerciseRecords", exerciseState(projection.exerciseRecords()));
		snapshot.put("foodRecords", foodState(projection.foodRecords()));
		return snapshot;
	}

	private List<Map<String, Object>> foodState(List<FoodRecord> records) {
		return records.stream().map(record -> {
			Map<String, Object> snapshot = new TreeMap<>();
			snapshot.put("calories", record.getCalories());
			snapshot.put("id", record.getId());
			snapshot.put("updatedAt", record.getUpdatedAt().toString());
			return snapshot;
		}).toList();
	}

	private List<Map<String, Object>> exerciseState(List<ExerciseRecord> records) {
		return records.stream().map(record -> {
			Map<String, Object> snapshot = new TreeMap<>();
			snapshot.put("caloriesBurned", record.getCaloriesBurned());
			snapshot.put("durationMinutes", record.getDurationMinutes());
			snapshot.put("id", record.getId());
			snapshot.put("updatedAt", record.getUpdatedAt().toString());
			return snapshot;
		}).toList();
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
