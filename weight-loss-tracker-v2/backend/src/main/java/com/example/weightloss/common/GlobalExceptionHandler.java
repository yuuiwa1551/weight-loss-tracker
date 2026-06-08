package com.example.weightloss.common;

import com.example.weightloss.service.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException exception) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
			errors.put(fieldError.getField(), fieldError.getDefaultMessage());
		}
		return ResponseEntity.badRequest().body(ApiResponse.error("Validation failed", errors));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(ConstraintViolationException exception) {
		Map<String, String> errors = new LinkedHashMap<>();
		exception.getConstraintViolations().forEach(violation ->
			errors.put(violation.getPropertyPath().toString(), violation.getMessage())
		);
		return ResponseEntity.badRequest().body(ApiResponse.error("Validation failed", errors));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleUnreadableMessage() {
		return ResponseEntity.badRequest().body(ApiResponse.error("Request body is invalid", null));
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(exception.getMessage(), null));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException exception) {
		return ResponseEntity.badRequest().body(ApiResponse.error(exception.getMessage(), null));
	}
}