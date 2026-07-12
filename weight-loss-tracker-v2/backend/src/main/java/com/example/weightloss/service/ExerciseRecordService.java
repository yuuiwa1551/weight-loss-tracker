package com.example.weightloss.service;

import com.example.weightloss.dto.CreateExerciseRecordRequest;
import com.example.weightloss.dto.EnergyBudgetResponse;
import com.example.weightloss.dto.ExerciseRecordPreviewResponse;
import com.example.weightloss.dto.ExerciseRecordResponse;
import com.example.weightloss.entity.AppUser;
import com.example.weightloss.entity.ExerciseRecord;
import com.example.weightloss.entity.RecordSource;
import com.example.weightloss.repository.ExerciseRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExerciseRecordService {

	private final ExerciseRecordRepository exerciseRecordRepository;
	private final UserService userService;
	private final ProfileService profileService;
	private final EnergyBudgetService energyBudgetService;
	private final PreviewFingerprintService previewFingerprintService;

	public ExerciseRecordService(
		ExerciseRecordRepository exerciseRecordRepository,
		UserService userService,
		ProfileService profileService,
		EnergyBudgetService energyBudgetService,
		PreviewFingerprintService previewFingerprintService
	) {
		this.exerciseRecordRepository = exerciseRecordRepository;
		this.userService = userService;
		this.profileService = profileService;
		this.energyBudgetService = energyBudgetService;
		this.previewFingerprintService = previewFingerprintService;
	}

	@Transactional(readOnly = true)
	public ExerciseRecordPreviewResponse preview(Long userId, CreateExerciseRecordRequest request) {
		CreateExerciseRecordRequest normalized = normalize(request);
		var profile = profileService.getProfileEntity(userId);
		EnergyBudgetProjection projection = energyBudgetService.project(
			profile,
			normalized.recordDate(),
			0,
			normalized.caloriesBurned()
		);
		String fingerprint = previewFingerprintService.exerciseRecordFingerprint(userId, normalized, projection);
		return previewResponse(normalized, projection.budget(), fingerprint);
	}

	@Transactional
	public ExerciseRecordResponse create(Long userId, CreateExerciseRecordRequest request) {
		CreateExerciseRecordRequest normalized = normalize(request);
		var profile = profileService.getProfileEntityForUpdate(userId);
		var existing = exerciseRecordRepository.findByUserIdAndClientRequestId(userId, normalized.clientRequestId());
		if (existing.isPresent()) {
			return ExerciseRecordResponse.from(
				existing.get(),
				energyBudgetService.project(profile, existing.get().getRecordDate(), 0, 0).budget()
			);
		}
		EnergyBudgetProjection projection = energyBudgetService.project(
			profile,
			normalized.recordDate(),
			0,
			normalized.caloriesBurned()
		);
		String currentFingerprint = previewFingerprintService.exerciseRecordFingerprint(userId, normalized, projection);
		if (!previewFingerprintService.matches(requireFingerprint(normalized.previewFingerprint()), currentFingerprint)) {
			throw new ConflictException("Exercise record preview is stale; preview and confirm again");
		}
		AppUser user = profile.getUser();
		ExerciseRecord record = new ExerciseRecord(
			user,
			normalized.recordDate(),
			normalized.exerciseType(),
			normalized.exerciseName(),
			normalized.durationMinutes(),
			normalized.caloriesBurned(),
			normalized.note(),
			normalized.source(),
			normalized.clientRequestId()
		);
		return ExerciseRecordResponse.from(exerciseRecordRepository.save(record), projection.budget());
	}

	@Transactional(readOnly = true)
	public List<ExerciseRecordResponse> listByDate(Long userId, LocalDate date) {
		userService.getEntity(userId);
		return exerciseRecordRepository.findByUserIdAndRecordDateOrderByCreatedAtAscIdAsc(userId, date).stream()
			.map(ExerciseRecordResponse::from)
			.toList();
	}

	@Transactional
	public void delete(Long userId, Long id) {
		profileService.getProfileEntityForUpdate(userId);
		ExerciseRecord record = exerciseRecordRepository.findByIdAndUserId(id, userId)
			.orElseThrow(() -> new ResourceNotFoundException("Exercise record not found: " + id));
		exerciseRecordRepository.delete(record);
	}

	private String normalize(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private CreateExerciseRecordRequest normalize(CreateExerciseRecordRequest request) {
		return new CreateExerciseRecordRequest(
			request.recordDate(),
			request.exerciseType().trim(),
			request.exerciseName().trim(),
			request.durationMinutes(),
			request.caloriesBurned(),
			normalize(request.note()),
			request.source() == null ? RecordSource.WEB : request.source(),
			normalize(request.clientRequestId()),
			normalize(request.previewFingerprint())
		);
	}

	private ExerciseRecordPreviewResponse previewResponse(
		CreateExerciseRecordRequest request,
		EnergyBudgetResponse budget,
		String fingerprint
	) {
		return new ExerciseRecordPreviewResponse(
			request.recordDate(),
			request.exerciseType(),
			request.exerciseName(),
			request.durationMinutes(),
			request.caloriesBurned(),
			request.note(),
			request.source(),
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
