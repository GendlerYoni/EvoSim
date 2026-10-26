
package io.github.gendleryoni.evosim;

public class SimulationConfig {

    public static final int MAX_INITIAL_HERBIVORES = 1000;
    public static final int MAX_INITIAL_PREDATORS = 1000;
    public static final int MAX_INITIAL_FOOD = 5000;
    public static final int MAX_FOOD_SPAWN_PER_TICK = 100;

    private final Genome initialGenome;
    private final int initialHerbivores;
    private final int initialPredators;
    private final int initialFood;
    private final long seed;
    private final int foodSpawnMaxPerTick;

    public SimulationConfig(
            Genome initialGenome,
            int initialHerbivores,
            int initialPredators,
            int initialFood,
            long seed,
            int foodSpawnMaxPerTick
    ) {
        if (initialGenome == null) {
            throw new IllegalArgumentException(
                    "Initial genome cannot be null"
            );
        }

        if (initialHerbivores < 0
                || initialHerbivores > MAX_INITIAL_HERBIVORES) {
            throw new IllegalArgumentException(
                    "Initial herbivore count must be between 0 and "
                            + MAX_INITIAL_HERBIVORES
            );
        }

        if (initialPredators < 0
                || initialPredators > MAX_INITIAL_PREDATORS) {
            throw new IllegalArgumentException(
                    "Initial predator count must be between 0 and "
                            + MAX_INITIAL_PREDATORS
            );
        }

        if (initialFood < 0 || initialFood > MAX_INITIAL_FOOD) {
            throw new IllegalArgumentException(
                    "Initial food count must be between 0 and "
                            + MAX_INITIAL_FOOD
            );
        }

        if (foodSpawnMaxPerTick < 0
                || foodSpawnMaxPerTick > MAX_FOOD_SPAWN_PER_TICK) {
            throw new IllegalArgumentException(
                    "Food spawn max per tick must be between 0 and "
                            + MAX_FOOD_SPAWN_PER_TICK
            );
        }

        this.initialGenome = initialGenome;
        this.initialHerbivores = initialHerbivores;
        this.initialPredators = initialPredators;
        this.initialFood = initialFood;
        this.seed = seed;
        this.foodSpawnMaxPerTick = foodSpawnMaxPerTick;
    }

    public Genome getInitialGenome() {
        return initialGenome;
    }

    public int getInitialHerbivores() {
        return initialHerbivores;
    }

    public int getInitialPredators() {
        return initialPredators;
    }

    public int getInitialFood() {
        return initialFood;
    }

    public long getSeed() {
        return seed;
    }

    public int getFoodSpawnMaxPerTick() {
        return foodSpawnMaxPerTick;
    }
}
