package io.github.gendleryoni.evosim;

public class SimulationConfig {
    private final double worldWidth;
    private final double worldHeight;
    private final Genome initialGenome;

    public SimulationConfig(
            double worldWidth,
            double worldHeight,
            Genome initialGenome
    ) {
        if (worldWidth <= 0 || worldHeight <= 0) {
            throw new IllegalArgumentException("World dimensions must be positive");
        }

        if (initialGenome == null) {
            throw new IllegalArgumentException("Initial genome cannot be null");
        }

        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.initialGenome = initialGenome;
    }

    public double getWorldWidth() {
        return worldWidth;
    }

    public double getWorldHeight() {
        return worldHeight;
    }

    public Genome getInitialGenome() {
        return initialGenome;
    }
}