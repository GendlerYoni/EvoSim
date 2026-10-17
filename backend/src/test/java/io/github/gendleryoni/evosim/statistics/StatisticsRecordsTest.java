
package io.github.gendleryoni.evosim.statistics;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsRecordsTest {

    private GenomeAverages validAverages() {
        return new GenomeAverages(
                1.0, 1.0, 1.0, 1.0, 1.0
        );
    }

    @Test
    void populationStatisticsAcceptsNonNegativeCounts() {
        PopulationStatistics stats =
                new PopulationStatistics(10, 5, 100);

        assertEquals(10, stats.herbivores());
        assertEquals(5, stats.predators());
        assertEquals(100, stats.food());

        assertDoesNotThrow(
                () -> new PopulationStatistics(0, 0, 0)
        );
    }

    @Test
    void populationStatisticsRejectsNegativeCounts() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new PopulationStatistics(-1, 0, 0)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new PopulationStatistics(0, -1, 0)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new PopulationStatistics(0, 0, -1)
                )
        );
    }

    @Test
    void genomeAveragesAcceptsValidBoundaryValues() {
        assertDoesNotThrow(
                () -> new GenomeAverages(
                        0.25, 0.25, 0.25, 0.5, 0.25
                )
        );

        assertDoesNotThrow(
                () -> new GenomeAverages(
                        4.0, 4.0, 4.0, 2.0, 4.0
                )
        );
    }

    @Test
    void genomeAveragesRejectsValuesOutsideTraitRanges() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new GenomeAverages(
                                0.24, 1.0, 1.0, 1.0, 1.0
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new GenomeAverages(
                                1.0, 4.01, 1.0, 1.0, 1.0
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new GenomeAverages(
                                1.0, 1.0, 0.24, 1.0, 1.0
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new GenomeAverages(
                                1.0, 1.0, 1.0, 0.49, 1.0
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new GenomeAverages(
                                1.0, 1.0, 1.0, 2.01, 1.0
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new GenomeAverages(
                                1.0, 1.0, 1.0, 1.0, 4.01
                        )
                )
        );
    }

    @Test
    void genomeAveragesRejectsNonFiniteValues() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new GenomeAverages(
                                Double.NaN, 1.0, 1.0, 1.0, 1.0
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new GenomeAverages(
                                1.0, Double.POSITIVE_INFINITY,
                                1.0, 1.0, 1.0
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new GenomeAverages(
                                1.0, 1.0, Double.NEGATIVE_INFINITY,
                                1.0, 1.0
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new GenomeAverages(
                                1.0, 1.0, 1.0, Double.NaN, 1.0
                        )
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new GenomeAverages(
                                1.0, 1.0, 1.0, 1.0, Double.NaN
                        )
                )
        );
    }

    @Test
    void simulationSnapshotRejectsNullPopulation() {
        assertThrows(
                NullPointerException.class,
                () -> new SimulationSnapshot(
                        null,
                        validAverages(),
                        validAverages()
                )
        );
    }

    @Test
    void simulationSnapshotAllowsNullGenomeAverages() {
        PopulationStatistics population =
                new PopulationStatistics(0, 0, 0);

        SimulationSnapshot snapshot =
                new SimulationSnapshot(
                        population,
                        null,
                        null
                );

        assertEquals(population, snapshot.population());
        assertNull(snapshot.herbivores());
        assertNull(snapshot.predators());
    }

    @Test
    void statisticsBatchStoresNextSampleIndexAndSnapshots() {
        SimulationSnapshot snapshot = new SimulationSnapshot(
                new PopulationStatistics(10, 5, 100),
                validAverages(),
                validAverages()
        );

        StatisticsBatch batch = new StatisticsBatch(
                151,
                List.of(snapshot)
        );

        assertEquals(151, batch.nextSampleIndex());
        assertEquals(List.of(snapshot), batch.snapshots());
    }

    @Test
    void statisticsBatchMakesDefensiveCopyOfList() {
        SimulationSnapshot snapshot = new SimulationSnapshot(
                new PopulationStatistics(1, 0, 10),
                validAverages(),
                null
        );

        List<SimulationSnapshot> original = new ArrayList<>();
        original.add(snapshot);

        StatisticsBatch batch = new StatisticsBatch(5, original);

        original.clear();

        assertEquals(1, batch.snapshots().size());
        assertEquals(snapshot, batch.snapshots().get(0));

        assertThrows(
                UnsupportedOperationException.class,
                () -> batch.snapshots().clear()
        );
    }

    @Test
    void statisticsBatchRejectsNegativeIndex() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new StatisticsBatch(-1, List.of())
        );

        assertDoesNotThrow(
                () -> new StatisticsBatch(0, List.of())
        );
    }

    @Test
    void statisticsBatchRejectsNullListsAndElements() {
        assertThrows(
                NullPointerException.class,
                () -> new StatisticsBatch(0, null)
        );

        List<SimulationSnapshot> snapshots = new ArrayList<>();
        snapshots.add(null);

        assertThrows(
                NullPointerException.class,
                () -> new StatisticsBatch(1, snapshots)
        );
    }
}
