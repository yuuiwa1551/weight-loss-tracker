package com.example.weightloss.service;

import com.example.weightloss.dto.CreateFoodRecordRequest;
import com.example.weightloss.dto.EnergyBudgetResponse;
import com.example.weightloss.dto.FoodRecordPreviewResponse;
import com.example.weightloss.dto.FoodRecordResponse;
import com.example.weightloss.entity.AppUser;
import com.example.weightloss.entity.FoodRecord;
import com.example.weightloss.entity.NutritionSource;
import com.example.weightloss.entity.RecordSource;
import com.example.weightloss.repository.FoodRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class FoodRecordService {

	private final FoodRecordRepository foodRecordRepository;
	private final UserService userService;
	private final ProfileService profileService;
	private final EnergyBudgetService energyBudgetService;
	private final PreviewFingerprintService previewFingerprintService;

	public FoodRecordService(
		FoodRecordRepository foodRecordRepository,
		UserService userService,
		ProfileService profileService,
		EnergyBudgetService energyBudgetService,
		PreviewFingerprintService previewFingerprintService
	) {
		this.foodRecordRepository = foodRecordRepository;
		this.userService = userService;
		this.profileService = profileService;
		this.energyBudgetService = energyBudgetService;
		this.previewFingerprintService = previewFingerprintService;
	}

	@Transactional(readOnly = true)
	public FoodRecordPreviewResponse preview(Long userId, CreateFoodRecordRequest request) {
		CreateFoodRecordRequest normalized = normalize(request);
		var profile = profileService.getProfileEntity(userId);
		EnergyBudgetProjection projection = energyBudgetService.project(
			profile,
			normalized.recordDate(),
			normalized.calories(),
			0
		);
		String fingerprint = previewFingerprintService.foodRecordFingerprint(userId, normalized, projection);
		return previewResponse(normalized, projection.budget(), fingerprint);
	}

	@Transactional
	public FoodRecordResponse create(Long userId, CreateFoodRecordRequest request) {
		CreateFoodRecordRequest normalized = normalize(request);
		var profile = profileService.getProfileEntityForUpdate(userId);
		var existing = foodRecordRepository.findByUserIdAndClientRequestId(userId, normalized.clientRequestId());
		if (existing.isPresent()) {
			return FoodRecordResponse.from(
				existing.get(),
				energyBudgetService.project(profile, existing.get().getRecordDate(), 0, 0).budget()
			);
		}
		EnergyBudgetProjection projection = energyBudgetService.project(
			profile,
			normalized.recordDate(),
			normalized.calories(),
			0
		);
		String currentFingerprint = previewFingerprintService.foodRecordFingerprint(userId, normalized, projection);
		if (!previewFingerprintService.matches(requireFingerprint(normalized.previewFingerprint()), currentFingerprint)) {
			throw new ConflictException("Food record preview is stale; preview and confirm again");
		}
		AppUser user = profile.getUser();
		FoodRecord record = new FoodRecord(
			user,
			normalized.recordDate(),
			normalized.mealType(),
			normalized.foodName(),
			normalized.calories(),
			normalized.protein(),
			normalized.fat(),
			normalized.carbohydrate(),
			normalized.note(),
			normalized.source(),
			normalized.clientRequestId(),
			normalized.nutritionSource(),
			normalized.estimationNote()
		);
		return FoodRecordResponse.from(foodRecordRepository.save(record), projection.budget());
	}

	@Transactional(readOnly = true)
	public List<FoodRecordResponse> listByDate(Long userId, LocalDate date) {
		userService.getEntity(userId);
		return foodRecordRepository.findByUserIdAndRecordDateOrderByCreatedAtAscIdAsc(userId, date).stream()
			.map(FoodRecordResponse::from)
			.toList();
	}

	@Transactional
	public void delete(Long userId, Long id) {
		profileService.getProfileEntityForUpdate(userId);
		FoodRecord record = foodRecordRepository.findByIdAndUserId(id, userId)
			.orElseThrow(() -> new ResourceNotFoundException("Food record not found: " + id));
		foodRecordRepository.delete(record);
	}

	private BigDecimal zeroIfNull(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private String normalize(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private CreateFoodRecordRequest normalize(CreateFoodRecordRequest request) {
		return new CreateFoodRecordRequest(
			request.recordDate(),
			request.mealType(),
			request.foodName().trim(),
			request.calories(),
			zeroIfNull(request.protein()),
			zeroIfNull(request.fat()),
			zeroIfNull(request.carbohydrate()),
			normalize(request.note()),
			request.source() == null ? RecordSource.WEB : request.source(),
			normalize(request.clientRequestId()),
			request.nutritionSource() == null ? NutritionSource.USER_PROVIDED : request.nutritionSource(),
			normalize(request.estimationNote()),
			normalize(request.previewFingerprint())
		);
	}

	private FoodRecordPreviewResponse previewResponse(
		CreateFoodRecordRequest request,
		EnergyBudgetResponse budget,
		String fingerprint
	) {
		return new FoodRecordPreviewResponse(
			request.recordDate(),
			request.mealType(),
			request.foodName(),
			request.calories(),
			request.protein(),
			request.fat(),
			request.carbohydrate(),
			request.note(),
			request.source(),
			request.nutritionSource(),
			request.estimationNote(),
			budget,
			fingerprint
		);
	}

	private String requireFingerprint(String fingerprint) {
		if (fingerprint == null) {
			throw new IllegalArgumentException("previewFingerprint is required");
		}
		return fingerprint;
	}
}
