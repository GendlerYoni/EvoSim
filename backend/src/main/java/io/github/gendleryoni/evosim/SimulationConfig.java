package io.github.gendleryoni.evosim;

public class SimulationConfig {

    private final double worldWidth;
    private final double worldHeight;
    private final Genome initialGenome;
    private final int initialHerbivores;
    private final int initialFood;
    private final long seed;
    private final double mutationStrength;
    private final int foodSpawnMaxPerTick;
    private final int foodCap;

    public SimulationConfig(
            double worldWidth,
            double worldHeight,
            Genome initialGenome,
            int initialHerbivores,
            int initialFood,
            long seed,
            double mutationStrength,
            int foodSpawnMaxPerTick,
            int foodCap
    ) {
        if (!Double.isFinite(worldWidth)
                || !Double.isFinite(worldHeight)
                || worldWidth <= 0
                || worldHeight <= 0) {
            throw new IllegalArgumentException(
                    "World dimensions must be positive and finite"
            );
        }

        if (initialGenome == null) {
            throw new IllegalArgumentException(
                    "Initial genome cannot be null"
            );
        }

        if (initialHerbivores < 0) {
            throw new IllegalArgumentException(
                    "Initial herbivore count cannot be negative"
            );
        }

        if (initialFood < 0) {
            throw new IllegalArgumentException(
                    "Initial food count cannot be negative"
            );
        }

        if (!Double.isFinite(mutationStrength)
                || mutationStrength < 0) {
            throw new IllegalArgumentException(
                    "Mutation strength must be non-negative and finite"
            );
        }

        if (foodSpawnMaxPerTick < 0) {
            throw new IllegalArgumentException(
                    "Food spawn max per tick cannot be negative"
            );
        }

        if (foodCap < 0) {
            throw new IllegalArgumentException(
                    "Food cap cannot be negative"
            );
        }

        if (initialFood > foodCap) {
            throw new IllegalArgumentException(
                    "Initial food count cannot exceed food cap"
            );
        }

        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.initialGenome = initialGenome;
        this.initialHerbivores = initialHerbivores;
        this.initialFood = initialFood;
        this.seed = seed;
        this.mutationStrength = mutationStrength;
        this.foodSpawnMaxPerTick = foodSpawnMaxPerTick;
        this.foodCap = foodCap;
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

    public int getInitialHerbivores() {
        return initialHerbivores;
    }

    public int getInitialFood() {
        return initialFood;
    }

    public long getSeed() {
        return seed;
    }

    public double getMutationStrength() {
        return mutationStrength;
    }

    public int getFoodSpawnMaxPerTick() {
        return foodSpawnMaxPerTick;
    }

    public int getFoodCap() {
        return foodCap;
    }
}