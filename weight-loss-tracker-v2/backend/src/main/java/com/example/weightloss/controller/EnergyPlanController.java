package com.example.weightloss.controller;

import com.example.weightloss.common.ApiResponse;
import com.example.weightloss.dto.ConfirmEnergyPlanRequest;
import com.example.weightloss.dto.EnergyPlanPreviewRequest;
import com.example.weightloss.dto.EnergyPlanPreviewResponse;
import com.example.weightloss.dto.EnergyPlanResponse;
import com.example.weightloss.service.EnergyPlanService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/energy-plans")
public class EnergyPlanController {

	private final EnergyPlanService energyPlanService;

	public EnergyPlanController(EnergyPlanService energyPlanService) {
		this.energyPlanService = energyPlanService;
	}

	@PostMapping("/preview")
	public ApiResponse<EnergyPlanPreviewResponse> preview(
		@PathVariable Long userId,
		@Valid @RequestBody EnergyPlanPreviewRequest request
	) {
		return ApiResponse.ok(energyPlanService.preview(userId, request));
	}

	@PostMapping
	public ApiResponse<EnergyPlanResponse> confirm(
		@PathVariable Long userId,
		@Valid @RequestBody ConfirmEnergyPlanRequest request
	) {
		return ApiResponse.ok("Energy plan confirmed", energyPlanService.confirm(userId, request));
	}

	@GetMapping("/active")
	public ApiResponse<EnergyPlanResponse> getActive(@PathVariable Long userId) {
		return ApiResponse.ok(energyPlanService.getActive(userId));
	}
}
