package com.example.weightloss.service;

import com.example.weightloss.dto.ConfirmEnergyPlanRequest;
import com.example.weightloss.dto.EnergyPlanCalculationResponse;
import com.example.weightloss.dto.EnergyPlanPreviewRequest;
import com.example.weightloss.dto.EnergyPlanPreviewResponse;
import com.example.weightloss.dto.EnergyPlanResponse;
import com.example.weightloss.entity.CalorieGoalMode;
import com.example.weightloss.entity.EnergyPlan;
import com.example.weightloss.entity.EnergyPlanStatus;
import com.example.weightloss.entity.UserProfile;
import com.example.weightloss.repository.EnergyPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class EnergyPlanService {

	private final EnergyPlanRepository energyPlanRepository;
	private final ProfileService profileService;
	private final EnergyCalculationService energyCalculationService;
	private final PreviewFingerprintService previewFingerprintService;

	public EnergyPlanService(
		EnergyPlanRepository energyPlanRepository,
		ProfileService profileService,
		EnergyCalculationService energyCalculationService,
		PreviewFingerprintService previewFingerprintService
	) {
		this.energyPlanRepository = energyPlanRepository;
		this.profileService = profileService;
		this.energyCalculationService = energyCalculationService;
		this.previewFingerprintService = previewFingerprintService;
	}

	@Transactional(readOnly = true)
	public EnergyPlanPreviewResponse preview(Long userId, EnergyPlanPreviewRequest request) {
		UserProfile profile = profileService.getProfileEntity(userId);
		EnergyPlanCalculationResponse calculation = energyCalculationService.calculate(profile, request);
		EnergyPlan activePlan = findActiveEntity(userId).orElse(null);
		String fingerprint = previewFingerprintService.energyPlanFingerprint(userId, request, calculation, activePlan);
		return new EnergyPlanPreviewResponse(calculation, fingerprint);
	}

	@Transactional
	public EnergyPlanResponse confirm(Long userId, ConfirmEnergyPlanRequest request) {
		String clientRequestId = request.clientRequestId().trim();
		var existing = energyPlanRepository.findByUserIdAndClientRequestId(userId, clientRequestId);
		if (existing.isPresent()) return EnergyPlanResponse.from(existing.get());

		UserProfile profile = profileService.getProfileEntityForUpdate(userId);
		EnergyPlanCalculationResponse calculation = energyCalculationService.calculate(profile, request.calculation());
		EnergyPlan activePlan = findActiveEntity(userId).orElse(null);
		String currentFingerprint = previewFingerprintService.energyPlanFingerprint(
			userId,
			request.calculation(),
			calculation,
			activePlan
		);
		if (!previewFingerprintService.matches(request.previewFingerprint(), currentFingerprint)) {
			throw new ConflictException("Energy plan preview is stale; preview and confirm again");
		}

		energyPlanRepository.findByUserIdAndStatus(userId, EnergyPlanStatus.ACTIVE).forEach(plan ->
			plan.setStatus(EnergyPlanStatus.SUPERSEDED)
		);
		EnergyPlan saved = energyPlanRepository.save(new EnergyPlan(
			profile.getUser(),
			calculation,
			LocalDate.now(),
			clientRequestId
		));
		profile.setCalorieGoalMode(CalorieGoalMode.AUTO);
		return EnergyPlanResponse.from(saved);
	}

	@Transactional(readOnly = true)
	public EnergyPlanResponse getActive(Long userId) {
		profileService.getProfileEntity(userId);
		return findActiveEntity(userId)
			.map(EnergyPlanResponse::from)
			.orElseThrow(() -> new ResourceNotFoundException("Active energy plan not found for user: " + userId));
	}

	@Transactional(readOnly = true)
	public java.util.Optional<EnergyPlan> findActiveEntity(Long userId) {
		return energyPlanRepository.findFirstByUserIdAndStatusOrderByCreatedAtDescIdDesc(
			userId,
			EnergyPlanStatus.ACTIVE
		);
	}
}
