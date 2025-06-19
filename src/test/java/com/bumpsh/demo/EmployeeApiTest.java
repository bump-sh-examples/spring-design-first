package com.bumpsh.demo;

import com.github.erosb.kappa.autoconfigure.EnableKappaContractTesting;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The @EnableKappaContractTesting will tell Kappa to validate all HTTP requests and responses against the openapi.yaml.
 *
 * So contract testing is an additional verification step in every test. If an endpoint sends a response that doesn't
 * match the openapi description, then the test will fail, even if all assertions of the concrete test would pass.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnableKappaContractTesting
@Tag("failing-contract-tests")
public class EmployeeApiTest {

    @Autowired
    MockMvc mvc;

    /**
     * Fails because the openapi.yaml describes a HTTP 201 Created response code, but the SUT responds with 200 OK.
     */
    @Test
    void responseCodeMismatch() throws Exception {
        mvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Bilbo",
                            "role": "ring-bearer"
                        }
                        """))
                // here we don't exactly specify the expected response code
                // but due to @EnableKappaContractTesting, Kappa will still check if the response code is in the openapi.yaml
                .andExpect(status().is2xxSuccessful());
    }

    /**
     * Fails because the 404 response in the openapi.yaml describes a mandatory "id" property, but it isn't present in the response
     */
    @Test
    void notFoundResponseBodyMismatch() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/employees/22").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                // actually, this is the json that will be returned by the endpoint, but it doesn't match the openapi description
                // due to the missing "id" property, so the test fails
                .andExpect(content().json("""
                        {
                            "message": "Could not find employee 22"
                        }
                        """));
    }
}
