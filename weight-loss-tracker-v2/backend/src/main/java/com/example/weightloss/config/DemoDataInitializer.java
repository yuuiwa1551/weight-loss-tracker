package com.example.weightloss.config;

import com.example.weightloss.entity.ExerciseRecord;
import com.example.weightloss.entity.FoodRecord;
import com.example.weightloss.entity.MealType;
import com.example.weightloss.entity.UserProfile;
import com.example.weightloss.entity.WeightRecord;
import com.example.weightloss.repository.ExerciseRecordRepository;
import com.example.weightloss.repository.FoodRecordRepository;
import com.example.weightloss.repository.UserProfileRepository;
import com.example.weightloss.repository.WeightRecordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DemoDataInitializer implements CommandLineRunner {

	private final UserProfileRepository userProfileRepository;
	private final FoodRecordRepository foodRecordRepository;
	private final ExerciseRecordRepository exerciseRecordRepository;
	private final WeightRecordRepository weightRecordRepository;

	public DemoDataInitializer(
		UserProfileRepository userProfileRepository,
		FoodRecordRepository foodRecordRepository,
		ExerciseRecordRepository exerciseRecordRepository,
		WeightRecordRepository weightRecordRepository
	) {
		this.userProfileRepository = userProfileRepository;
		this.foodRecordRepository = foodRecordRepository;
		this.exerciseRecordRepository = exerciseRecordRepository;
		this.weightRecordRepository = weightRecordRepository;
	}

	@Override
	public void run(String... args) {
		UserProfile profile = userProfileRepository.findFirstByOrderByIdAsc()
			.orElseGet(() -> userProfileRepository.save(defaultProfile()));

		LocalDate today = LocalDate.now();
		if (foodRecordRepository.countByRecordDate(today) == 0) {
			foodRecordRepository.save(new FoodRecord(today, MealType.BREAKFAST, "Oatmeal bowl", 320,
				new BigDecimal("12.0"), new BigDecimal("7.0"), new BigDecimal("52.0"), "Seed breakfast"));
			foodRecordRepository.save(new FoodRecord(today, MealType.LUNCH, "Chicken salad", 460,
				new BigDecimal("38.0"), new BigDecimal("18.0"), new BigDecimal("28.0"), "Seed lunch"));
		}

		if (exerciseRecordRepository.countByRecordDate(today) == 0) {
			exerciseRecordRepository.save(new ExerciseRecord(today, "Cardio", "Easy run", 30, 280, "Seed exercise"));
		}

		if (weightRecordRepository.count() == 0) {
			weightRecordRepository.save(new WeightRecord(today.minusDays(28), new BigDecimal("76.4"), new BigDecimal("24.8"), "Seed baseline"));
			weightRecordRepository.save(new WeightRecord(today.minusDays(21), new BigDecimal("75.9"), new BigDecimal("24.4"), "Seed trend"));
			weightRecordRepository.save(new WeightRecord(today.minusDays(14), new BigDecimal("75.3"), new BigDecimal("24.0"), "Seed trend"));
			weightRecordRepository.save(new WeightRecord(today.minusDays(7), new BigDecimal("74.8"), new BigDecimal("23.7"), "Seed trend"));
			weightRecordRepository.save(new WeightRecord(today, profile.getCurrentWeightKg(), new BigDecimal("23.5"), "Seed current"));
		}
	}

	private UserProfile defaultProfile() {
		UserProfile profile = new UserProfile();
		profile.setNickname("Demo User");
		profile.setHeightCm(new BigDecimal("175.0"));
		profile.setCurrentWeightKg(new BigDecimal("75.0"));
		profile.setTargetWeightKg(new BigDecimal("68.0"));
		profile.setDailyCalorieGoal(1900);
		return profile;
	}
}