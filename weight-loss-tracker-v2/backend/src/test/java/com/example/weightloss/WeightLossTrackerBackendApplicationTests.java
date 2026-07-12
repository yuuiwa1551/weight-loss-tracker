package com.example.weightloss;

import com.example.weightloss.entity.EnergyPlanStatus;
import com.example.weightloss.repository.EnergyPlanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WeightLossTrackerBackendApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EnergyPlanRepository energyPlanRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void exposesHealthEndpoint() throws Exception {
		mockMvc.perform(get("/actuator/health"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void resolvesQqUserIdempotentlyAndRefreshesDisplayName() throws Exception {
		long firstId = resolveUser("1154824108", "First name");
		long secondId = resolveUser("1154824108", "Updated name");

		assertThat(secondId).isEqualTo(firstId);
		mockMvc.perform(get("/api/users"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data[?(@.username == '1154824108')].displayName").value("Updated name"));
	}

	@Test
	void rejectsNonQqUsername() throws Exception {
		mockMvc.perform(post("/api/users/resolve")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "platform": "aiocqhttp",
					  "username": "nickname-is-not-an-id",
					  "displayName": "Bad identity"
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void newUserHasIncompleteProfileAndUnsetGoalSummary() throws Exception {
		long userId = resolveUser("2000000001", "New user");

		mockMvc.perform(get(profilePath(userId)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.nickname").value("New user"))
			.andExpect(jsonPath("$.data.profileComplete").value(false))
			.andExpect(jsonPath("$.data.missingFields.length()").value(4));

		mockMvc.perform(get(userPath(userId, "/summaries/daily")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.goalStatus").value("UNSET"))
			.andExpect(jsonPath("$.data.calorieDifference").doesNotExist());

		mockMvc.perform(get(userPath(userId, "/energy-budgets/daily")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.goalMode").value("UNSET"))
			.andExpect(jsonPath("$.data.exerciseCaloriesBurned").value(0))
			.andExpect(jsonPath("$.data.caloriesConsumed").value(0))
			.andExpect(jsonPath("$.data.todayIntakeBudgetCalories").doesNotExist());
	}

	@Test
	void updatesProfileAndRejectsUnrealisticValues() throws Exception {
		long userId = resolveUser("2000000002", "Profile user");

		mockMvc.perform(put(profilePath(userId))
				.contentType(MediaType.APPLICATION_JSON)
				.content(profileJson(175, 75, 68, 1900)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.profileComplete").value(true))
			.andExpect(jsonPath("$.data.bmi").value(24.5));

		mockMvc.perform(put(profilePath(userId))
				.contentType(MediaType.APPLICATION_JSON)
				.content(profileJson(30, 75, 68, 20000)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false));

		mockMvc.perform(get(userPath(userId, "/energy-budgets/daily")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.goalMode").value("MANUAL"))
			.andExpect(jsonPath("$.data.baseIntakeTargetCalories").value(1900))
			.andExpect(jsonPath("$.data.remainingIntakeCalories").value(1900));
	}

	@Test
	void updatesEnergyProfileAndKeepsNewFieldsForLegacyRequests() throws Exception {
		long userId = resolveUser("2000000013", "Energy profile user");

		mockMvc.perform(put(profilePath(userId))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "nickname": "Energy profile user",
					  "heightCm": 165,
					  "currentWeightKg": 52,
					  "targetWeightKg": 48,
					  "dailyCalorieGoal": null,
					  "ageYears": 24,
					  "formulaSex": "FEMALE",
					  "nonExerciseActivityLevel": "LIGHT",
					  "calorieGoalMode": "AUTO"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.ageYears").value(24))
			.andExpect(jsonPath("$.data.formulaSex").value("FEMALE"))
			.andExpect(jsonPath("$.data.nonExerciseActivityLevel").value("LIGHT"))
			.andExpect(jsonPath("$.data.calorieGoalMode").value("AUTO"))
			.andExpect(jsonPath("$.data.energyProfileComplete").value(true))
			.andExpect(jsonPath("$.data.energyMissingFields.length()").value(0));

		mockMvc.perform(put(profilePath(userId))
				.contentType(MediaType.APPLICATION_JSON)
				.content(profileJson(165, 52, 48, 1500)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.ageYears").value(24))
			.andExpect(jsonPath("$.data.formulaSex").value("FEMALE"))
			.andExpect(jsonPath("$.data.nonExerciseActivityLevel").value("LIGHT"))
			.andExpect(jsonPath("$.data.calorieGoalMode").value("MANUAL"))
			.andExpect(jsonPath("$.data.energyProfileComplete").value(true));
	}

	@Test
	void isolatesFoodAndExerciseRecordsByUser() throws Exception {
		long firstUser = resolveUser("2000000003", "First");
		long secondUser = resolveUser("2000000004", "Second");
		String date = LocalDate.now().toString();

		mockMvc.perform(post(userPath(firstUser, "/food-records"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(foodJson(date, "meal-first", "LLM_ESTIMATE")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.foodName").value("Salmon rice bowl"))
			.andExpect(jsonPath("$.data.nutritionSource").value("LLM_ESTIMATE"))
			.andExpect(jsonPath("$.data.estimationNote").value("Estimated from one serving"));

		mockMvc.perform(post(userPath(firstUser, "/exercise-records"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(exerciseJson(date, "exercise-first")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.exerciseName").value("Full-body session"));

		mockMvc.perform(get(userPath(secondUser, "/food-records") + "?date=" + date))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(0));
		mockMvc.perform(get(userPath(secondUser, "/exercise-records") + "?date=" + date))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(0));
	}

	@Test
	void preventsCrossUserDelete() throws Exception {
		long owner = resolveUser("2000000005", "Owner");
		long other = resolveUser("2000000006", "Other");
		String date = LocalDate.now().toString();
		long recordId = responseDataId(mockMvc.perform(post(userPath(owner, "/food-records"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(foodJson(date, "owner-meal", "USER_PROVIDED")))
			.andExpect(status().isOk())
			.andReturn());

		mockMvc.perform(delete(userPath(other, "/food-records/") + recordId))
			.andExpect(status().isNotFound());
		mockMvc.perform(delete(userPath(owner, "/food-records/") + recordId))
			.andExpect(status().isOk());
	}

	@Test
	void foodCreateIsIdempotentByClientRequestId() throws Exception {
		long userId = resolveUser("2000000007", "Idempotent");
		String date = LocalDate.now().toString();
		String payload = foodJson(date, "same-message", "LLM_ESTIMATE");

		long firstId = responseDataId(mockMvc.perform(post(userPath(userId, "/food-records"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
			.andExpect(status().isOk())
			.andReturn());
		long secondId = responseDataId(mockMvc.perform(post(userPath(userId, "/food-records"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
			.andExpect(status().isOk())
			.andReturn());

		assertThat(secondId).isEqualTo(firstId);
		mockMvc.perform(get(userPath(userId, "/food-records") + "?date=" + date))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.length()").value(1));
	}

	@Test
	void rejectsUnrealisticFoodCalories() throws Exception {
		long userId = resolveUser("2000000008", "Validation");
		String date = LocalDate.now().toString();

		mockMvc.perform(post(userPath(userId, "/food-records"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "recordDate": "%s",
					  "mealType": "DINNER",
					  "foodName": "Impossible feast",
					  "calories": 25000
					}
					""".formatted(date)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void latestWeightRecordSynchronizesProfileAndDeleteFallsBack() throws Exception {
		long userId = resolveUser("2000000009", "Weight user");
		String olderDate = LocalDate.now().minusDays(1).toString();
		String latestDate = LocalDate.now().toString();

		mockMvc.perform(post(userPath(userId, "/weight-records"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(weightJson(olderDate, "75.3", "weight-old")))
			.andExpect(status().isOk());
		long latestId = responseDataId(mockMvc.perform(post(userPath(userId, "/weight-records"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(weightJson(latestDate, "74.2", "weight-new")))
			.andExpect(status().isOk())
			.andReturn());

		mockMvc.perform(get(profilePath(userId)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.currentWeightKg").value(74.2));

		mockMvc.perform(delete(userPath(userId, "/weight-records/") + latestId))
			.andExpect(status().isOk());
		mockMvc.perform(get(profilePath(userId)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.currentWeightKg").value(75.3));
	}

	@Test
	void reportsAreIsolatedAndSupportUnsetGoals() throws Exception {
		long configured = resolveUser("2000000010", "Configured");
		long empty = resolveUser("2000000011", "Empty");
		String date = LocalDate.now().toString();

		mockMvc.perform(put(profilePath(configured))
				.contentType(MediaType.APPLICATION_JSON)
				.content(profileJson(175, 75, 68, 1900)))
			.andExpect(status().isOk());
		mockMvc.perform(post(userPath(configured, "/food-records"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(foodJson(date, "report-food", "USER_PROVIDED")))
			.andExpect(status().isOk());

		mockMvc.perform(get(userPath(configured, "/reports/overview?days=7")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.totalCaloriesConsumed").value(620));
		mockMvc.perform(get(userPath(empty, "/reports/overview?days=7")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.totalCaloriesConsumed").value(0))
			.andExpect(jsonPath("$.data.daysUnderGoal").value(0))
			.andExpect(jsonPath("$.data.daysMeetGoal").value(0))
			.andExpect(jsonPath("$.data.daysOverGoal").value(0));
	}

	@Test
	void recentWeightRecordsRejectOutOfRangeDays() throws Exception {
		long userId = resolveUser("2000000012", "Days validation");

		mockMvc.perform(get(userPath(userId, "/weight-records/recent?days=3")))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void previewsConfirmsAndCalculatesAutomaticDailyBudget() throws Exception {
		long userId = resolveUser("2000000014", "Automatic plan user");
		updateEnergyProfile(userId, 24);

		mockMvc.perform(get(userPath(userId, "/energy-plans/active")))
			.andExpect(status().isNotFound());

		String fingerprint = previewPlan(userId, 450);
		mockMvc.perform(get(userPath(userId, "/energy-plans/active")))
			.andExpect(status().isNotFound());
		MvcResult confirmed = confirmPlan(userId, 450, fingerprint, "plan-auto-1");
		long planId = responseDataId(confirmed);

		mockMvc.perform(post(userPath(userId, "/energy-plans"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(confirmPlanJson(450, fingerprint, "plan-auto-1")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.id").value(planId));

		String date = LocalDate.now().toString();
		mockMvc.perform(post(userPath(userId, "/food-records"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(foodJson(date, "budget-food", "USER_PROVIDED")))
			.andExpect(status().isOk());
		mockMvc.perform(post(userPath(userId, "/exercise-records"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(exerciseJson(date, "budget-exercise")))
			.andExpect(status().isOk());

		mockMvc.perform(get(userPath(userId, "/energy-budgets/daily?date=" + date)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.restingEnergyCalories").value(1270))
			.andExpect(jsonPath("$.data.baselineExpenditureCalories").value(1651))
			.andExpect(jsonPath("$.data.exerciseCaloriesBurned").value(260))
			.andExpect(jsonPath("$.data.estimatedTotalExpenditureCalories").value(1911))
			.andExpect(jsonPath("$.data.baseIntakeTargetCalories").value(1201))
			.andExpect(jsonPath("$.data.todayIntakeBudgetCalories").value(1461))
			.andExpect(jsonPath("$.data.caloriesConsumed").value(620))
			.andExpect(jsonPath("$.data.remainingIntakeCalories").value(841))
			.andExpect(jsonPath("$.data.projectedDeficitCalories").value(1291))
			.andExpect(jsonPath("$.data.goalMode").value("AUTO"))
			.andExpect(jsonPath("$.data.calculationVersion").value("P6_V1"));

		mockMvc.perform(get(profilePath(userId)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.calorieGoalMode").value("AUTO"));
	}

	@Test
	void rejectsStalePlanPreviewAndSupersedesPreviousPlan() throws Exception {
		long userId = resolveUser("2000000015", "Stale plan user");
		updateEnergyProfile(userId, 24);
		String staleFingerprint = previewPlan(userId, 300);
		updateEnergyProfile(userId, 25);

		mockMvc.perform(post(userPath(userId, "/energy-plans"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(confirmPlanJson(300, staleFingerprint, "stale-plan")))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.success").value(false));
		assertThat(energyPlanRepository.findAll()).isEmpty();

		String firstFingerprint = previewPlan(userId, 300);
		long firstPlanId = responseDataId(confirmPlan(userId, 300, firstFingerprint, "fresh-plan-1"));
		String staleReplacementFingerprint = previewPlan(userId, 400);
		String interleavingFingerprint = previewPlan(userId, 350);
		confirmPlan(userId, 350, interleavingFingerprint, "interleaving-plan");
		mockMvc.perform(post(userPath(userId, "/energy-plans"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(confirmPlanJson(400, staleReplacementFingerprint, "stale-replacement")))
			.andExpect(status().isConflict());

		String secondFingerprint = previewPlan(userId, 400);
		long secondPlanId = responseDataId(confirmPlan(userId, 400, secondFingerprint, "fresh-plan-2"));

		assertThat(secondPlanId).isNotEqualTo(firstPlanId);
		assertThat(energyPlanRepository.findByUserIdAndStatus(userId, EnergyPlanStatus.ACTIVE)).hasSize(1);
		assertThat(energyPlanRepository.findByUserIdAndStatus(userId, EnergyPlanStatus.SUPERSEDED)).hasSize(2);
		mockMvc.perform(get(userPath(userId, "/energy-plans/active")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.id").value(secondPlanId))
			.andExpect(jsonPath("$.data.calculation.dailyDeficitCalories").value(400));
	}

	private long resolveUser(String username, String displayName) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/users/resolve")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "platform": "aiocqhttp",
					  "username": "%s",
					  "displayName": "%s"
					}
					""".formatted(username, displayName)))
			.andExpect(status().isOk())
			.andReturn();
		return responseDataId(result);
	}

	private long responseDataId(MvcResult result) throws Exception {
		JsonNode root = objectMapper.readTree(result.getResponse().getContentAsByteArray());
		return root.path("data").path("id").longValue();
	}

	private void updateEnergyProfile(long userId, int ageYears) throws Exception {
		mockMvc.perform(put(profilePath(userId))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "nickname": "Energy plan user",
					  "heightCm": 165,
					  "currentWeightKg": 52,
					  "targetWeightKg": 48,
					  "dailyCalorieGoal": null,
					  "ageYears": %d,
					  "formulaSex": "FEMALE",
					  "nonExerciseActivityLevel": "LIGHT",
					  "calorieGoalMode": "UNSET"
					}
					""".formatted(ageYears)))
			.andExpect(status().isOk());
	}

	private String previewPlan(long userId, int dailyDeficitCalories) throws Exception {
		MvcResult result = mockMvc.perform(post(userPath(userId, "/energy-plans/preview"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "dailyDeficitCalories": %d,
					  "targetPeriodDays": null
					}
					""".formatted(dailyDeficitCalories)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.calculation.restingEnergyCalories").isNumber())
			.andExpect(jsonPath("$.data.previewFingerprint").isString())
			.andReturn();
		JsonNode root = objectMapper.readTree(result.getResponse().getContentAsByteArray());
		return root.path("data").path("previewFingerprint").stringValue();
	}

	private MvcResult confirmPlan(long userId, int dailyDeficitCalories, String fingerprint, String requestId)
		throws Exception {
		return mockMvc.perform(post(userPath(userId, "/energy-plans"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(confirmPlanJson(dailyDeficitCalories, fingerprint, requestId)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.status").value("ACTIVE"))
			.andExpect(jsonPath("$.data.calculation.dailyDeficitCalories").value(dailyDeficitCalories))
			.andReturn();
	}

	private String confirmPlanJson(int dailyDeficitCalories, String fingerprint, String requestId) {
		return """
			{
			  "calculation": {
			    "dailyDeficitCalories": %d,
			    "targetPeriodDays": null
			  },
			  "previewFingerprint": "%s",
			  "clientRequestId": "%s"
			}
			""".formatted(dailyDeficitCalories, fingerprint, requestId);
	}

	private String userPath(long userId, String suffix) {
		return "/api/users/" + userId + suffix;
	}

	private String profilePath(long userId) {
		return userPath(userId, "/profile");
	}

	private String profileJson(int height, int currentWeight, int targetWeight, int calorieGoal) {
		return """
			{
			  "nickname": "Profile user",
			  "heightCm": %d,
			  "currentWeightKg": %d,
			  "targetWeightKg": %d,
			  "dailyCalorieGoal": %d
			}
			""".formatted(height, currentWeight, targetWeight, calorieGoal);
	}

	private String foodJson(String date, String requestId, String nutritionSource) {
		return """
			{
			  "recordDate": "%s",
			  "mealType": "DINNER",
			  "foodName": "Salmon rice bowl",
			  "calories": 620,
			  "protein": 42.5,
			  "fat": 22.0,
			  "carbohydrate": 58.0,
			  "note": "integration test",
			  "source": "ASTRBOT",
			  "clientRequestId": "%s",
			  "nutritionSource": "%s",
			  "estimationNote": "Estimated from one serving"
			}
			""".formatted(date, requestId, nutritionSource);
	}

	private String exerciseJson(String date, String requestId) {
		return """
			{
			  "recordDate": "%s",
			  "exerciseType": "Strength",
			  "exerciseName": "Full-body session",
			  "durationMinutes": 45,
			  "caloriesBurned": 260,
			  "note": "integration test",
			  "source": "ASTRBOT",
			  "clientRequestId": "%s"
			}
			""".formatted(date, requestId);
	}

	private String weightJson(String date, String weight, String requestId) {
		return """
			{
			  "recordDate": "%s",
			  "weightKg": %s,
			  "bodyFatPercentage": 23.4,
			  "note": "integration test",
			  "source": "ASTRBOT",
			  "clientRequestId": "%s"
			}
			""".formatted(date, weight, requestId);
	}
}
