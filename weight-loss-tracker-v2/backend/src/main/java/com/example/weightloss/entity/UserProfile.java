package com.example.weightloss.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
	name = "user_profiles",
	uniqueConstraints = @UniqueConstraint(name = "uk_user_profiles_user_id", columnNames = "user_id")
)
public class UserProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Column(length = 60)
	private String nickname;

	@Column(precision = 6, scale = 2)
	private BigDecimal heightCm;

	@Column(precision = 6, scale = 2)
	private BigDecimal currentWeightKg;

	@Column(precision = 6, scale = 2)
	private BigDecimal targetWeightKg;

	@Column
	private Integer dailyCalorieGoal;

	@Column
	private Integer ageYears;

	@Enumerated(EnumType.STRING)
	@Column(length = 16)
	private FormulaSex formulaSex;

	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private NonExerciseActivityLevel nonExerciseActivityLevel;

	@Enumerated(EnumType.STRING)
	@Column(length = 16)
	private CalorieGoalMode calorieGoalMode;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	public UserProfile(AppUser user, String nickname) {
		this.user = user;
		this.nickname = nickname;
	}

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
		normalizeCalorieGoalMode();
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = LocalDateTime.now();
		normalizeCalorieGoalMode();
	}

	public CalorieGoalMode effectiveCalorieGoalMode() {
		if (calorieGoalMode == CalorieGoalMode.AUTO) {
			return CalorieGoalMode.AUTO;
		}
		return dailyCalorieGoal == null ? CalorieGoalMode.UNSET : CalorieGoalMode.MANUAL;
	}

	private void normalizeCalorieGoalMode() {
		if (calorieGoalMode == null) {
			calorieGoalMode = dailyCalorieGoal == null ? CalorieGoalMode.UNSET : CalorieGoalMode.MANUAL;
		}
	}
}
