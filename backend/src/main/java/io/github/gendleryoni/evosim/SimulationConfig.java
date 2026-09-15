package io.github.gendleryoni.evosim;

public class SimulationConfig {
    private final double worldWidth;
    private final double worldHeight;

    public SimulationConfig(double worldWidth, double worldHeight) {
        if (worldWidth <= 0 || worldHeight <= 0) {
            throw new IllegalArgumentException("World dimensions must be positive");
        }

        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    public double getWorldWidth() {
        return worldWidth;
    }

    public double getWorldHeight() {
        return worldHeight;
    }
}