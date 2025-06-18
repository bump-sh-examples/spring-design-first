package com.bumpsh.demo;

import com.github.erosb.kappa.autoconfigure.EnableKappaRequestValidation;
import com.github.erosb.kappa.autoconfigure.KappaSpringConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;

import java.util.LinkedHashMap;

@SpringBootApplication
@EnableKappaRequestValidation
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public KappaSpringConfiguration kappaSpringConfiguration() {
		KappaSpringConfiguration kappaConfig = new KappaSpringConfiguration();
		var pathPatternToOpenapiDescription = new LinkedHashMap<String, String>();
		pathPatternToOpenapiDescription.put("/**", "/api/openapi.yaml");
		kappaConfig.setOpenapiDescriptions(pathPatternToOpenapiDescription);
		return kappaConfig;
	}

}
