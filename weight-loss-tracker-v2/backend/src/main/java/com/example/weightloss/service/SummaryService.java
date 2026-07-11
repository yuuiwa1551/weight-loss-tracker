package com.example.weightloss.service;

import com.example.weightloss.dto.DailySummaryResponse;
import com.example.weightloss.dto.ExerciseRecordResponse;
import com.example.weightloss.dto.FoodRecordResponse;
import com.example.weightloss.dto.PeriodReportResponse;
import com.example.weightloss.dto.RecentSummaryResponse;
import com.example.weightloss.entity.ExerciseRecord;
import com.example.weightloss.entity.FoodRecord;
import com.example.weightloss.entity.GoalStatus;
import com.example.weightloss.entity.UserProfile;
import com.example.weightloss.entity.WeightRecord;
import com.example.weightloss.repository.ExerciseRecordRepository;
import com.example.weightloss.repository.FoodRecordRepository;
import com.example.weightloss.repository.WeightRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SummaryService {

	private final ProfileService profileService;
	private final FoodRecordRepository foodRecordRepository;
	private final ExerciseRecordRepository exerciseRecordRepository;
	private final WeightRecordRepository weightRecordRepository;

	public SummaryService(
		ProfileService profileService,
		FoodRecordRepository foodRecordRepository,
		ExerciseRecordRepository exerciseRecordRepository,
		WeightRecordRepository weightRecordRepository
	) {
		this.profileService = profileService;
		this.foodRecordRepository = foodRecordRepository;
		this.exerciseRecordRepository = exerciseRecordRepository;
		this.weightRecordRepository = weightRecordRepository;
	}

	@Transactional(readOnly = true)
	public DailySummaryResponse getDailySummary(Long userId, LocalDate date) {
		UserProfile profile = profileService.getProfileEntity(userId);
		List<FoodRecord> foodRecords = foodRecordRepository.findByUserIdAndRecordDateOrderByCreatedAtAscIdAsc(userId, date);
		List<ExerciseRecord> exerciseRecords = exerciseRecordRepository.findByUserIdAndRecordDateOrderByCreatedAtAscIdAsc(userId, date);

		return buildDailySummary(date, profile.getDailyCalorieGoal(), foodRecords, exerciseRecords);
	}

	@Transactional(readOnly = true)
	public List<RecentSummaryResponse> getRecentSummaries(Long userId, int days) {
		LocalDate endDate = LocalDate.now();
		LocalDate startDate = endDate.minusDays(days - 1L);
		UserProfile profile = profileService.getProfileEntity(userId);
		List<FoodRecord> foodRecords = foodRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAscCreatedAtAscIdAsc(userId, startDate, endDate);
		List<ExerciseRecord> exerciseRecords = exerciseRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAscCreatedAtAscIdAsc(userId, startDate, endDate);
		Map<LocalDate, List<FoodRecord>> foodRecordsByDate = groupFoodRecordsByDate(foodRecords);
		Map<LocalDate, List<ExerciseRecord>> exerciseRecordsByDate = groupExerciseRecordsByDate(exerciseRecords);

		return startDate.datesUntil(endDate.plusDays(1))
			.map(date -> buildDailySummary(
				date,
				profile.getDailyCalorieGoal(),
				foodRecordsByDate.getOrDefault(date, List.of()),
				exerciseRecordsByDate.getOrDefault(date, List.of())
			))
			.map(RecentSummaryResponse::fromDaily)
			.toList();
	}

	@Transactional(readOnly = true)
	public PeriodReportResponse getPeriodReport(Long userId, int days) {
		LocalDate endDate = LocalDate.now();
		LocalDate startDate = endDate.minusDays(days - 1L);
		UserProfile profile = profileService.getProfileEntity(userId);
		List<FoodRecord> foodRecords = foodRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAscCreatedAtAscIdAsc(userId, startDate, endDate);
		List<ExerciseRecord> exerciseRecords = exerciseRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAscCreatedAtAscIdAsc(userId, startDate, endDate);
		List<WeightRecord> weightRecords = weightRecordRepository.findByUserIdAndRecordDateBetweenOrderByRecordDateAscCreatedAtAscIdAsc(userId, startDate, endDate);
		Map<LocalDate, List<FoodRecord>> foodRecordsByDate = groupFoodRecordsByDate(foodRecords);
		Map<LocalDate, List<ExerciseRecord>> exerciseRecordsByDate = groupExerciseRecordsByDate(exerciseRecords);

		List<DailySummaryResponse> dailySummaries = startDate.datesUntil(endDate.plusDays(1))
			.map(date -> buildDailySummary(
				date,
				profile.getDailyCalorieGoal(),
				foodRecordsByDate.getOrDefault(date, List.of()),
				exerciseRecordsByDate.getOrDefault(date, List.of())
			))
			.toList();

		int totalCaloriesConsumed = dailySummaries.stream().mapToInt(DailySummaryResponse::totalCaloriesConsumed).sum();
		int totalCaloriesBurned = dailySummaries.stream().mapToInt(DailySummaryResponse::totalCaloriesBurned).sum();
		int netCalories = totalCaloriesConsumed - totalCaloriesBurned;
		BigDecimal totalProtein = sum(dailySummaries.stream().map(DailySummaryResponse::totalProtein).toList());
		BigDecimal totalFat = sum(dailySummaries.stream().map(DailySummaryResponse::totalFat).toList());
		BigDecimal totalCarbohydrate = sum(dailySummaries.stream().map(DailySummaryResponse::totalCarbohydrate).toList());
		BigDecimal startWeightKg = weightRecords.isEmpty() ? null : weightRecords.get(0).getWeightKg();
		BigDecimal endWeightKg = weightRecords.isEmpty() ? null : weightRecords.get(weightRecords.size() - 1).getWeightKg();

		return new PeriodReportResponse(
			startDate,
			endDate,
			days,
			totalCaloriesConsumed,
			totalCaloriesBurned,
			netCalories,
			average(totalCaloriesConsumed, days),
			average(totalCaloriesBurned, days),
			average(netCalories, days),
			average(totalProtein, days),
			average(totalFat, days),
			average(totalCarbohydrate, days),
			profile.getDailyCalorieGoal(),
			dailySummaries.stream().filter(summary -> summary.goalStatus() == GoalStatus.UNDER).count(),
			dailySummaries.stream().filter(summary -> summary.goalStatus() == GoalStatus.MEET).count(),
			dailySummaries.stream().filter(summary -> summary.goalStatus() == GoalStatus.OVER).count(),
			startWeightKg,
			endWeightKg,
			startWeightKg == null || endWeightKg == null ? null : endWeightKg.subtract(startWeightKg),
			dailySummaries.stream().map(RecentSummaryResponse::fromDaily).toList()
		);
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
		Integer calorieDifference = dailyCalorieGoal == null ? null : dailyCalorieGoal - netCalories;

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

	private GoalStatus calculateGoalStatus(int netCalories, Integer goal) {
		if (goal == null) {
			return GoalStatus.UNSET;
		}
		int difference = goal - netCalories;
		if (Math.abs(difference) <= Math.max(100, goal * 0.1)) {
			return GoalStatus.MEET;
		}
		return difference > 0 ? GoalStatus.UNDER : GoalStatus.OVER;
	}

	private BigDecimal sum(List<BigDecimal> values) {
		return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private BigDecimal average(int value, int days) {
		return BigDecimal.valueOf(value).divide(BigDecimal.valueOf(days), 1, RoundingMode.HALF_UP);
	}

	private BigDecimal average(BigDecimal value, int days) {
		return value.divide(BigDecimal.valueOf(days), 1, RoundingMode.HALF_UP);
	}

	private Map<LocalDate, List<FoodRecord>> groupFoodRecordsByDate(List<FoodRecord> records) {
		return records.stream().collect(Collectors.groupingBy(FoodRecord::getRecordDate));
	}

	private Map<LocalDate, List<ExerciseRecord>> groupExerciseRecordsByDate(List<ExerciseRecord> records) {
		return records.stream().collect(Collectors.groupingBy(ExerciseRecord::getRecordDate));
	}
}
