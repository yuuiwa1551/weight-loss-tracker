package com.example.weightloss.dto;

import com.example.weightloss.entity.AppUser;

import java.time.LocalDateTime;

public record UserResponse(
	Long id,
	String platform,
	String username,
	String displayName,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static UserResponse from(AppUser user) {
		return new UserResponse(
			user.getId(),
			user.getPlatform(),
			user.getUsername(),
			user.getDisplayName(),
			user.getCreatedAt(),
			user.getUpdatedAt()
		);
	}
}
