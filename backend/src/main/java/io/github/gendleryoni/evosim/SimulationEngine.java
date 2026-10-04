package io.github.gendleryoni.evosim;

import java.util.List;
import java.util.Random;

public class SimulationEngine {

    // Initial V1 timestep; subject to tuning as simulation behavior is tested.
    private static final double TICK_DURATION_SECONDS = 1.0 / 20.0;

    private final SimulationConfig config;
    private final World world;
    private final Random random;
    private final HerbivoreBehavior herbivoreBehavior;
    private final EnergyModel energyModel;

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
        this.energyModel = new EnergyModel();

        this.tickCount = 0;
        this.nextCreatureId = 1;

        initializeSimulation();
    }

    private void initializeSimulation() {
        createInitialHerbivores();
        createInitialFood();
    }

    private void createInitialHerbivores() {
        Genome genome = config.getInitialGenome();
        double radius = Creature.calculateRadius(genome);

        for (int i = 0; i < config.getInitialHerbivores(); i++) {
            double x = randomCoordinate(
                    radius,
                    world.getWidth()
            );

            double y = randomCoordinate(
                    radius,
                    world.getHeight()
            );

            Direction explorationDirection =
                    randomDirection();

            createHerbivore(
                    x,
                    y,
                    energyModel.initialEnergy(),
                    1,
                    genome,
                    explorationDirection
            );
        }
    }

    private void createInitialFood() {
        for (int i = 0; i < config.getInitialFood(); i++) {
            double x = randomCoordinate(
                    Food.RADIUS,
                    world.getWidth()
            );

            double y = randomCoordinate(
                    Food.RADIUS,
                    world.getHeight()
            );

            world.addFood(x, y);
        }
    }

    private double randomCoordinate(
            double radius,
            double worldSize
    ) {
        double min = radius;
        double max = worldSize - radius;

        if (max < min) {
            throw new IllegalStateException(
                    "World is too small for entity radius"
            );
        }

        return min
                + random.nextDouble()
                * (max - min);
    }

    private Direction randomDirection() {
        Direction[] directions = Direction.values();

        return directions[
                random.nextInt(directions.length)
                ];
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
                        energyModel.foodEnergyGain()
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
                energyModel.calculateTickCost(
                        herbivore.getGenome()
                )
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

    public double getTickDurationSeconds() {
        return TICK_DURATION_SECONDS;
    }
}