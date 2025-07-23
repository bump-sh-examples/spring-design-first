package com.bumpsh.demo;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.erosb.kappa.autoconfigure.EnableKappaRequestValidation;
import com.github.erosb.kappa.autoconfigure.KappaSpringConfiguration;
import com.github.erosb.kappa.core.validation.OpenApiValidationFailure;
import com.github.erosb.kappa.core.validation.ValidationException;
import com.github.erosb.kappa.operation.validator.adapters.server.servlet.ValidationFailureSender;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.LinkedHashMap;

import static java.util.Objects.requireNonNull;

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
        pathPatternToOpenapiDescription.put("/**", "/openapi/openapi.yaml");
        kappaConfig.setOpenapiDescriptions(pathPatternToOpenapiDescription);
        kappaConfig.setValidationFailureSender(ValidationFailureSender.rfc9457Sender());
        return kappaConfig;
    }

}
