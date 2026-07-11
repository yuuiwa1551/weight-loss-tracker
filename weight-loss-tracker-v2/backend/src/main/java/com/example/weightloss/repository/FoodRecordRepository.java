package com.example.weightloss.repository;

import com.example.weightloss.entity.FoodRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FoodRecordRepository extends JpaRepository<FoodRecord, Long> {
	List<FoodRecord> findByUserIdAndRecordDateOrderByCreatedAtAscIdAsc(Long userId, LocalDate recordDate);

	List<FoodRecord> findByUserIdAndRecordDateBetweenOrderByRecordDateAscCreatedAtAscIdAsc(
		Long userId,
		LocalDate startDate,
		LocalDate endDate
	);

	long countByUserIdAndRecordDate(Long userId, LocalDate recordDate);

	Optional<FoodRecord> findByIdAndUserId(Long id, Long userId);

	Optional<FoodRecord> findByUserIdAndClientRequestId(Long userId, String clientRequestId);
}
