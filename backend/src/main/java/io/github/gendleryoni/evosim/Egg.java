package io.github.gendleryoni.evosim;

public class Egg {

    static final double RADIUS = 1.5;

    private final double x;
    private final double y;
    private final Genome genome;
    private final int generation;

    private int remainingHatchTicks;

    public Egg(
            double x,
            double y,
            Genome genome,
            int generation,
            int remainingHatchTicks
    ) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException(
                    "Egg position must be finite"
            );
        }

        if (genome == null) {
            throw new IllegalArgumentException(
                    "Egg genome cannot be null"
            );
        }

        if (generation < 1) {
            throw new IllegalArgumentException(
                    "Egg generation must be at least 1"
            );
        }

        if (remainingHatchTicks <= 0) {
            throw new IllegalArgumentException(
                    "Remaining hatch ticks must be positive"
            );
        }

        this.x = x;
        this.y = y;
        this.genome = genome;
        this.generation = generation;
        this.remainingHatchTicks = remainingHatchTicks;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Genome getGenome() {
        return genome;
    }

    public int getGeneration() {
        return generation;
    }

    public int getRemainingHatchTicks() {
        return remainingHatchTicks;
    }

    public double getRadius() {
        return RADIUS;
    }

    public boolean isReadyToHatch() {
        return remainingHatchTicks == 0;
    }

    void advanceTick() {
        if (remainingHatchTicks > 0) {
            remainingHatchTicks--;
        }
    }
}