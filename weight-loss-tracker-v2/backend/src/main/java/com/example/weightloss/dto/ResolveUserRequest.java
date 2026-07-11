package com.example.weightloss.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResolveUserRequest(
	@NotBlank @Pattern(regexp = "aiocqhttp", message = "must be aiocqhttp") String platform,
	@NotBlank @Pattern(regexp = "[0-9]+", message = "must be a QQ number") @Size(max = 40) String username,
	@Size(max = 100) String displayName
) {
}
