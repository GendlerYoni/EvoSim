
package io.github.gendleryoni.evosim.controller;

import io.github.gendleryoni.evosim.dto.CreateSimulationRequest;
import io.github.gendleryoni.evosim.service.ServerAtCapacityException;
import io.github.gendleryoni.evosim.service.SimulationService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SimulationController.class)
@Import(GlobalExceptionHandler.class)
class SimulationControllerTest {

    private static final String API_PATH = "/api/simulations";

    private static final String VALID_REQUEST = """
            {
                "initialHerbivores": 100,
                "initialPredators": 10,
                "initialFood": 500,
                "foodSpawnMaxPerTick": 20
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SimulationService simulationService;

    @Test
    void validRequestReturns201AndSessionId() throws Exception {
        UUID sessionId = UUID.randomUUID();

        when(simulationService.createSimulation(
                any(CreateSimulationRequest.class)
        )).thenReturn(sessionId);

        mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId")
                        .value(sessionId.toString()));

        verify(simulationService).createSimulation(
                new CreateSimulationRequest(100, 10, 500, 20)
        );
    }

    @Test
    void zeroValuesAreAccepted() throws Exception {
        String request = """
                {
                    "initialHerbivores": 0,
                    "initialPredators": 0,
                    "initialFood": 0,
                    "foodSpawnMaxPerTick": 0
                }
                """;

        when(simulationService.createSimulation(
                any(CreateSimulationRequest.class)
        )).thenReturn(UUID.randomUUID());

        mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        verify(simulationService).createSimulation(
                new CreateSimulationRequest(0, 0, 0, 0)
        );
    }

    @Test
    void maximumValuesAreAccepted() throws Exception {
        String request = """
                {
                    "initialHerbivores": 1000,
                    "initialPredators": 1000,
                    "initialFood": 5000,
                    "foodSpawnMaxPerTick": 100
                }
                """;

        when(simulationService.createSimulation(
                any(CreateSimulationRequest.class)
        )).thenReturn(UUID.randomUUID());

        mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        verify(simulationService).createSimulation(
                new CreateSimulationRequest(1000, 1000, 5000, 100)
        );
    }

    @Test
    void invalidValuesReturn400WithFieldErrors() throws Exception {
        String request = """
                {
                    "initialHerbivores": -10,
                    "initialPredators": 5000,
                    "initialFood": 500,
                    "foodSpawnMaxPerTick": 20
                }
                """;

        mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.initialHerbivores")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.initialPredators")
                        .exists());

        verifyNoInteractions(simulationService);
    }

    @Test
    void foodLimitsAreEnforced() throws Exception {
        String request = """
                {
                    "initialHerbivores": 100,
                    "initialPredators": 10,
                    "initialFood": 5001,
                    "foodSpawnMaxPerTick": 101
                }
                """;

        mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.initialFood")
                        .exists())
                .andExpect(jsonPath("$.fieldErrors.foodSpawnMaxPerTick")
                        .exists());

        verifyNoInteractions(simulationService);
    }

    @Test
    void missingRequiredFieldReturns400() throws Exception {
        String request = """
                {
                    "initialHerbivores": 100,
                    "initialPredators": 10,
                    "initialFood": 500
                }
                """;

        mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.foodSpawnMaxPerTick")
                        .exists());

        verifyNoInteractions(simulationService);
    }

    @Test
    void nullRequiredFieldReturns400() throws Exception {
        String request = """
                {
                    "initialHerbivores": 100,
                    "initialPredators": null,
                    "initialFood": 500,
                    "foodSpawnMaxPerTick": 20
                }
                """;

        mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.initialPredators")
                        .exists());

        verifyNoInteractions(simulationService);
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        String request = """
                {"initialHerbivores":100,
                """;

        mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_REQUEST_BODY"));

        verifyNoInteractions(simulationService);
    }

    @Test
    void invalidFieldTypeReturns400() throws Exception {
        String request = """
                {
                    "initialHerbivores": 100,
                    "initialPredators": 10,
                    "initialFood": "hello",
                    "foodSpawnMaxPerTick": 20
                }
                """;

        mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_REQUEST_BODY"));

        verifyNoInteractions(simulationService);
    }

    @Test
    void serverAtCapacityReturns503() throws Exception {
        when(simulationService.createSimulation(
                any(CreateSimulationRequest.class)
        )).thenThrow(new ServerAtCapacityException());

        mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code")
                        .value("SERVER_AT_CAPACITY"));
    }

    @Test
    void unexpectedExceptionReturns500() throws Exception {
        when(simulationService.createSimulation(
                any(CreateSimulationRequest.class)
        )).thenThrow(new RuntimeException("Sensitive internal detail"));

        mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code")
                        .value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("An unexpected server error occurred"));
    }

    @Test
    void getOnPostOnlyEndpointReturns405() throws Exception {
        mockMvc.perform(get(API_PATH))
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(simulationService);
    }

    @Test
    void unknownEndpointReturns404() throws Exception {
        mockMvc.perform(get("/api/nonexistent-endpoint"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(simulationService);
    }

    @Test
    void unsupportedContentTypeReturns415() throws Exception {
        mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(VALID_REQUEST))
                .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(simulationService);
    }
}
