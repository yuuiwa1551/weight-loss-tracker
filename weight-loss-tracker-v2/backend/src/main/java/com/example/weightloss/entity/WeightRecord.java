package com.example.weightloss.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
	indexes = {
		@Index(name = "idx_weight_records_user_date", columnList = "user_id,record_date"),
		@Index(name = "uk_weight_records_user_request", columnList = "user_id,client_request_id", unique = true)
	}
)
public class WeightRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Column(nullable = false)
	private LocalDate recordDate;

	@Column(nullable = false, precision = 6, scale = 2)
	private BigDecimal weightKg;

	@Column(precision = 5, scale = 2)
	private BigDecimal bodyFatPercentage;

	@Column(length = 500)
	private String note;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RecordSource source;

	@Column(length = 160)
	private String clientRequestId;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	public WeightRecord(
		AppUser user,
		LocalDate recordDate,
		BigDecimal weightKg,
		BigDecimal bodyFatPercentage,
		String note,
		RecordSource source,
		String clientRequestId
	) {
		this.user = user;
		this.recordDate = recordDate;
		this.weightKg = weightKg;
		this.bodyFatPercentage = bodyFatPercentage;
		this.note = note;
		this.source = source;
		this.clientRequestId = clientRequestId;
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
