package com.example.weightloss.config;

import com.example.weightloss.entity.ExerciseRecord;
import com.example.weightloss.entity.FoodRecord;
import com.example.weightloss.entity.AppUser;
import com.example.weightloss.entity.MealType;
import com.example.weightloss.entity.NutritionSource;
import com.example.weightloss.entity.RecordSource;
import com.example.weightloss.entity.UserProfile;
import com.example.weightloss.entity.WeightRecord;
import com.example.weightloss.repository.ExerciseRecordRepository;
import com.example.weightloss.repository.FoodRecordRepository;
import com.example.weightloss.repository.AppUserRepository;
import com.example.weightloss.repository.UserProfileRepository;
import com.example.weightloss.repository.WeightRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DemoDataInitializer implements CommandLineRunner {

	private final UserProfileRepository userProfileRepository;
	private final AppUserRepository appUserRepository;
	private final FoodRecordRepository foodRecordRepository;
	private final ExerciseRecordRepository exerciseRecordRepository;
	private final WeightRecordRepository weightRecordRepository;
	private final boolean sampleRecordsEnabled;

	public DemoDataInitializer(
		AppUserRepository appUserRepository,
		UserProfileRepository userProfileRepository,
		FoodRecordRepository foodRecordRepository,
		ExerciseRecordRepository exerciseRecordRepository,
		WeightRecordRepository weightRecordRepository,
		@Value("${app.demo-data.sample-records-enabled:false}") boolean sampleRecordsEnabled
	) {
		this.appUserRepository = appUserRepository;
		this.userProfileRepository = userProfileRepository;
		this.foodRecordRepository = foodRecordRepository;
		this.exerciseRecordRepository = exerciseRecordRepository;
		this.weightRecordRepository = weightRecordRepository;
		this.sampleRecordsEnabled = sampleRecordsEnabled;
	}

	@Override
	public void run(String... args) {
		AppUser user = appUserRepository.findByPlatformAndUsername("local", "local")
			.orElseGet(() -> appUserRepository.save(new AppUser("local", "local", "Local User")));
		UserProfile profile = userProfileRepository.findByUserId(user.getId())
			.orElseGet(() -> userProfileRepository.save(defaultProfile(user)));

		if (!sampleRecordsEnabled) {
			return;
		}

		LocalDate today = LocalDate.now();
		if (foodRecordRepository.countByUserIdAndRecordDate(user.getId(), today) == 0) {
			foodRecordRepository.save(new FoodRecord(user, today, MealType.BREAKFAST, "Oatmeal bowl", 320,
				new BigDecimal("12.0"), new BigDecimal("7.0"), new BigDecimal("52.0"), "Seed breakfast",
				RecordSource.WEB, null, NutritionSource.USER_PROVIDED, null));
			foodRecordRepository.save(new FoodRecord(user, today, MealType.LUNCH, "Chicken salad", 460,
				new BigDecimal("38.0"), new BigDecimal("18.0"), new BigDecimal("28.0"), "Seed lunch",
				RecordSource.WEB, null, NutritionSource.USER_PROVIDED, null));
		}

		if (exerciseRecordRepository.countByUserIdAndRecordDate(user.getId(), today) == 0) {
			exerciseRecordRepository.save(new ExerciseRecord(user, today, "Cardio", "Easy run", 30, 280,
				"Seed exercise", RecordSource.WEB, null));
		}

		if (weightRecordRepository.countByUserIdAndRecordDate(user.getId(), today) == 0) {
			weightRecordRepository.save(new WeightRecord(user, today.minusDays(28), new BigDecimal("76.4"), new BigDecimal("24.8"), "Seed baseline", RecordSource.WEB, null));
			weightRecordRepository.save(new WeightRecord(user, today.minusDays(21), new BigDecimal("75.9"), new BigDecimal("24.4"), "Seed trend", RecordSource.WEB, null));
			weightRecordRepository.save(new WeightRecord(user, today.minusDays(14), new BigDecimal("75.3"), new BigDecimal("24.0"), "Seed trend", RecordSource.WEB, null));
			weightRecordRepository.save(new WeightRecord(user, today.minusDays(7), new BigDecimal("74.8"), new BigDecimal("23.7"), "Seed trend", RecordSource.WEB, null));
			weightRecordRepository.save(new WeightRecord(user, today, profile.getCurrentWeightKg(), new BigDecimal("23.5"), "Seed current", RecordSource.WEB, null));
		}
	}

	private UserProfile defaultProfile(AppUser user) {
		UserProfile profile = new UserProfile(user, "Demo User");
		profile.setHeightCm(new BigDecimal("175.0"));
		profile.setCurrentWeightKg(new BigDecimal("75.0"));
		profile.setTargetWeightKg(new BigDecimal("68.0"));
		profile.setDailyCalorieGoal(1900);
		return profile;
	}
}
