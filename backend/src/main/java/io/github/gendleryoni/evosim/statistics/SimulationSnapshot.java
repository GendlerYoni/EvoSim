package io.github.gendleryoni.evosim.statistics;

import java.util.Objects;

public record SimulationSnapshot(
        PopulationStatistics population,
        GenomeAverages herbivores,
        GenomeAverages predators
) {
    public SimulationSnapshot {
        Objects.requireNonNull(population, "Population statistics cannot be null");
    }
}