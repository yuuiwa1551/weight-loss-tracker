package com.example.weightloss.controller;

import com.example.weightloss.common.ApiResponse;
import com.example.weightloss.dto.CreateFoodRecordRequest;
import com.example.weightloss.dto.FoodRecordPreviewResponse;
import com.example.weightloss.dto.FoodRecordResponse;
import com.example.weightloss.service.FoodRecordService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/food-records")
public class FoodRecordController {

	private final FoodRecordService foodRecordService;

	public FoodRecordController(FoodRecordService foodRecordService) {
		this.foodRecordService = foodRecordService;
	}

	@PostMapping
	public ApiResponse<FoodRecordResponse> create(
		@PathVariable Long userId,
		@Valid @RequestBody CreateFoodRecordRequest request
	) {
		return ApiResponse.ok("Food record created", foodRecordService.create(userId, request));
	}

	@PostMapping("/preview")
	public ApiResponse<FoodRecordPreviewResponse> preview(
		@PathVariable Long userId,
		@Valid @RequestBody CreateFoodRecordRequest request
	) {
		return ApiResponse.ok(foodRecordService.preview(userId, request));
	}

	@GetMapping
	public ApiResponse<List<FoodRecordResponse>> listByDate(
		@PathVariable Long userId,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
	) {
		return ApiResponse.ok(foodRecordService.listByDate(userId, date == null ? LocalDate.now() : date));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable Long userId, @PathVariable Long id) {
		foodRecordService.delete(userId, id);
		return ApiResponse.ok("Food record deleted", null);
	}
}
