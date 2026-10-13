
package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimulationConfigTest {

    private Genome createGenome() {
        return new Genome(
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                180.0
        );
    }

    private SimulationConfig createConfig(
            int herbivores,
            int predators,
            int food,
            int foodSpawnMaxPerTick
    ) {
        return new SimulationConfig(
                createGenome(),
                herbivores,
                predators,
                food,
                12345L,
                foodSpawnMaxPerTick
        );
    }

    @Test
    void storesConfiguredValues() {
        Genome genome = createGenome();

        SimulationConfig config = new SimulationConfig(
                genome,
                20,
                5,
                100,
                12345L,
                10
        );

        assertAll(
                () -> assertSame(
                        genome,
                        config.getInitialGenome()
                ),
                () -> assertEquals(
                        20,
                        config.getInitialHerbivores()
                ),
                () -> assertEquals(
                        5,
                        config.getInitialPredators()
                ),
                () -> assertEquals(
                        100,
                        config.getInitialFood()
                ),
                () -> assertEquals(
                        12345L,
                        config.getSeed()
                ),
                () -> assertEquals(
                        10,
                        config.getFoodSpawnMaxPerTick()
                )
        );
    }

    @Test
    void allowsZeroInitialEntitiesAndFoodSpawning() {
        SimulationConfig config = createConfig(
                0, 0, 0, 0
        );

        assertAll(
                () -> assertEquals(
                        0,
                        config.getInitialHerbivores()
                ),
                () -> assertEquals(
                        0,
                        config.getInitialPredators()
                ),
                () -> assertEquals(
                        0,
                        config.getInitialFood()
                ),
                () -> assertEquals(
                        0,
                        config.getFoodSpawnMaxPerTick()
                )
        );
    }

    @Test
    void allowsMaximumInitialPopulations() {
        SimulationConfig config = createConfig(
                1000, 1000, 0, 0
        );

        assertEquals(
                1000,
                config.getInitialHerbivores()
        );

        assertEquals(
                1000,
                config.getInitialPredators()
        );
    }

    @Test
    void rejectsNegativeInitialHerbivoreCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createConfig(-1, 0, 0, 0)
        );
    }

    @Test
    void rejectsInitialHerbivoresAboveMaximum() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createConfig(1001, 0, 0, 0)
        );
    }

    @Test
    void rejectsNegativeInitialPredatorCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createConfig(0, -1, 0, 0)
        );
    }

    @Test
    void rejectsInitialPredatorsAboveMaximum() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createConfig(0, 1001, 0, 0)
        );
    }

    @Test
    void rejectsNegativeInitialFoodCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createConfig(0, 0, -1, 0)
        );
    }

    @Test
    void rejectsNullInitialGenome() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationConfig(
                        null,
                        0,
                        0,
                        0,
                        12345L,
                        0
                )
        );
    }

    @Test
    void allowsMaximumFoodSpawnPerTick() {
        SimulationConfig config = createConfig(
                0, 0, 0, 100
        );

        assertEquals(
                100,
                config.getFoodSpawnMaxPerTick()
        );
    }

    @Test
    void rejectsNegativeFoodSpawnMaxPerTick() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createConfig(0, 0, 0, -1)
        );
    }

    @Test
    void rejectsFoodSpawnMaxPerTickAboveMaximum() {
        assertThrows(
                IllegalArgumentException.class,
                () -> createConfig(0, 0, 0, 101)
        );
    }

    @Test
    void allowsAnyLongSeed() {
        SimulationConfig minSeedConfig = new SimulationConfig(
                createGenome(),
                0,
                0,
                0,
                Long.MIN_VALUE,
                0
        );

        SimulationConfig maxSeedConfig = new SimulationConfig(
                createGenome(),
                0,
                0,
                0,
                Long.MAX_VALUE,
                0
        );

        assertEquals(
                Long.MIN_VALUE,
                minSeedConfig.getSeed()
        );

        assertEquals(
                Long.MAX_VALUE,
                maxSeedConfig.getSeed()
        );
    }
}
