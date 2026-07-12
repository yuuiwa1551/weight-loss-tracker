package com.example.weightloss.dto;

import com.example.weightloss.entity.FoodRecord;
import com.example.weightloss.entity.MealType;
import com.example.weightloss.entity.NutritionSource;
import com.example.weightloss.entity.RecordSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FoodRecordResponse(
	Long id,
	LocalDate recordDate,
	MealType mealType,
	String foodName,
	Integer calories,
	BigDecimal protein,
	BigDecimal fat,
	BigDecimal carbohydrate,
	String note,
	RecordSource source,
	String clientRequestId,
	NutritionSource nutritionSource,
	String estimationNote,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	EnergyBudgetResponse energyBudget
) {
	public static FoodRecordResponse from(FoodRecord record) {
		return from(record, null);
	}

	public static FoodRecordResponse from(FoodRecord record, EnergyBudgetResponse energyBudget) {
		return new FoodRecordResponse(
			record.getId(),
			record.getRecordDate(),
			record.getMealType(),
			record.getFoodName(),
			record.getCalories(),
			record.getProtein(),
			record.getFat(),
			record.getCarbohydrate(),
			record.getNote(),
			record.getSource(),
			record.getClientRequestId(),
			record.getNutritionSource(),
			record.getEstimationNote(),
			record.getCreatedAt(),
			record.getUpdatedAt(),
			energyBudget
		);
	}
}
