package com.example.weightloss.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateWeightRecordRequest(
	@NotNull @PastOrPresent LocalDate recordDate,
	@NotNull @DecimalMin("1.0") @DecimalMax("500.0") BigDecimal weightKg,
	@DecimalMin("0.0") @DecimalMax("100.0") BigDecimal bodyFatPercentage,
	@Size(max = 500) String note
) {
}
