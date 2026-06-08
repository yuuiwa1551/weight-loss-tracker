package com.example.weightloss.dto;

import com.example.weightloss.entity.MealType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateFoodRecordRequest(
	@NotNull @PastOrPresent LocalDate recordDate,
	@NotNull MealType mealType,
	@NotBlank @Size(max = 120) String foodName,
	@NotNull @PositiveOrZero Integer calories,
	@PositiveOrZero BigDecimal protein,
	@PositiveOrZero BigDecimal fat,
	@PositiveOrZero BigDecimal carbohydrate,
	@Size(max = 500) String note
) {
}