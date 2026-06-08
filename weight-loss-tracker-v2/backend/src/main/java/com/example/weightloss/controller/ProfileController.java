package com.example.weightloss.controller;

import com.example.weightloss.common.ApiResponse;
import com.example.weightloss.dto.UpdateProfileRequest;
import com.example.weightloss.dto.UserProfileResponse;
import com.example.weightloss.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

	private final ProfileService profileService;

	public ProfileController(ProfileService profileService) {
		this.profileService = profileService;
	}

	@GetMapping
	public ApiResponse<UserProfileResponse> getProfile() {
		return ApiResponse.ok(profileService.getProfile());
	}

	@PutMapping
	public ApiResponse<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
		return ApiResponse.ok("Profile updated", profileService.updateProfile(request));
	}
}