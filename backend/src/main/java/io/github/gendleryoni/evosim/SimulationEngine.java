package io.github.gendleryoni.evosim;

import java.util.Random;

public class SimulationEngine {

    // Initial V1 timestep; subject to tuning as simulation behavior is tested.
    private static final double TICK_DURATION_SECONDS = 1.0 / 20.0;

    private final SimulationConfig config;
    private final World world;
    private final Random random;

    private long tickCount;
    private int nextCreatureId;

    public SimulationEngine(SimulationConfig config) {
        if (config == null) {
            throw new IllegalArgumentException(
                    "SimulationConfig cannot be null"
            );
        }

        this.config = config;

        this.world = new World(
                config.getWorldWidth(),
                config.getWorldHeight()
        );

        this.random = new Random(config.getSeed());

        this.tickCount = 0;
        this.nextCreatureId = 1;
    }

    public void tick() {
        tickCount++;
    }

    Herbivore createHerbivore(
            double x,
            double y,
            double energy,
            int generation,
            Genome genome,
            Direction explorationDirection
    ) {
        Herbivore herbivore = new Herbivore(
                nextCreatureId,
                x,
                y,
                energy,
                generation,
                genome,
                explorationDirection
        );

        world.addHerbivore(herbivore);

        nextCreatureId++;

        return herbivore;
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