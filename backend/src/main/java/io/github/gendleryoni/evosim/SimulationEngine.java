package io.github.gendleryoni.evosim;

public class SimulationEngine {
    // Initial V1 timestep; subject to tuning as simulation behavior is tested.
    private static final double TICK_DURATION_SECONDS = 1.0 / 20.0;

    private final SimulationConfig config;
    private final World world;
    private long tickCount;

    public SimulationEngine(SimulationConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("SimulationConfig cannot be null");
        }

        this.config = config;
        this.world = new World(
                config.getWorldWidth(),
                config.getWorldHeight()
        );
        this.tickCount = 0;
    }

    public void tick() {
        tickCount++;
    }

    public long getTickCount() {
        return tickCount;
    }

    public World getWorld() {
        return world;
    }

    public SimulationConfig getConfig() {
        return config;
    }

    public double getSimulationTimeSeconds() {
        return tickCount * TICK_DURATION_SECONDS;
    }
}