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
                        0.0
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
    }

    @Test
    void allowsZeroInitialEntities() {
        SimulationConfig config =
                new SimulationConfig(
                        1000.0,
                        800.0,
                        createGenome(),
                        0,
                        0,
                        12345L,
                        0.0
                );

        assertEquals(
                0,
                config.getInitialHerbivores()
        );

        assertEquals(
                0,
                config.getInitialFood()
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
                        0.0
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
                        0.0
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
                        0.0
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
                        0.0
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
                        0.0
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
                        0.0
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
                        0.0
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
                        0.0
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
                        0.0
                )
        );
    }
}