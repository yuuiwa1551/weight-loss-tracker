package com.example.weightloss.service;

import com.example.weightloss.dto.UpdateProfileRequest;
import com.example.weightloss.dto.UserProfileResponse;
import com.example.weightloss.entity.UserProfile;
import com.example.weightloss.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

	private final UserProfileRepository userProfileRepository;
	private final UserService userService;

	public ProfileService(UserProfileRepository userProfileRepository, UserService userService) {
		this.userProfileRepository = userProfileRepository;
		this.userService = userService;
	}

	@Transactional(readOnly = true)
	public UserProfileResponse getProfile(Long userId) {
		return UserProfileResponse.from(getProfileEntity(userId));
	}

	@Transactional
	public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
		UserProfile profile = getProfileEntity(userId);
		profile.setNickname(normalize(request.nickname()));
		profile.setHeightCm(request.heightCm());
		profile.setCurrentWeightKg(request.currentWeightKg());
		profile.setTargetWeightKg(request.targetWeightKg());
		profile.setDailyCalorieGoal(request.dailyCalorieGoal());
		return UserProfileResponse.from(userProfileRepository.save(profile));
	}

	@Transactional(readOnly = true)
	public UserProfile getProfileEntity(Long userId) {
		userService.getEntity(userId);
		return userProfileRepository.findByUserId(userId)
			.orElseThrow(() -> new ResourceNotFoundException("Profile not found for user: " + userId));
	}

	@Transactional
	public void updateCurrentWeight(Long userId, java.math.BigDecimal currentWeightKg) {
		UserProfile profile = getProfileEntity(userId);
		profile.setCurrentWeightKg(currentWeightKg);
		userProfileRepository.save(profile);
	}

	private String normalize(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
