package com.example.weightloss.repository;

import com.example.weightloss.entity.FoodRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FoodRecordRepository extends JpaRepository<FoodRecord, Long> {
	List<FoodRecord> findByRecordDateOrderByCreatedAtAscIdAsc(LocalDate recordDate);

	List<FoodRecord> findByRecordDateBetweenOrderByRecordDateAscCreatedAtAscIdAsc(LocalDate startDate, LocalDate endDate);

	long countByRecordDate(LocalDate recordDate);
}