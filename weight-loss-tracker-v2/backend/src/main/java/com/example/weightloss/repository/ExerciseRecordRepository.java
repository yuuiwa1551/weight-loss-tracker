package com.example.weightloss.repository;

import com.example.weightloss.entity.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long> {
	List<ExerciseRecord> findByUserIdAndRecordDateOrderByCreatedAtAscIdAsc(Long userId, LocalDate recordDate);

	List<ExerciseRecord> findByUserIdAndRecordDateBetweenOrderByRecordDateAscCreatedAtAscIdAsc(
		Long userId,
		LocalDate startDate,
		LocalDate endDate
	);

	long countByUserIdAndRecordDate(Long userId, LocalDate recordDate);

	Optional<ExerciseRecord> findByIdAndUserId(Long id, Long userId);

	Optional<ExerciseRecord> findByUserIdAndClientRequestId(Long userId, String clientRequestId);
}
