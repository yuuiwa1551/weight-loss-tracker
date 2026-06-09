package com.example.weightloss.repository;

import com.example.weightloss.entity.WeightRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WeightRecordRepository extends JpaRepository<WeightRecord, Long> {
	List<WeightRecord> findByRecordDateBetweenOrderByRecordDateAscCreatedAtAscIdAsc(LocalDate startDate, LocalDate endDate);

	long countByRecordDate(LocalDate recordDate);
}