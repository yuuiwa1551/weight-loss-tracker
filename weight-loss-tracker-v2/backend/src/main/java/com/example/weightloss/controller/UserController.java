package com.example.weightloss.controller;

import com.example.weightloss.common.ApiResponse;
import com.example.weightloss.dto.ResolveUserRequest;
import com.example.weightloss.dto.UserResponse;
import com.example.weightloss.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/resolve")
	public ApiResponse<UserResponse> resolve(@Valid @RequestBody ResolveUserRequest request) {
		return ApiResponse.ok("User resolved", userService.resolve(request));
	}

	@GetMapping
	public ApiResponse<List<UserResponse>> list() {
		return ApiResponse.ok(userService.list());
	}
}
