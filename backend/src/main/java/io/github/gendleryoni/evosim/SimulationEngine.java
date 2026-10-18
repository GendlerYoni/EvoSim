
package io.github.gendleryoni.evosim;

import io.github.gendleryoni.evosim.statistics.StatisticsCalculator;
import io.github.gendleryoni.evosim.statistics.SimulationSnapshot;
import io.github.gendleryoni.evosim.statistics.StatisticsBatch;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;


public class SimulationEngine {

    private static final double WORLD_WIDTH = 1000.0;
    private static final double WORLD_HEIGHT = 800.0;
    private static final double MUTATION_STRENGTH = 0.05;
    private static final int FOOD_CAP = 5000;
    private static final int MAX_TOTAL_POPULATION = 10_000;

    // Initial V1 timestep; subject to tuning as simulation behavior is tested.
    private static final double TICK_DURATION_SECONDS = 1.0 / 20.0;
    private static final double MIN_PREDATOR_TO_PREY_SIZE_RATIO = 0.8;

    private static final double PREDATOR_SPEED_MULTIPLIER = 1.2;
    private static final double PREDATOR_ENERGY_COST_MULTIPLIER = 1.0;

    private static final double EGG_SPECIES_MUTATION_CHANCE = 0.05;

    private static final int STATISTICS_INTERVAL_TICKS = 50;
    private static final int STATISTICS_HISTORY_TICKS = 10_000;
    private static final int STATISTICS_HISTORY_CAPACITY =
            STATISTICS_HISTORY_TICKS / STATISTICS_INTERVAL_TICKS + 1;

    private final SimulationConfig config;
    private final World world;
    private final Random random;
    private final HerbivoreBehavior herbivoreBehavior;
    private final EnergyModel energyModel;
    private final PredatorBehavior predatorBehavior;

    private long tickCount;
    private int nextCreatureId;

    private final StatisticsCalculator statisticsCalculator =
            new StatisticsCalculator();

    private final SimulationSnapshot[] statisticsHistory =
            new SimulationSnapshot[STATISTICS_HISTORY_CAPACITY];

    private final Object statisticsLock = new Object();

    private int nextSampleIndex = 0;

    public SimulationEngine(SimulationConfig config) {
        if (config == null) {
            throw new IllegalArgumentException(
                    "SimulationConfig cannot be null"
            );
        }

        if (config.getInitialFood() > FOOD_CAP) {
            throw new IllegalArgumentException(
                    "Initial food count cannot exceed " + FOOD_CAP
            );
        }

        this.config = config;

        this.world = new World(WORLD_WIDTH, WORLD_HEIGHT);

        this.random = new Random(config.getSeed());
        this.herbivoreBehavior = new HerbivoreBehavior();
        this.predatorBehavior = new PredatorBehavior();
        this.energyModel = new EnergyModel();

        this.tickCount = 0;
        this.nextCreatureId = 1;

        initializeSimulation();
    }

    public StatisticsBatch getStatisticsFromIndex(int fromIndex) {
        synchronized (statisticsLock) {

            if (fromIndex < 0 || fromIndex > nextSampleIndex) {
                throw new IndexOutOfBoundsException(
                        "Invalid statistics sample index: " + fromIndex
                );
            }

            int oldestAvailableIndex = Math.max(
                    0,
                    nextSampleIndex - STATISTICS_HISTORY_CAPACITY
            );

            int startIndex = Math.max(
                    fromIndex,
                    oldestAvailableIndex
            );

            List<SimulationSnapshot> snapshots = new ArrayList<>();

            for (int i = startIndex; i < nextSampleIndex; i++) {
                int arrayIndex = i % STATISTICS_HISTORY_CAPACITY;
                snapshots.add(statisticsHistory[arrayIndex]);
            }

            return new StatisticsBatch(nextSampleIndex, snapshots);
        }
    }

    private void initializeSimulation() {
        createInitialHerbivores();
        createInitialPredators();
        createInitialFood();
    }

