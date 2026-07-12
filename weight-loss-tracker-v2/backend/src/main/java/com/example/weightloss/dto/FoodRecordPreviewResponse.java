package com.example.weightloss.dto;

import com.example.weightloss.entity.MealType;
import com.example.weightloss.entity.NutritionSource;
import com.example.weightloss.entity.RecordSource;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FoodRecordPreviewResponse(
	LocalDate recordDate,
	MealType mealType,
	String foodName,
	Integer calories,
	BigDecimal protein,
	BigDecimal fat,
	BigDecimal carbohydrate,
	String note,
	RecordSource source,
	NutritionSource nutritionSource,
	String estimationNote,
	EnergyBudgetResponse projectedEnergyBudget,
	String previewFingerprint
) {
}
