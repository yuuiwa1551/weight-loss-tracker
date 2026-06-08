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

	public ProfileService(UserProfileRepository userProfileRepository) {
		this.userProfileRepository = userProfileRepository;
	}

	@Transactional(readOnly = true)
	public UserProfileResponse getProfile() {
		return UserProfileResponse.from(getProfileEntity());
	}

	@Transactional
	public UserProfileResponse updateProfile(UpdateProfileRequest request) {
		UserProfile profile = getProfileEntity();
		profile.setNickname(request.nickname());
		profile.setHeightCm(request.heightCm());
		profile.setCurrentWeightKg(request.currentWeightKg());
		profile.setTargetWeightKg(request.targetWeightKg());
		profile.setDailyCalorieGoal(request.dailyCalorieGoal());
		return UserProfileResponse.from(userProfileRepository.save(profile));
	}

	@Transactional(readOnly = true)
	public UserProfile getProfileEntity() {
		return userProfileRepository.findFirstByOrderByIdAsc()
			.orElseThrow(() -> new ResourceNotFoundException("Profile has not been initialized"));
	}
}