package com.example.weightloss.service;

import com.example.weightloss.dto.CreateWeightRecordRequest;
import com.example.weightloss.dto.WeightRecordResponse;
import com.example.weightloss.entity.WeightRecord;
import com.example.weightloss.repository.WeightRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class WeightRecordService {

	private final WeightRecordRepository weightRecordRepository;

	public WeightRecordService(WeightRecordRepository weightRecordRepository) {
		this.weightRecordRepository = weightRecordRepository;
	}

	@Transactional
	public WeightRecordResponse create(CreateWeightRecordRequest request) {
		WeightRecord record = new WeightRecord(
			request.recordDate(),
			request.weightKg(),
			request.bodyFatPercentage(),
			normalizeNote(request.note())
		);
		return WeightRecordResponse.from(weightRecordRepository.save(record));
	}

	@Transactional(readOnly = true)
	public List<WeightRecordResponse> listRecent(int days) {
		LocalDate endDate = LocalDate.now();
		LocalDate startDate = endDate.minusDays(days - 1L);
		return weightRecordRepository.findByRecordDateBetweenOrderByRecordDateAscCreatedAtAscIdAsc(startDate, endDate).stream()
			.map(WeightRecordResponse::from)
			.toList();
	}

	@Transactional
	public void delete(Long id) {
		if (!weightRecordRepository.existsById(id)) {
			throw new ResourceNotFoundException("Weight record not found: " + id);
		}
		weightRecordRepository.deleteById(id);
	}

	private String normalizeNote(String note) {
		return note == null || note.isBlank() ? null : note.trim();
	}
}
