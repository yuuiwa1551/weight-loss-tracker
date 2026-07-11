package com.example.weightloss.repository;

import com.example.weightloss.entity.WeightRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeightRecordRepository extends JpaRepository<WeightRecord, Long> {
	List<WeightRecord> findByUserIdAndRecordDateBetweenOrderByRecordDateAscCreatedAtAscIdAsc(
		Long userId,
		LocalDate startDate,
		LocalDate endDate
	);

	long countByUserIdAndRecordDate(Long userId, LocalDate recordDate);

	Optional<WeightRecord> findByIdAndUserId(Long id, Long userId);

	Optional<WeightRecord> findByUserIdAndClientRequestId(Long userId, String clientRequestId);

	Optional<WeightRecord> findFirstByUserIdOrderByRecordDateDescCreatedAtDescIdDesc(Long userId);
}
