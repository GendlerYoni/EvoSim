package io.github.gendleryoni.evosim;

import java.util.List;
import java.util.Random;

public class SimulationEngine {

    // Initial V1 timestep; subject to tuning as simulation behavior is tested.
    private static final double TICK_DURATION_SECONDS = 1.0 / 20.0;

    // Temporary V1 values; replaced by the Energy Economy in a later milestone.
    private static final double FOOD_ENERGY_GAIN = 100.0;
    private static final double BASE_TICK_ENERGY_COST = 1.0;

    private final SimulationConfig config;
    private final World world;
    private final Random random;
    private final HerbivoreBehavior herbivoreBehavior;

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
        this.herbivoreBehavior = new HerbivoreBehavior();

        this.tickCount = 0;
        this.nextCreatureId = 1;
    }

    public void tick() {
        List<Herbivore> herbivores = world.getHerbivores();

        // V1 decision: herbivores are processed in reverse insertion order.
        // Since newly created herbivores are appended to the list, newer
        // creatures receive priority in same-tick interaction conflicts.
        // Revisit this ordering if simulation results show meaningful bias.
        for (int i = herbivores.size() - 1; i >= 0; i--) {
            processHerbivore(herbivores.get(i));
        }

        tickCount++;
    }

    private void processHerbivore(Herbivore herbivore) {
        double senseRadius =
                herbivore.getSenseRadius();

        double contactRadius =
                herbivore.getRadius() + Food.RADIUS;

        double queryRadius =
                Math.max(
                        senseRadius,
                        contactRadius
                );

        List<Food> nearbyFoods =
                world.findFoodWithinRadius(
                        herbivore.getX(),
                        herbivore.getY(),
                        queryRadius
                );

        double contactRadiusSquared =
                contactRadius * contactRadius;

        for (int i = nearbyFoods.size() - 1; i >= 0; i--) {
            Food food = nearbyFoods.get(i);

            double dx =
                    food.getX() - herbivore.getX();

            double dy =
                    food.getY() - herbivore.getY();

            double distanceSquared =
                    dx * dx + dy * dy;

            if (distanceSquared <= contactRadiusSquared) {
                world.removeFood(food);

                herbivore.addEnergy(
                        FOOD_ENERGY_GAIN
                );

                nearbyFoods.remove(i);
            }
        }

        if (herbivore.isDead()) {
            world.removeHerbivore(herbivore);
            return;
        }

        // nearbyFoods now contains only sensed, non-eaten food.
        Vector2D desiredDirection =
                herbivoreBehavior.chooseDirection(
                        herbivore,
                        nearbyFoods,
                        random
                );

        double movementDistance =
                herbivore.getMovementDistance(
                        TICK_DURATION_SECONDS
                );

        boolean hitWall =
                world.moveCreature(
                        herbivore,
                        desiredDirection,
                        movementDistance
                );

        if (hitWall) {
            Direction newExplorationDirection =
                    chooseAlternativeDirection(
                            herbivore.getExplorationDirection()
                    );

            herbivore.changeExplorationDirection(
                    newExplorationDirection
            );
        }

        herbivore.consumeEnergy(
                BASE_TICK_ENERGY_COST
        );
    }

    private Direction chooseAlternativeDirection(
            Direction currentDirection
    ) {
        Direction[] directions = Direction.values();

        int alternativeIndex =
                random.nextInt(directions.length - 1);

        for (Direction direction : directions) {
            if (direction == currentDirection) {
                continue;
            }

            if (alternativeIndex == 0) {
                return direction;
            }

            alternativeIndex--;
        }

        throw new IllegalStateException(
                "Failed to select alternative direction"
        );
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