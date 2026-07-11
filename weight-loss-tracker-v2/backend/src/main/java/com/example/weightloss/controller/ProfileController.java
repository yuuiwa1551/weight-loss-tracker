package com.example.weightloss.controller;

import com.example.weightloss.common.ApiResponse;
import com.example.weightloss.dto.UpdateProfileRequest;
import com.example.weightloss.dto.UserProfileResponse;
import com.example.weightloss.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/profile")
public class ProfileController {

	private final ProfileService profileService;

	public ProfileController(ProfileService profileService) {
		this.profileService = profileService;
	}

	@GetMapping
	public ApiResponse<UserProfileResponse> getProfile(@PathVariable Long userId) {
		return ApiResponse.ok(profileService.getProfile(userId));
	}

	@PutMapping
	public ApiResponse<UserProfileResponse> updateProfile(
		@PathVariable Long userId,
		@Valid @RequestBody UpdateProfileRequest request
	) {
		return ApiResponse.ok("Profile updated", profileService.updateProfile(userId, request));
	}
}
