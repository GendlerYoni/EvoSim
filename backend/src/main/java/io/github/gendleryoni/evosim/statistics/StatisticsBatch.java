package io.github.gendleryoni.evosim.statistics;

import java.util.List;
import java.util.Objects;

public record StatisticsBatch(
        int nextSampleIndex,
        List<SimulationSnapshot> snapshots
) {
    public StatisticsBatch {
        if (nextSampleIndex < 0) {
            throw new IllegalArgumentException(
                    "Next sample index cannot be negative"
            );
        }

        snapshots = List.copyOf(
                Objects.requireNonNull(snapshots)
        );
    }
}