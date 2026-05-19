package com.company.runcoach.platform.api;

import com.company.runcoach.app.RunCoachApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RunCoachApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiErrorEnvelopeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unknownRoutesShouldReturnDocumentedErrorEnvelope() throws Exception {
        mockMvc.perform(get("/v1/auth/unknown-route"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
            .andExpect(jsonPath("$.error.message").isString())
            .andExpect(jsonPath("$.error.details").isArray())
            .andExpect(jsonPath("$.error.correlationId").isString());
    }
}
