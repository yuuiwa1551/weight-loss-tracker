package com.example.weightloss.controller;

import com.example.weightloss.common.ApiResponse;
import com.example.weightloss.dto.CreateExerciseRecordRequest;
import com.example.weightloss.dto.ExerciseRecordResponse;
import com.example.weightloss.service.ExerciseRecordService;
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
@RequestMapping("/api/exercise-records")
public class ExerciseRecordController {

	private final ExerciseRecordService exerciseRecordService;

	public ExerciseRecordController(ExerciseRecordService exerciseRecordService) {
		this.exerciseRecordService = exerciseRecordService;
	}

	@PostMapping
	public ApiResponse<ExerciseRecordResponse> create(@Valid @RequestBody CreateExerciseRecordRequest request) {
		return ApiResponse.ok("Exercise record created", exerciseRecordService.create(request));
	}

	@GetMapping
	public ApiResponse<List<ExerciseRecordResponse>> listByDate(
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
	) {
		return ApiResponse.ok(exerciseRecordService.listByDate(date == null ? LocalDate.now() : date));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable Long id) {
		exerciseRecordService.delete(id);
		return ApiResponse.ok("Exercise record deleted", null);
	}
}