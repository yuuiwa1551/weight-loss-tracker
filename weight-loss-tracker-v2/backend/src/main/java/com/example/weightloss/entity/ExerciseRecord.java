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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
	name = "exercise_records",
	indexes = @Index(name = "idx_exercise_records_record_date", columnList = "record_date")
)
public class ExerciseRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private LocalDate recordDate;

	@Column(nullable = false, length = 60)
	private String exerciseType;

	@Column(nullable = false, length = 120)
	private String exerciseName;

	@Column(nullable = false)
	private Integer durationMinutes;

	@Column(nullable = false)
	private Integer caloriesBurned;

	@Column(length = 500)
	private String note;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	public ExerciseRecord(
		LocalDate recordDate,
		String exerciseType,
		String exerciseName,
		Integer durationMinutes,
		Integer caloriesBurned,
		String note
	) {
		this.recordDate = recordDate;
		this.exerciseType = exerciseType;
		this.exerciseName = exerciseName;
		this.durationMinutes = durationMinutes;
		this.caloriesBurned = caloriesBurned;
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
