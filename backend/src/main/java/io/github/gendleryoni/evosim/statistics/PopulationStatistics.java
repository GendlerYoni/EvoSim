package io.github.gendleryoni.evosim.statistics;

public record PopulationStatistics(
        int herbivores,
        int predators,
        int food
) {
    public PopulationStatistics {
        if (herbivores < 0 || predators < 0 || food < 0) {
            throw new IllegalArgumentException(
                    "Population counts cannot be negative"
            );
        }
    }
}