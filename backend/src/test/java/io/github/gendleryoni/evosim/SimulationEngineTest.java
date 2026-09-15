package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimulationEngineTest {

    @Test
    void startsAtTickZero() {
        SimulationConfig config = new SimulationConfig(1000.0, 800.0);
        SimulationEngine engine = new SimulationEngine(config);

        assertEquals(0, engine.getTickCount());
    }

    @Test
    void tickAdvancesTickCount() {
        SimulationConfig config = new SimulationConfig(1000.0, 800.0);
        SimulationEngine engine = new SimulationEngine(config);

        engine.tick();

        assertEquals(1, engine.getTickCount());
    }

    @Test
    void multipleTicksAdvanceConsistently() {
        SimulationConfig config = new SimulationConfig(1000.0, 800.0);
        SimulationEngine engine = new SimulationEngine(config);

        for (int i = 0; i < 10_000; i++) {
            engine.tick();
        }

        assertEquals(10_000, engine.getTickCount());
    }

    @Test
    void createsWorldFromConfig() {
        SimulationConfig config = new SimulationConfig(1000.0, 800.0);
        SimulationEngine engine = new SimulationEngine(config);

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
        SimulationConfig config = new SimulationConfig(1000.0, 800.0);
        SimulationEngine engine = new SimulationEngine(config);

        for (int i = 0; i < 20; i++) {
            engine.tick();
        }

        assertEquals(1.0, engine.getSimulationTimeSeconds(), 0.000001);
    }
}