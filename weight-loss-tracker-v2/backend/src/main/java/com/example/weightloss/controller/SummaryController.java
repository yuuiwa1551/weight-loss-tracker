package com.example.weightloss.controller;

import com.example.weightloss.common.ApiResponse;
import com.example.weightloss.dto.DailySummaryResponse;
import com.example.weightloss.dto.RecentSummaryResponse;
import com.example.weightloss.service.SummaryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/summaries")
public class SummaryController {

	private final SummaryService summaryService;

	public SummaryController(SummaryService summaryService) {
		this.summaryService = summaryService;
	}

	@GetMapping("/daily")
	public ApiResponse<DailySummaryResponse> getDailySummary(
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
	) {
		return ApiResponse.ok(summaryService.getDailySummary(date == null ? LocalDate.now() : date));
	}

	@GetMapping("/recent")
	public ApiResponse<List<RecentSummaryResponse>> getRecentSummaries(
		@RequestParam(defaultValue = "7") @Min(1) @Max(90) int days
	) {
		return ApiResponse.ok(summaryService.getRecentSummaries(days));
	}
}