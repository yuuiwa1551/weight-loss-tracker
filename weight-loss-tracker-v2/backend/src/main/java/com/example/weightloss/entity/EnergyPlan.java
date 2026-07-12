package com.example.weightloss.entity;

import com.example.weightloss.dto.EnergyPlanCalculationResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
	name = "energy_plans",
	indexes = {
		@Index(name = "idx_energy_plans_user_status", columnList = "user_id,status"),
		@Index(name = "uk_energy_plans_user_request", columnList = "user_id,client_request_id", unique = true)
	}
)
public class EnergyPlan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private EnergyCalculationMethod calculationMethod;

	@Column(nullable = false, length = 32)
	private String calculationVersion;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private EnergyPlanDeficitMode deficitMode;

	@Column(nullable = false)
	private Integer ageYears;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private FormulaSex formulaSex;

	@Column(nullable = false, precision = 6, scale = 2)
	private BigDecimal heightCm;

	@Column(nullable = false, precision = 6, scale = 2)
	private BigDecimal weightKg;

	@Column(precision = 6, scale = 2)
	private BigDecimal targetWeightKg;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private NonExerciseActivityLevel nonExerciseActivityLevel;

	@Column
	private Integer targetPeriodDays;

	@Column(nullable = false)
	private Integer restingEnergyCalories;

	@Column(nullable = false)
	private Integer baselineExpenditureCalories;

	@Column(nullable = false)
	private Integer dailyDeficitCalories;

	@Column(nullable = false)
	private Integer baseIntakeTargetCalories;

	@Column(nullable = false)
	private LocalDateTime profileUpdatedAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private EnergyPlanStatus status;

	@Column(nullable = false)
	private LocalDate effectiveFrom;

	@Column(nullable = false, length = 160)
	private String clientRequestId;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	public EnergyPlan(
		AppUser user,
		EnergyPlanCalculationResponse calculation,
		LocalDate effectiveFrom,
		String clientRequestId
	) {
		this.user = user;
		this.calculationMethod = calculation.calculationMethod();
		this.calculationVersion = calculation.calculationVersion();
		this.deficitMode = calculation.deficitMode();
		this.ageYears = calculation.ageYears();
		this.formulaSex = calculation.formulaSex();
		this.heightCm = calculation.heightCm();
		this.weightKg = calculation.weightKg();
		this.targetWeightKg = calculation.targetWeightKg();
		this.nonExerciseActivityLevel = calculation.nonExerciseActivityLevel();
		this.targetPeriodDays = calculation.targetPeriodDays();
		this.restingEnergyCalories = calculation.restingEnergyCalories();
		this.baselineExpenditureCalories = calculation.baselineExpenditureCalories();
		this.dailyDeficitCalories = calculation.dailyDeficitCalories();
		this.baseIntakeTargetCalories = calculation.baseIntakeTargetCalories();
		this.profileUpdatedAt = calculation.profileUpdatedAt();
		this.status = EnergyPlanStatus.ACTIVE;
		this.effectiveFrom = effectiveFrom;
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
