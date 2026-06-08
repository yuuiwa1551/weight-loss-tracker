package com.example.weightloss.repository;

import com.example.weightloss.entity.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long> {
	List<ExerciseRecord> findByRecordDateOrderByCreatedAtAscIdAsc(LocalDate recordDate);

	List<ExerciseRecord> findByRecordDateBetweenOrderByRecordDateAscCreatedAtAscIdAsc(LocalDate startDate, LocalDate endDate);

	long countByRecordDate(LocalDate recordDate);
}