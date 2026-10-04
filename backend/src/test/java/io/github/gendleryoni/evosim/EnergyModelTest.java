package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnergyModelTest {

    private static final double DELTA = 1e-9;

    private final EnergyModel energyModel = new EnergyModel();

    @Test
    void initialEnergyIs100() {
        assertEquals(
                100.0,
                energyModel.initialEnergy(),
                DELTA
        );
    }

    @Test
    void foodEnergyGainIs100() {
        assertEquals(
                100.0,
                energyModel.foodEnergyGain(),
                DELTA
        );
    }

    @Test
    void baselineGenomeCostsOneEnergyPerTick() {
        Genome genome = createGenome(
                1.0,
                1.0,
                1.0
        );

        assertEquals(
                1.0,
                energyModel.calculateTickCost(genome),
                DELTA
        );
    }

    @Test
    void senseUsesSquareRootCostScaling() {
        Genome genome = createGenome(
                1.0,
                1.0,
                4.0
        );

        double expectedCost =
                (1.0 + 1.0 + 2.0) / 3.0;

        assertEquals(
                expectedCost,
                energyModel.calculateTickCost(genome),
                DELTA
        );
    }

    @Test
    void speedUsesLinearCostScaling() {
        Genome genome = createGenome(
                4.0,
                1.0,
                1.0
        );

        double expectedCost =
                (4.0 + 1.0 + 1.0) / 3.0;

        assertEquals(
                expectedCost,
                energyModel.calculateTickCost(genome),
                DELTA
        );
    }

    @Test
    void sizeUsesPowerOnePointFiveCostScaling() {
        Genome genome = createGenome(
                1.0,
                4.0,
                1.0
        );

        double expectedCost =
                (1.0 + 8.0 + 1.0) / 3.0;

        assertEquals(
                expectedCost,
                energyModel.calculateTickCost(genome),
                DELTA
        );
    }

    @Test
    void minimumEnergyTraitsProduceExpectedTickCost() {
        Genome genome = createGenome(
                0.25,
                0.25,
                0.25
        );

        double expectedCost =
                (0.25 + 0.125 + 0.5) / 3.0;

        assertEquals(
                expectedCost,
                energyModel.calculateTickCost(genome),
                DELTA
        );
    }

    @Test
    void maximumEnergyTraitsProduceExpectedTickCost() {
        Genome genome = createGenome(
                4.0,
                4.0,
                4.0
        );

        double expectedCost =
                (4.0 + 8.0 + 2.0) / 3.0;

        assertEquals(
                expectedCost,
                energyModel.calculateTickCost(genome),
                DELTA
        );
    }

    private Genome createGenome(
            double speed,
            double size,
            double senseRadius
    ) {
        return new Genome(
                speed,
                size,
                senseRadius,
                1.0,
                1.0,
                180.0
        );
    }
}