package com.example.weightloss.repository;

import com.example.weightloss.entity.EnergyPlan;
import com.example.weightloss.entity.EnergyPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnergyPlanRepository extends JpaRepository<EnergyPlan, Long> {
	Optional<EnergyPlan> findFirstByUserIdAndStatusOrderByCreatedAtDescIdDesc(Long userId, EnergyPlanStatus status);

	Optional<EnergyPlan> findByUserIdAndClientRequestId(Long userId, String clientRequestId);

	List<EnergyPlan> findByUserIdAndStatus(Long userId, EnergyPlanStatus status);
}
