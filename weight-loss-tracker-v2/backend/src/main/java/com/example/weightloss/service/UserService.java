package com.example.weightloss.service;

import com.example.weightloss.dto.ResolveUserRequest;
import com.example.weightloss.dto.UserResponse;
import com.example.weightloss.entity.AppUser;
import com.example.weightloss.entity.UserProfile;
import com.example.weightloss.repository.AppUserRepository;
import com.example.weightloss.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

	private final AppUserRepository appUserRepository;
	private final UserProfileRepository userProfileRepository;

	public UserService(AppUserRepository appUserRepository, UserProfileRepository userProfileRepository) {
		this.appUserRepository = appUserRepository;
		this.userProfileRepository = userProfileRepository;
	}

	@Transactional
	public UserResponse resolve(ResolveUserRequest request) {
		String platform = request.platform().trim().toLowerCase();
		String username = request.username().trim();
		String displayName = normalize(request.displayName());

		AppUser user = appUserRepository.findByPlatformAndUsername(platform, username)
			.orElseGet(() -> appUserRepository.save(new AppUser(platform, username, displayName)));
		if (displayName != null && !displayName.equals(user.getDisplayName())) {
			user.setDisplayName(displayName);
			appUserRepository.save(user);
		}
		userProfileRepository.findByUserId(user.getId())
			.orElseGet(() -> userProfileRepository.save(new UserProfile(user, displayName)));
		return UserResponse.from(user);
	}

	@Transactional(readOnly = true)
	public List<UserResponse> list() {
		return appUserRepository.findAllByOrderByDisplayNameAscUsernameAsc().stream()
			.map(UserResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public AppUser getEntity(Long userId) {
		return appUserRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
	}

	private String normalize(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
