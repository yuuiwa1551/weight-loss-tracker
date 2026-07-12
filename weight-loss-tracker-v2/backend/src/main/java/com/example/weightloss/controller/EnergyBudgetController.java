package com.example.weightloss.controller;

import com.example.weightloss.common.ApiResponse;
import com.example.weightloss.dto.EnergyBudgetResponse;
import com.example.weightloss.service.EnergyBudgetService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/users/{userId}/energy-budgets")
public class EnergyBudgetController {

	private final EnergyBudgetService energyBudgetService;

	public EnergyBudgetController(EnergyBudgetService energyBudgetService) {
		this.energyBudgetService = energyBudgetService;
	}

	@GetMapping("/daily")
	public ApiResponse<EnergyBudgetResponse> getDailyBudget(
		@PathVariable Long userId,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
	) {
		return ApiResponse.ok(energyBudgetService.getDailyBudget(userId, date == null ? LocalDate.now() : date));
	}
}
