package com.example.weightloss.controller;

import com.example.weightloss.common.ApiResponse;
import com.example.weightloss.dto.CreateWeightRecordRequest;
import com.example.weightloss.dto.WeightRecordResponse;
import com.example.weightloss.service.WeightRecordService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/users/{userId}/weight-records")
public class WeightRecordController {

	private final WeightRecordService weightRecordService;

	public WeightRecordController(WeightRecordService weightRecordService) {
		this.weightRecordService = weightRecordService;
	}

	@PostMapping
	public ApiResponse<WeightRecordResponse> create(
		@PathVariable Long userId,
		@Valid @RequestBody CreateWeightRecordRequest request
	) {
		return ApiResponse.ok("Weight record created", weightRecordService.create(userId, request));
	}

	@GetMapping("/recent")
	public ApiResponse<List<WeightRecordResponse>> listRecent(
		@PathVariable Long userId,
		@RequestParam(defaultValue = "30") @Min(7) @Max(365) int days
	) {
		return ApiResponse.ok(weightRecordService.listRecent(userId, days));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable Long userId, @PathVariable Long id) {
		weightRecordService.delete(userId, id);
		return ApiResponse.ok("Weight record deleted", null);
	}
}
