package com.example.weightloss.config;

import com.example.weightloss.entity.ExerciseRecord;
import com.example.weightloss.entity.FoodRecord;
import com.example.weightloss.entity.MealType;
import com.example.weightloss.entity.UserProfile;
import com.example.weightloss.repository.ExerciseRecordRepository;
import com.example.weightloss.repository.FoodRecordRepository;
import com.example.weightloss.repository.UserProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DemoDataInitializer implements CommandLineRunner {

	private final UserProfileRepository userProfileRepository;
	private final FoodRecordRepository foodRecordRepository;
	private final ExerciseRecordRepository exerciseRecordRepository;

	public DemoDataInitializer(
		UserProfileRepository userProfileRepository,
		FoodRecordRepository foodRecordRepository,
		ExerciseRecordRepository exerciseRecordRepository
	) {
		this.userProfileRepository = userProfileRepository;
		this.foodRecordRepository = foodRecordRepository;
		this.exerciseRecordRepository = exerciseRecordRepository;
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