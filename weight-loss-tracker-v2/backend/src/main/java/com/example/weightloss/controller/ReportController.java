package com.example.weightloss.controller;

import com.example.weightloss.common.ApiResponse;
import com.example.weightloss.dto.PeriodReportResponse;
import com.example.weightloss.service.SummaryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/reports")
public class ReportController {

	private final SummaryService summaryService;

	public ReportController(SummaryService summaryService) {
		this.summaryService = summaryService;
	}

	@GetMapping("/overview")
	public ApiResponse<PeriodReportResponse> getOverview(@RequestParam(defaultValue = "7") @Min(7) @Max(365) int days) {
		return ApiResponse.ok(summaryService.getPeriodReport(days));
	}
}