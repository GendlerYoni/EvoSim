package io.github.gendleryoni.evosim.dto;

import io.github.gendleryoni.evosim.SimulationConfig;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateSimulationRequest(
        @NotNull
        @Min(0)
        @Max(SimulationConfig.MAX_INITIAL_HERBIVORES)
        Integer initialHerbivores,

        @NotNull
        @Min(0)
        @Max(SimulationConfig.MAX_INITIAL_PREDATORS)
        Integer initialPredators,

        @NotNull
        @Min(0)
        @Max(SimulationConfig.MAX_INITIAL_FOOD)
        Integer initialFood,

        @NotNull
        @Min(0)
        @Max(SimulationConfig.MAX_FOOD_SPAWN_PER_TICK)
        Integer foodSpawnMaxPerTick
) {
}