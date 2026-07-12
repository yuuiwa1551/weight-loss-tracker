package com.example.weightloss.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.energy")
public class EnergyCalculationProperties {

	private String calculationVersion = "P6_V1";
	private BigDecimal defaultDeficitRate = new BigDecimal("0.15");
	private BigDecimal kcalPerKilogram = new BigDecimal("7700");
}
