package com.example.weightloss;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(properties = "app.spa.enabled=true")
class SpaRoutingConfigTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void forwardsClientSideRouteToIndex() throws Exception {
		mockMvc.perform(get("/reports"))
			.andExpect(status().isOk())
			.andExpect(forwardedUrl("/index.html"));
	}

	@Test
	void leavesApiRoutesToControllers() throws Exception {
		mockMvc.perform(get("/api/users"))
			.andExpect(status().isOk());
	}

	@Test
	void doesNotTreatUnknownBackendPathsAsSpaRoutes() throws Exception {
		mockMvc.perform(get("/h2-console"))
			.andExpect(status().isNotFound());
	}
}