    private void createInitialHerbivores() {
        for (int i = 0; i < config.getInitialHerbivores(); i++) {
            Genome genome = createInitialGenome();

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

    private void createInitialPredators() {
        for (int i = 0; i < config.getInitialPredators(); i++) {
            Genome genome =
                    createInitialGenome();

            double boundaryMargin =
                    Creature.calculateMaximumRadius();

            double x =
                    randomCoordinate(
                            boundaryMargin,
                            world.getWidth()
                    );

            double y =
                    randomCoordinate(
                            boundaryMargin,
                            world.getHeight()
                    );

            Direction explorationDirection =
                    randomDirection();

            createPredator(
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
        int herbivoreEggsAtTickStart =
                world.getHerbivoreEggs().size();

        int predatorEggsAtTickStart =
                world.getPredatorEggs().size();

        if (tickCount % STATISTICS_INTERVAL_TICKS == 0) {
            saveSnapshot();
        }

        resolvePredation();

        List<Herbivore> herbivores =
                world.getHerbivores();

        // V1 decision: creatures are processed in reverse insertion order.
        // Since newly created creatures are appended to their species list,
        // newer creatures receive priority in same-tick interaction conflicts.
        // Herbivores are processed before predators to give prey an intentional
        // escape advantage. Revisit if simulation results show meaningful bias.
        for (int i = herbivores.size() - 1; i >= 0; i--) {
            processHerbivore(
                    herbivores.get(i)
            );
        }

        List<Predator> predators =
                world.getPredators();

        for (int i = predators.size() - 1; i >= 0; i--) {
            processPredator(
                    predators.get(i)
            );
        }

        processHerbivoreEggs(
                herbivoreEggsAtTickStart
        );

        processPredatorEggs(
                predatorEggsAtTickStart
        );

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

        List<Predator> nearbyPredators =
                world.findPredatorsWithinRadius(
                        herbivore.getX(),
                        herbivore.getY(),
                        senseRadius
                );

        nearbyPredators.removeIf(
                predator ->
                        !canPredatorEatHerbivore(
                                predator,
                                herbivore
                        )
        );

        // nearbyFoods now contains only sensed, non-eaten food.
        // nearbyPredators now contains only predators that can
        // legally eat this herbivore.
        Vector2D desiredDirection =
                herbivoreBehavior.chooseDirection(
                        herbivore,
                        nearbyFoods,
                        nearbyPredators,
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

    private void processPredator(Predator predator) {

        predator.advanceReproductionCooldown();

        if (predator.isDead()) {
            world.removePredator(predator);
            return;
        }

        if (predator.canReproduce()) {
            reproducePredator(predator);
        }

        double senseRadius =
                predator.getSenseRadius();

        List<Herbivore> nearbyHerbivores =
                world.findHerbivoresWithinRadius(
                        predator.getX(),
                        predator.getY(),
                        senseRadius
                );

        nearbyHerbivores.removeIf(
                herbivore ->
                        !canPredatorEatHerbivore(
                                predator,
                                herbivore
                        )
        );

        Vector2D desiredDirection =
                predatorBehavior.chooseDirection(
                        predator,
                        nearbyHerbivores,
                        random
                );

        double movementDistance =
                predator.getMovementDistance(TICK_DURATION_SECONDS)
                        * PREDATOR_SPEED_MULTIPLIER;

        boolean hitWall =
                world.moveCreature(
                        predator,
                        desiredDirection,
                        movementDistance
                );

        if (hitWall) {
            Direction newExplorationDirection =
                    chooseAlternativeDirection(
                            predator.getExplorationDirection()
                    );

            predator.changeExplorationDirection(
                    newExplorationDirection
            );
        }

        double energyCost =
                energyModel.calculateTickCost(predator.getGenome())
                        * PREDATOR_ENERGY_COST_MULTIPLIER;

        predator.consumeEnergy(energyCost);
    }

    private boolean hasPopulationCapacity() {
        int totalPopulation =
                world.getHerbivores().size()
                        + world.getPredators().size()
                        + world.getHerbivoreEggs().size()
                        + world.getPredatorEggs().size();

        return totalPopulation < MAX_TOTAL_POPULATION;
    }

    private void reproduceHerbivore(Herbivore herbivore) {
        if (!hasPopulationCapacity()) {
            return;
        }

        Genome childGenome = herbivore.getGenome().mutate(
                MUTATION_STRENGTH,
                random
        );

        int childGeneration = herbivore.getGeneration() + 1;
        int hatchTicks = Creature.calculateEggHatchTicks(childGenome);

        boolean speciesMutation =
                random.nextDouble() < EGG_SPECIES_MUTATION_CHANCE;

        if (speciesMutation) {
            world.addPredatorEgg(
                    herbivore.getX(),
                    herbivore.getY(),
                    childGenome,
                    childGeneration,
                    hatchTicks
            );
        } else {
            world.addHerbivoreEgg(
                    herbivore.getX(),
                    herbivore.getY(),
                    childGenome,
                    childGeneration,
                    hatchTicks
            );
        }

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

            world.removeHerbivoreEgg(egg);

            createHerbivore(
                    egg.getX(),
                    egg.getY(),
                    energyModel.hatchEnergy(),
                    egg.getGeneration(),
                    egg.getGenome(),
                    randomDirection()
            );
        }
    }

    private void reproducePredator(Predator predator) {
        if (!hasPopulationCapacity()) {
            return;
        }

        Genome childGenome = predator.getGenome().mutate(
                MUTATION_STRENGTH,
                random
        );

        int childGeneration = predator.getGeneration() + 1;
        int hatchTicks = Creature.calculateEggHatchTicks(childGenome);

        boolean speciesMutation =
                random.nextDouble() < EGG_SPECIES_MUTATION_CHANCE;

        if (speciesMutation) {
            world.addHerbivoreEgg(
                    predator.getX(),
                    predator.getY(),
                    childGenome,
                    childGeneration,
                    hatchTicks
            );
        } else {
            world.addPredatorEgg(
                    predator.getX(),
                    predator.getY(),
                    childGenome,
                    childGeneration,
                    hatchTicks
            );
        }

        predator.consumeEnergy(
                energyModel.reproductionEnergyCost()
        );

        predator.startReproductionCooldown();
    }

    private void processPredatorEggs(int eggsAtTickStart) {
        List<Egg> eggs =
                world.getPredatorEggs();

        for (int i = eggsAtTickStart - 1; i >= 0; i--) {
            Egg egg = eggs.get(i);

            egg.advanceTick();

            if (!egg.isReadyToHatch()) {
                continue;
            }

            world.removePredatorEgg(egg);

            createPredator(
                    egg.getX(),
                    egg.getY(),
                    energyModel.hatchEnergy(),
                    egg.getGeneration(),
                    egg.getGenome(),
                    randomDirection()
            );
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

    Predator createPredator(
            double x,
            double y,
            double energy,
            int generation,
            Genome genome,
            Direction explorationDirection
    ) {
        Predator predator =
                new Predator(
                        nextCreatureId,
                        x,
                        y,
                        energy,
                        generation,
                        genome,
                        explorationDirection
                );

        world.addPredator(predator);

        nextCreatureId++;

        return predator;
    }

    private Genome createInitialGenome() {
        Genome mutatedGenome =
                config.getInitialGenome().mutate(
                        MUTATION_STRENGTH,
                        random
                );

        double randomHue =
                random.nextDouble() * 360.0;

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
                FOOD_CAP - world.getFoods().size();

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

    private boolean canPredatorEatHerbivore(
            Predator predator,
            Herbivore herbivore
    ) {
        return predator.getGenome().getSize()
                >= herbivore.getGenome().getSize()
                * MIN_PREDATOR_TO_PREY_SIZE_RATIO;
    }

    private void resolvePredation() {
        List<Predator> predators =
                world.getPredators();

        // Newest predators receive priority.
        for (int i = predators.size() - 1; i >= 0; i--) {
            Predator predator = predators.get(i);

            double queryRadius =
                    predator.getRadius()
                            + Creature.calculateMaximumRadius();

            List<Herbivore> nearbyHerbivores =
                    world.findHerbivoresWithinRadius(
                            predator.getX(),
                            predator.getY(),
                            queryRadius
                    );

            for (Herbivore herbivore : nearbyHerbivores) {
                if (!canPredatorEatHerbivore(
                        predator,
                        herbivore
                )) {
                    continue;
                }

                double dx =
                        herbivore.getX()
                                - predator.getX();

                double dy =
                        herbivore.getY()
                                - predator.getY();

                double contactRadius =
                        predator.getRadius()
                                + herbivore.getRadius();

                double distanceSquared =
                        dx * dx + dy * dy;

                double contactRadiusSquared =
                        contactRadius * contactRadius;

                if (distanceSquared > contactRadiusSquared) {
                    continue;
                }

                world.removeHerbivore(herbivore);

                predator.addEnergy(
                        energyModel.predationEnergyGain(
                                herbivore.getGenome()
                        )
                );
            }
        }
    }

    private void saveSnapshot() {
        SimulationSnapshot snapshot =
                statisticsCalculator.calculate(world);

        synchronized (statisticsLock) {
            int index =
                    nextSampleIndex % STATISTICS_HISTORY_CAPACITY;

            statisticsHistory[index] = snapshot;

            nextSampleIndex++;
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

    public int getStatisticsIntervalTicks() {
        return STATISTICS_INTERVAL_TICKS;
    }

    public int getStatisticsHistoryCapacity() {
        return STATISTICS_HISTORY_CAPACITY;
    }
}
