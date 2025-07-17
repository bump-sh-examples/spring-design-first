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
        pathPatternToOpenapiDescription.put("/**", "/api/openapi.yaml");
        kappaConfig.setOpenapiDescriptions(pathPatternToOpenapiDescription);
        kappaConfig.setValidationFailureSender(new RFC9457FailureSender());
        return kappaConfig;
    }

}

class RFC9457FailureSender
        implements ValidationFailureSender {

    private final String typeAttributeValue;

    public RFC9457FailureSender() {
        this("https://erosb.github.io/kappa/request-validation-failure");
    }

    public RFC9457FailureSender(String typeAttributeValue) {
        this.typeAttributeValue = requireNonNull(typeAttributeValue);
    }

    @Override
    public void send(ValidationException ex, HttpServletResponse httpResp)
            throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode respObj = objectMapper.createObjectNode();
        ArrayNode errors = objectMapper.createArrayNode();
        respObj.put("type", typeAttributeValue);
        respObj.put("status", 400);
        respObj.put("title", "Validation failure");
        respObj.put("detail", ex.getMessage());
        ex.results().forEach(item -> {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("message", item.getMessage());
            error.put("dataLocation", item.describeInstanceLocation());
            if (item instanceof OpenApiValidationFailure.SchemaValidationFailure) {
                OpenApiValidationFailure.SchemaValidationFailure schemaValidationFailure = (OpenApiValidationFailure.SchemaValidationFailure) item;
                error.put("dynamicPath", schemaValidationFailure.getFailure().getDynamicPath().getPointer().toString());
            }
            errors.add(error);
        });
        respObj.put("errors", errors);

        httpResp.setStatus(400);
        httpResp.getWriter().print(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(respObj));
        httpResp.flushBuffer();
    }
}
