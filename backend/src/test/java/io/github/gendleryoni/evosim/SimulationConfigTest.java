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

    @Test
    void storesConfiguredValues() {
        Genome genome = createGenome();

        SimulationConfig config =
                new SimulationConfig(
                        1000.0,
                        800.0,
                        genome,
                        20,
                        100,
                        12345L,
                        0.05,
                        10,
                        500
                );

        assertEquals(
                1000.0,
                config.getWorldWidth()
        );

        assertEquals(
                800.0,
                config.getWorldHeight()
        );

        assertSame(
                genome,
                config.getInitialGenome()
        );

        assertEquals(
                20,
                config.getInitialHerbivores()
        );

        assertEquals(
                100,
                config.getInitialFood()
        );

        assertEquals(
                12345L,
                config.getSeed()
        );

        assertEquals(
                0.05,
                config.getMutationStrength()
        );

        assertEquals(
                10,
                config.getFoodSpawnMaxPerTick()
        );

        assertEquals(
                500,
                config.getFoodCap()
        );
    }

    @Test
    void allowsZeroInitialEntitiesAndFoodSpawning() {
        SimulationConfig config =
                new SimulationConfig(
                        1000.0,
                        800.0,
                        createGenome(),
                        0,
                        0,
                        12345L,
                        0.0,
                        0,
                        0
                );

        assertEquals(
                0,
                config.getInitialHerbivores()
        );

        assertEquals(
                0,
                config.getInitialFood()
        );

        assertEquals(
                0.0,
                config.getMutationStrength()
        );

        assertEquals(
                0,
                config.getFoodSpawnMaxPerTick()
        );

        assertEquals(
                0,
                config.getFoodCap()
        );
    }

    @Test
    void rejectsNegativeInitialHerbivoreCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationConfig(
                        1000.0,
                        800.0,
                        createGenome(),
                        -1,
                        0,
                        12345L,
                        0.0,
                        0,
                        500
                )
        );
    }

    @Test
    void rejectsNegativeInitialFoodCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationConfig(
                        1000.0,
                        800.0,
                        createGenome(),
                        0,
                        -1,
                        12345L,
                        0.0,
                        0,
                        500
                )
        );
    }

    @Test
    void rejectsNonPositiveWorldDimensions() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationConfig(
                        0.0,
                        800.0,
                        createGenome(),
                        0,
                        0,
                        12345L,
                        0.0,
                        0,
                        500
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationConfig(
                        1000.0,
                        0.0,
                        createGenome(),
                        0,
                        0,
                        12345L,
                        0.0,
                        0,
                        500
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationConfig(
                        -1.0,
                        800.0,
                        createGenome(),
                        0,
                        0,
                        12345L,
                        0.0,
                        0,
                        500
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationConfig(
                        1000.0,
                        -1.0,
                        createGenome(),
                        0,
                        0,
                        12345L,
                        0.0,
                        0,
                        500
                )
        );
    }

    @Test
    void rejectsNonFiniteWorldDimensions() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationConfig(
                        Double.NaN,
                        800.0,
                        createGenome(),
                        0,
                        0,
                        12345L,
                        0.0,
                        0,
                        500
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationConfig(
                        1000.0,
                        Double.POSITIVE_INFINITY,
                        createGenome(),
                        0,
                        0,
                        12345L,
                        0.0,
                        0,
                        500
                )
        );
    }

    @Test
    void rejectsNullInitialGenome() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationConfig(
                        1000.0,
                        800.0,
                        null,
                        0,
                        0,
                        12345L,
                        0.0,
                        0,
                        500
                )
        );
    }

    @Test
    void rejectsNegativeMutationStrength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationConfig(
                        1000.0,
                        800.0,
                        createGenome(),
                        0,
                        0,
                        12345L,
                        -0.01,
                        0,
                        500
                )
        );
    }

    @Test
    void rejectsNonFiniteMutationStrength() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationConfig(
                        1000.0,
                        800.0,
                        createGenome(),
                        0,
                        0,
                        12345L,
                        Double.NaN,
                        0,
                        500
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationConfig(
                        1000.0,
                        800.0,
                        createGenome(),
                        0,
                        0,
                        12345L,
                        Double.POSITIVE_INFINITY,
                        0,
                        500
                )
        );
    }

    @Test
    void rejectsNegativeFoodSpawnMaxPerTick() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationConfig(
                        1000.0,
                        800.0,
                        createGenome(),
                        0,
                        0,
                        12345L,
                        0.0,
                        -1,
                        500
                )
        );
    }

    @Test
    void rejectsNegativeFoodCap() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationConfig(
                        1000.0,
                        800.0,
                        createGenome(),
                        0,
                        0,
                        12345L,
                        0.0,
                        0,
                        -1
                )
        );
    }

    @Test
    void rejectsInitialFoodAboveFoodCap() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationConfig(
                        1000.0,
                        800.0,
                        createGenome(),
                        0,
                        101,
                        12345L,
                        0.0,
                        10,
                        100
                )
        );
    }

    @Test
    void allowsInitialFoodEqualToFoodCap() {
        assertDoesNotThrow(
                () -> new SimulationConfig(
                        1000.0,
                        800.0,
                        createGenome(),
                        0,
                        100,
                        12345L,
                        0.0,
                        10,
                        100
                )
        );
    }
}