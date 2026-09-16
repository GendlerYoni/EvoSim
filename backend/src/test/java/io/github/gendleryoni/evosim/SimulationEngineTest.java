package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimulationEngineTest {

    private SimulationConfig createDefaultConfig() {
        Genome initialGenome = new Genome(
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                180.0
        );

        return new SimulationConfig(
                1000.0,
                800.0,
                initialGenome
        );
    }

    @Test
    void startsAtTickZero() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        assertEquals(0, engine.getTickCount());
    }

    @Test
    void tickAdvancesTickCount() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        engine.tick();

        assertEquals(1, engine.getTickCount());
    }

    @Test
    void multipleTicksAdvanceConsistently() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        for (int i = 0; i < 10_000; i++) {
            engine.tick();
        }

        assertEquals(10_000, engine.getTickCount());
    }

    @Test
    void createsWorldFromConfig() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        assertEquals(1000.0, engine.getWorld().getWidth());
        assertEquals(800.0, engine.getWorld().getHeight());
    }

    @Test
    void rejectsNullConfig() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationEngine(null)
        );
    }

    @Test
    void simulationTimeAdvancesWithTicks() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        for (int i = 0; i < 20; i++) {
            engine.tick();
        }

        assertEquals(1.0, engine.getSimulationTimeSeconds(), 0.000001);
    }
}