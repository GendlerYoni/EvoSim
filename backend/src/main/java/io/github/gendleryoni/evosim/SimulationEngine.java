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
        for (int i = 0; i < config.getInitialHerbivores(); i++) {
            Genome genome = createInitialHerbivoreGenome();

            double boundaryMargin =
                    Creature.calculateMaximumRadius();

            double x = randomCoordinate(
                    boundaryMargin,
                    world.getWidth()
            );

            double y = randomCoordinate(
                    boundaryMargin,
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
                    Creature.calculateMaximumRadius(),
                    world.getWidth()
            );

            double y = randomCoordinate(
                    Creature.calculateMaximumRadius(),
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

        int herbivoreEggsAtTickStart =
                world.getHerbivoreEggs().size();

        // V1 decision: herbivores are processed in reverse insertion order.
        // Since newly created herbivores are appended to the list, newer
        // creatures receive priority in same-tick interaction conflicts.
        // Revisit this ordering if simulation results show meaningful bias.
        for (int i = herbivores.size() - 1; i >= 0; i--) {
            processHerbivore(herbivores.get(i));
        }

        processHerbivoreEggs(herbivoreEggsAtTickStart);

        spawnFoodForTick();

        tickCount++;
    }

    private void processHerbivore(Herbivore herbivore) {

        herbivore.advanceReproductionCooldown();

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

        if (herbivore.canReproduce()) {
            reproduceHerbivore(herbivore);
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


    private void reproduceHerbivore(Herbivore herbivore) {
        Genome parentGenome = herbivore.getGenome();

        Genome childGenome =
                parentGenome.mutate(
                        config.getMutationStrength(),
                        random
                );

        world.addHerbivoreEgg(
                herbivore.getX(),
                herbivore.getY(),
                childGenome,
                herbivore.getGeneration() + 1,
                Creature.calculateEggHatchTicks(childGenome)
        );

        herbivore.consumeEnergy(
                energyModel.reproductionEnergyCost()
        );

        herbivore.startReproductionCooldown();
    }

    private void processHerbivoreEggs(int eggsAtTickStart) {
        List<Egg> eggs = world.getHerbivoreEggs();

        for (int i = eggsAtTickStart - 1; i >= 0; i--) {
            Egg egg = eggs.get(i);

            egg.advanceTick();

            if (!egg.isReadyToHatch()) {
                continue;
            }

            createHerbivore(
                    egg.getX(),
                    egg.getY(),
                    energyModel.hatchEnergy(),
                    egg.getGeneration(),
                    egg.getGenome(),
                    randomDirection()
            );

            world.removeHerbivoreEgg(egg);
        }
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

    private Genome createInitialHerbivoreGenome() {
        Genome mutatedGenome =
                config.getInitialGenome().mutate(
                        config.getMutationStrength(),
                        random
                );

        double randomHue = random.nextDouble() * 360.0;

        return new Genome(
                mutatedGenome.getSpeed(),
                mutatedGenome.getSize(),
                mutatedGenome.getSenseRadius(),
                mutatedGenome.getReproductionThreshold(),
                mutatedGenome.getEggHatchTime(),
                randomHue
        );
    }

    private void spawnFoodForTick() {
        int remainingCapacity =
                config.getFoodCap() - world.getFoods().size();

        if (remainingCapacity <= 0
                || config.getFoodSpawnMaxPerTick() == 0) {
            return;
        }

        int foodToSpawn =
                random.nextInt(
                        config.getFoodSpawnMaxPerTick() + 1
                );

        foodToSpawn =
                Math.min(
                        foodToSpawn,
                        remainingCapacity
                );

        for (int i = 0; i < foodToSpawn; i++) {
            double x = randomCoordinate(
                    Creature.calculateMaximumRadius(),
                    world.getWidth()
            );

            double y = randomCoordinate(
                    Creature.calculateMaximumRadius(),
                    world.getHeight()
            );

            world.addFood(x, y);
        }
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