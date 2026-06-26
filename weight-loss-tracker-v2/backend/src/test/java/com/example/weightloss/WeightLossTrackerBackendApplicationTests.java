package com.example.weightloss;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class WeightLossTrackerBackendApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void profileAndSummaryEndpointsWork() throws Exception {
		mockMvc.perform(get("/api/profile"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.dailyCalorieGoal").exists());

		mockMvc.perform(get("/api/summaries/daily"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.totalCaloriesConsumed").exists())
			.andExpect(jsonPath("$.data.foodRecords").isArray())
			.andExpect(jsonPath("$.data.exerciseRecords").isArray());
	}

	@Test
	void canCreateFoodAndExerciseRecords() throws Exception {
		String foodJson = """
			{
			  "recordDate": "2026-06-08",
			  "mealType": "DINNER",
			  "foodName": "Salmon rice bowl",
			  "calories": 620,
			  "protein": 42.5,
			  "fat": 22.0,
			  "carbohydrate": 58.0,
			  "note": "integration test"
			}
			""";

		mockMvc.perform(post("/api/food-records")
				.contentType(MediaType.APPLICATION_JSON)
				.content(foodJson))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.foodName").value("Salmon rice bowl"));

		String exerciseJson = """
			{
			  "recordDate": "2026-06-08",
			  "exerciseType": "Strength",
			  "exerciseName": "Full-body session",
			  "durationMinutes": 45,
			  "caloriesBurned": 260,
			  "note": "integration test"
			}
			""";

		mockMvc.perform(post("/api/exercise-records")
				.contentType(MediaType.APPLICATION_JSON)
				.content(exerciseJson))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.exerciseName").value("Full-body session"));
	}

	@Test
	void rejectsUnrealisticFoodCalories() throws Exception {
		String foodJson = """
			{
			  "recordDate": "2026-06-08",
			  "mealType": "DINNER",
			  "foodName": "Impossible feast",
			  "calories": 25000
			}
			""";

		mockMvc.perform(post("/api/food-records")
				.contentType(MediaType.APPLICATION_JSON)
				.content(foodJson))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void rejectsUnrealisticProfileValues() throws Exception {
		String profileJson = """
			{
			  "nickname": "Demo User",
			  "heightCm": 30.0,
			  "currentWeightKg": 75.0,
			  "targetWeightKg": 68.0,
			  "dailyCalorieGoal": 20000
			}
			""";

		mockMvc.perform(put("/api/profile")
				.contentType(MediaType.APPLICATION_JSON)
				.content(profileJson))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void canCreateAndListWeightRecords() throws Exception {
		String weightJson = """
			{
			  "recordDate": "2026-06-08",
			  "weightKg": 74.2,
			  "bodyFatPercentage": 23.4,
			  "note": "integration test"
			}
			""";

		mockMvc.perform(post("/api/weight-records")
				.contentType(MediaType.APPLICATION_JSON)
				.content(weightJson))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.weightKg").value(74.2));

		mockMvc.perform(get("/api/weight-records/recent?days=30"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data").isArray());
	}

	@Test
	void recentWeightRecordsRejectOutOfRangeDays() throws Exception {
		mockMvc.perform(get("/api/weight-records/recent?days=3"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void reportsEndpointWorks() throws Exception {
		mockMvc.perform(get("/api/reports/overview?days=7"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.days").value(7))
			.andExpect(jsonPath("$.data.totalCaloriesConsumed").exists())
			.andExpect(jsonPath("$.data.dailySummaries").isArray());
	}

}
