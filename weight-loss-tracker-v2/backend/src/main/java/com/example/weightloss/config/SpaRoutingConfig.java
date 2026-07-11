package com.example.weightloss.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnProperty(name = "app.spa.enabled", havingValue = "true")
public class SpaRoutingConfig implements WebMvcConfigurer {

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		for (String route : new String[] {"/food", "/exercise", "/weight", "/reports", "/profile"}) {
			registry.addViewController(route).setViewName("forward:/index.html");
		}
	}
}
