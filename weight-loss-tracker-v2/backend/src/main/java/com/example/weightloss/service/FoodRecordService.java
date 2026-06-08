package com.example.weightloss.service;

import com.example.weightloss.dto.CreateFoodRecordRequest;
import com.example.weightloss.dto.FoodRecordResponse;
import com.example.weightloss.entity.FoodRecord;
import com.example.weightloss.repository.FoodRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class FoodRecordService {

	private final FoodRecordRepository foodRecordRepository;

	public FoodRecordService(FoodRecordRepository foodRecordRepository) {
		this.foodRecordRepository = foodRecordRepository;
	}

	@Transactional
	public FoodRecordResponse create(CreateFoodRecordRequest request) {
		FoodRecord record = new FoodRecord(
			request.recordDate(),
			request.mealType(),
			request.foodName().trim(),
			request.calories(),
			zeroIfNull(request.protein()),
			zeroIfNull(request.fat()),
			zeroIfNull(request.carbohydrate()),
			normalizeNote(request.note())
		);
		return FoodRecordResponse.from(foodRecordRepository.save(record));
	}

	@Transactional(readOnly = true)
	public List<FoodRecordResponse> listByDate(LocalDate date) {
		return foodRecordRepository.findByRecordDateOrderByCreatedAtAscIdAsc(date).stream()
			.map(FoodRecordResponse::from)
			.toList();
	}

	@Transactional
	public void delete(Long id) {
		if (!foodRecordRepository.existsById(id)) {
			throw new ResourceNotFoundException("Food record not found: " + id);
		}
		foodRecordRepository.deleteById(id);
	}

	private BigDecimal zeroIfNull(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private String normalizeNote(String note) {
		return note == null || note.isBlank() ? null : note.trim();
	}
}