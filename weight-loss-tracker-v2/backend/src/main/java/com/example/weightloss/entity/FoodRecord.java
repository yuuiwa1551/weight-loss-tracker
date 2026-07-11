package com.example.weightloss.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
	name = "food_records",
	indexes = {
		@Index(name = "idx_food_records_user_date", columnList = "user_id,record_date"),
		@Index(name = "uk_food_records_user_request", columnList = "user_id,client_request_id", unique = true)
	}
)
public class FoodRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Column(nullable = false)
	private LocalDate recordDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MealType mealType;

	@Column(nullable = false, length = 120)
	private String foodName;

	@Column(nullable = false)
	private Integer calories;

	@Column(nullable = false, precision = 8, scale = 2)
	private BigDecimal protein = BigDecimal.ZERO;

	@Column(nullable = false, precision = 8, scale = 2)
	private BigDecimal fat = BigDecimal.ZERO;

	@Column(nullable = false, precision = 8, scale = 2)
	private BigDecimal carbohydrate = BigDecimal.ZERO;

	@Column(length = 500)
	private String note;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private RecordSource source;

	@Column(length = 160)
	private String clientRequestId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private NutritionSource nutritionSource;

	@Column(length = 1000)
	private String estimationNote;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	public FoodRecord(
		AppUser user,
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
		String estimationNote
	) {
		this.user = user;
		this.recordDate = recordDate;
		this.mealType = mealType;
		this.foodName = foodName;
		this.calories = calories;
		this.protein = protein == null ? BigDecimal.ZERO : protein;
		this.fat = fat == null ? BigDecimal.ZERO : fat;
		this.carbohydrate = carbohydrate == null ? BigDecimal.ZERO : carbohydrate;
		this.note = note;
		this.source = source;
		this.clientRequestId = clientRequestId;
		this.nutritionSource = nutritionSource;
		this.estimationNote = estimationNote;
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
