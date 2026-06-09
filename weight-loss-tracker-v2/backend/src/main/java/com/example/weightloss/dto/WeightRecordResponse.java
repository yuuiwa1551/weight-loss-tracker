package com.example.weightloss.dto;

import com.example.weightloss.entity.WeightRecord;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record WeightRecordResponse(
	Long id,
	LocalDate recordDate,
	BigDecimal weightKg,
	BigDecimal bodyFatPercentage,
	String note,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static WeightRecordResponse from(WeightRecord record) {
		return new WeightRecordResponse(
			record.getId(),
			record.getRecordDate(),
			record.getWeightKg(),
			record.getBodyFatPercentage(),
			record.getNote(),
			record.getCreatedAt(),
			record.getUpdatedAt()
		);
	}
}