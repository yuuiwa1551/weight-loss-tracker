package com.example.weightloss.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
	name = "weight_records",
	indexes = @Index(name = "idx_weight_records_record_date", columnList = "record_date")
)
public class WeightRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private LocalDate recordDate;

	@Column(nullable = false, precision = 6, scale = 2)
	private BigDecimal weightKg;

	@Column(precision = 5, scale = 2)
	private BigDecimal bodyFatPercentage;

	@Column(length = 500)
	private String note;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	public WeightRecord(LocalDate recordDate, BigDecimal weightKg, BigDecimal bodyFatPercentage, String note) {
		this.recordDate = recordDate;
		this.weightKg = weightKg;
		this.bodyFatPercentage = bodyFatPercentage;
		this.note = note;
	}

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
