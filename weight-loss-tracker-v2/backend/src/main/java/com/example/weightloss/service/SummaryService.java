package com.example.weightloss.service;

import com.example.weightloss.dto.DailySummaryResponse;
import com.example.weightloss.dto.ExerciseRecordResponse;
import com.example.weightloss.dto.FoodRecordResponse;
import com.example.weightloss.dto.RecentSummaryResponse;
import com.example.weightloss.entity.ExerciseRecord;
import com.example.weightloss.entity.FoodRecord;
import com.example.weightloss.entity.GoalStatus;
import com.example.weightloss.entity.UserProfile;
import com.example.weightloss.repository.ExerciseRecordRepository;
import com.example.weightloss.repository.FoodRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class SummaryService {

	private final ProfileService profileService;
	private final FoodRecordRepository foodRecordRepository;
	private final ExerciseRecordRepository exerciseRecordRepository;

	public SummaryService(
		ProfileService profileService,
		FoodRecordRepository foodRecordRepository,
		ExerciseRecordRepository exerciseRecordRepository
	) {
		this.profileService = profileService;
		this.foodRecordRepository = foodRecordRepository;
		this.exerciseRecordRepository = exerciseRecordRepository;
	}

	@Transactional(readOnly = true)
	public DailySummaryResponse getDailySummary(LocalDate date) {
		UserProfile profile = profileService.getProfileEntity();
		List<FoodRecord> foodRecords = foodRecordRepository.findByRecordDateOrderByCreatedAtAscIdAsc(date);
		List<ExerciseRecord> exerciseRecords = exerciseRecordRepository.findByRecordDateOrderByCreatedAtAscIdAsc(date);

		return buildDailySummary(date, profile.getDailyCalorieGoal(), foodRecords, exerciseRecords);
	}

	@Transactional(readOnly = true)
	public List<RecentSummaryResponse> getRecentSummaries(int days) {
		LocalDate endDate = LocalDate.now();
		LocalDate startDate = endDate.minusDays(days - 1L);
		UserProfile profile = profileService.getProfileEntity();
		List<FoodRecord> foodRecords = foodRecordRepository.findByRecordDateBetweenOrderByRecordDateAscCreatedAtAscIdAsc(startDate, endDate);
		List<ExerciseRecord> exerciseRecords = exerciseRecordRepository.findByRecordDateBetweenOrderByRecordDateAscCreatedAtAscIdAsc(startDate, endDate);

		return startDate.datesUntil(endDate.plusDays(1))
			.map(date -> buildDailySummary(
				date,
				profile.getDailyCalorieGoal(),
				foodRecords.stream().filter(record -> record.getRecordDate().equals(date)).toList(),
				exerciseRecords.stream().filter(record -> record.getRecordDate().equals(date)).toList()
			))
			.map(RecentSummaryResponse::fromDaily)
			.toList();
	}

	private DailySummaryResponse buildDailySummary(
		LocalDate date,
		Integer dailyCalorieGoal,
		List<FoodRecord> foodRecords,
		List<ExerciseRecord> exerciseRecords
	) {
		int totalCaloriesConsumed = foodRecords.stream().mapToInt(FoodRecord::getCalories).sum();
		int totalCaloriesBurned = exerciseRecords.stream().mapToInt(ExerciseRecord::getCaloriesBurned).sum();
		int netCalories = totalCaloriesConsumed - totalCaloriesBurned;
		int calorieDifference = dailyCalorieGoal - netCalories;

		return new DailySummaryResponse(
			date,
			totalCaloriesConsumed,
			totalCaloriesBurned,
			netCalories,
			dailyCalorieGoal,
			calorieDifference,
			calculateGoalStatus(netCalories, dailyCalorieGoal),
			sum(foodRecords.stream().map(FoodRecord::getProtein).toList()),
			sum(foodRecords.stream().map(FoodRecord::getFat).toList()),
			sum(foodRecords.stream().map(FoodRecord::getCarbohydrate).toList()),
			foodRecords.stream().map(FoodRecordResponse::from).toList(),
			exerciseRecords.stream().map(ExerciseRecordResponse::from).toList()
		);
	}

	private GoalStatus calculateGoalStatus(int netCalories, int goal) {
		int difference = goal - netCalories;
		if (Math.abs(difference) <= Math.max(100, goal * 0.1)) {
			return GoalStatus.MEET;
		}
		return difference > 0 ? GoalStatus.UNDER : GoalStatus.OVER;
	}

	private BigDecimal sum(List<BigDecimal> values) {
		return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}