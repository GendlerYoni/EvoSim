package io.github.gendleryoni.evosim.controller;

import io.github.gendleryoni.evosim.service.SimulationService;
import io.github.gendleryoni.evosim.dto.CreateSimulationRequest;
import io.github.gendleryoni.evosim.dto.CreateSimulationResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@RestController
@RequestMapping("/api/simulations")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping
    public ResponseEntity<CreateSimulationResponse> createSimulation(
            @Valid @RequestBody CreateSimulationRequest request
    ) {
        UUID sessionId = simulationService.createSimulation(request);

        CreateSimulationResponse response =
                new CreateSimulationResponse(sessionId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}