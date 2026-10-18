package io.github.gendleryoni.evosim;

import io.github.gendleryoni.evosim.statistics.SimulationSnapshot;
import io.github.gendleryoni.evosim.statistics.StatisticsBatch;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SimulationEngineTest {

    private SimulationConfig createDefaultConfig() {
        return createConfig(12345L);
    }

    private SimulationConfig createConfig(long seed) {
        return createConfig(seed, 0, 0);
    }

    private SimulationConfig createConfig(long seed, int initialHerbivores, int initialFood) {
        return createConfig(seed, initialHerbivores, 0, initialFood);
    }

    private SimulationConfig createConfig(long seed, int initialHerbivores,
                                          int initialPredators, int initialFood) {
        return new SimulationConfig(new Genome(1.0, 1.0, 1.0, 1.0, 1.0, 180.0), initialHerbivores,
                initialPredators, initialFood, seed, 0);
    }

    private Genome createGenome(double reproductionThreshold, double eggHatchTime) {
        return new Genome(1.0, 1.0, 1.0, reproductionThreshold, eggHatchTime, 180.0);
    }

    private Herbivore createHerbivore(SimulationEngine engine, double x, double y, double energy,
                                      Direction explorationDirection) {
        return createHerbivore(engine, x, y, energy, engine.getConfig().getInitialGenome(), explorationDirection
        );
    }

    private Herbivore createHerbivore(SimulationEngine engine, double x, double y, double energy, Genome genome,
                                      Direction explorationDirection) {
        return engine.createHerbivore(x, y, energy, 1, genome, explorationDirection);
    }

    private Predator createPredator(SimulationEngine engine, double x, double y, double energy, Genome genome,
                                    Direction explorationDirection) {
        return engine.createPredator(x, y, energy, 1, genome, explorationDirection);
    }

    private void assertTraitsWithinMutationRange(Genome parent, Genome child) {
        double strength = 0.05;
        double epsilon = 1e-9;
        assertAll(() -> assertEquals(parent.getSpeed(), child.getSpeed(), parent.getSpeed() * strength + epsilon),
                () -> assertEquals(parent.getSize(), child.getSize(), parent.getSize() * strength + epsilon),
                () -> assertEquals(parent.getSenseRadius(), child.getSenseRadius(),
                        parent.getSenseRadius() * strength + epsilon),
                () -> assertEquals(parent.getReproductionThreshold(), child.getReproductionThreshold(),
                        parent.getReproductionThreshold() * strength + epsilon),
                () -> assertEquals(parent.getEggHatchTime(), child.getEggHatchTime(),
                        parent.getEggHatchTime() * strength + epsilon));
        assertTrue(child.getHue() >= 0.0 && child.getHue() < 360.0);
    }

    @Test
    void startsAtTickZero() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        assertEquals(0, engine.getTickCount());
    }

    @Test
    void tickAdvancesTickCount() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        engine.tick();

        assertEquals(1, engine.getTickCount());
    }

    @Test
    void multipleTicksAdvanceConsistently() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        for (int i = 0; i < 10_000; i++) {
            engine.tick();
        }

        assertEquals(10_000, engine.getTickCount());
    }

    @Test
    void createsFixedSizeWorld() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        assertEquals(1000.0, engine.getWorld().getWidth());

        assertEquals(800.0, engine.getWorld().getHeight());
    }

    @Test
    void rejectsNullConfig() {
        assertThrows(IllegalArgumentException.class, () -> new SimulationEngine(null));
    }

    @Test
    void simulationTimeAdvancesWithTicks() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        for (int i = 0; i < 20; i++) {
            engine.tick();
        }

        assertEquals(1.0, engine.getSimulationTimeSeconds(), 1e-9);
    }

    @Test
    void createsConfiguredInitialPopulation() {
        SimulationEngine engine = new SimulationEngine(createConfig(12345L, 20, 100));

        assertEquals(20, engine.getWorld().getHerbivores().size());

        assertEquals(100, engine.getWorld().getFoods().size());
    }

    @Test
    void initialHerbivoresUseConfiguredTraitsAndInitialState() {
        SimulationEngine engine = new SimulationEngine(createConfig(12345L, 20, 0));
        Genome baseline = engine.getConfig().getInitialGenome();
        for (Herbivore herbivore : engine.getWorld().getHerbivores()) {
            assertNotSame(baseline, herbivore.getGenome());
            assertTraitsWithinMutationRange(baseline, herbivore.getGenome());
            assertEquals(100.0, herbivore.getEnergy(), 1e-9);
            assertEquals(1, herbivore.getGeneration());
            assertNotNull(herbivore.getExplorationDirection());
        }
    }

    @Test
    void initialHerbivoresAreCreatedInsideWorldBounds() {
        SimulationEngine engine = new SimulationEngine(createConfig(12345L, 100, 0));

        double worldWidth = engine.getWorld().getWidth();

        double worldHeight = engine.getWorld().getHeight();

        for (Herbivore herbivore :
                engine.getWorld().getHerbivores()) {

            double radius = herbivore.getRadius();

            assertTrue(herbivore.getX() >= radius && herbivore.getX() <= worldWidth - radius);

            assertTrue(herbivore.getY() >= radius && herbivore.getY() <= worldHeight - radius);
        }
    }

    @Test
    void initialFoodIsCreatedInsideWorldBounds() {
        SimulationEngine engine = new SimulationEngine(createConfig(12345L, 0, 100));

        double worldWidth = engine.getWorld().getWidth();

        double worldHeight = engine.getWorld().getHeight();

        for (Food food : engine.getWorld().getFoods()) {
            double radius = food.getRadius();

            assertTrue(food.getX() >= radius && food.getX() <= worldWidth - radius);

            assertTrue(food.getY() >= radius && food.getY() <= worldHeight - radius);
        }
    }

    @Test
    void sameSeedProducesSameInitialWorld() {
        SimulationEngine firstEngine = new SimulationEngine(createConfig(98765L, 20, 100));

        SimulationEngine secondEngine = new SimulationEngine(createConfig(98765L, 20, 100));

        List<Herbivore> firstHerbivores = firstEngine.getWorld().getHerbivores();

        List<Herbivore> secondHerbivores = secondEngine.getWorld().getHerbivores();

        assertEquals(firstHerbivores.size(), secondHerbivores.size());

        for (int i = 0; i < firstHerbivores.size(); i++) {
            Herbivore first = firstHerbivores.get(i);

            Herbivore second = secondHerbivores.get(i);

            assertEquals(first.getId(), second.getId());

            assertEquals(first.getX(), second.getX(), 1e-9);

            assertEquals(first.getY(), second.getY(), 1e-9);

            assertEquals(first.getExplorationDirection(), second.getExplorationDirection());
        }

        List<Food> firstFoods = firstEngine.getWorld().getFoods();

        List<Food> secondFoods = secondEngine.getWorld().getFoods();

        assertEquals(firstFoods.size(), secondFoods.size());

        for (int i = 0; i < firstFoods.size(); i++) {
            Food first = firstFoods.get(i);

            Food second = secondFoods.get(i);

            assertEquals(first.getX(), second.getX(), 1e-9);

            assertEquals(first.getY(), second.getY(), 1e-9);
        }
    }

    @Test
    void createsHerbivoreAndAddsItToWorld() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Herbivore herbivore = createHerbivore(engine, 50.0, 50.0, 100.0, Direction.NORTH);

        assertEquals(1, engine.getWorld().getHerbivores().size());

        assertSame(herbivore, engine.getWorld().getHerbivores().get(0));
    }

    @Test
    void assignsUniqueSequentialCreatureIds() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Herbivore first = createHerbivore(engine, 50.0, 50.0, 100.0, Direction.NORTH);

        Herbivore second = createHerbivore(engine, 60.0, 60.0, 100.0, Direction.SOUTH);

        assertEquals(1, first.getId());

        assertEquals(2, second.getId());
    }

    @Test
    void failedCreatureCreationDoesNotConsumeId() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        assertThrows(IllegalArgumentException.class, () -> createHerbivore(engine, 0.0, 0.0, 100.0,
                Direction.NORTH));

        Herbivore herbivore = createHerbivore(engine, 50.0, 50.0, 100.0, Direction.NORTH);

        assertEquals(1, herbivore.getId());
    }

    @Test
    void foodInsideSenseRadiusInfluencesMovement() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Herbivore herbivore = createHerbivore(engine, 50.0, 50.0, 100.0, Direction.NORTH);

        engine.getWorld().addFood(60.0, 50.0);

        engine.tick();

        assertEquals(51.25, herbivore.getX(), 1e-9);

        assertEquals(50.0, herbivore.getY(), 1e-9);
    }

    @Test
    void foodExactlyOnSenseBoundaryInfluencesMovement() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Herbivore herbivore = createHerbivore(engine, 50.0, 50.0, 100.0, Direction.NORTH);

        // BASE_SENSE_RADIUS = 100.0 for a baseline genome.
        engine.getWorld().addFood(150.0, 50.0);

        engine.tick();

        assertEquals(51.25, herbivore.getX(), 1e-9);

        assertEquals(50.0, herbivore.getY(), 1e-9);
    }

    @Test
    void foodOutsideSenseRadiusDoesNotInfluenceMovement() {
        SimulationEngine engineWithoutFood = new SimulationEngine(createDefaultConfig());

        SimulationEngine engineWithFarFood = new SimulationEngine(createDefaultConfig());

        Herbivore withoutFood = createHerbivore(engineWithoutFood, 50.0, 50.0, 100.0, Direction.NORTH);

        Herbivore withFarFood = createHerbivore(engineWithFarFood, 50.0, 50.0, 100.0, Direction.NORTH);

        // Distance = 101, just outside the baseline sense radius of 100.
        engineWithFarFood.getWorld().addFood(151.0, 50.0);

        engineWithoutFood.tick();
        engineWithFarFood.tick();

        assertEquals(withoutFood.getX(), withFarFood.getX(), 1e-9);

        assertEquals(withoutFood.getY(), withFarFood.getY(), 1e-9);

        assertEquals(1, engineWithFarFood .getWorld().findFoodWithinRadius(151.0, 50.0, 0.0).size());
    }

    @Test
    void overlappingFoodIsEatenAndAddsEnergy() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        // Maximum reproduction threshold keeps this test focused only
        // on eating and Energy gain.
        Genome genome = createGenome(2.0, 1.0);

        Herbivore herbivore = createHerbivore(engine, 50.0, 50.0, 100.0, genome, Direction.NORTH);

        // Herbivore radius = 6, Food radius = 3.
        // Distance 9 means the two circles touch exactly.
        engine.getWorld().addFood(59.0, 50.0);

        engine.tick();

        assertEquals(199.0, herbivore.getEnergy(), 1e-9);

        assertTrue(engine.getWorld().findFoodWithinRadius(59.0, 50.0, 0.0).isEmpty());

        assertTrue(engine.getWorld().getHerbivoreEggs().isEmpty());
    }

    @Test
    void herbivoreReachingZeroEnergySurvivesUntilNextTick() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Herbivore herbivore = createHerbivore(engine, 50.0, 50.0, 1.0, Direction.NORTH);

        engine.tick();

        assertEquals(0.0, herbivore.getEnergy(), 1e-9);

        assertEquals(1, engine.getWorld().getHerbivores().size());

        engine.tick();

        assertTrue(engine.getWorld().getHerbivores().isEmpty());
    }

    @Test
    void herbivoreAtZeroEnergyCanBeRescuedByOverlappingFood() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Herbivore herbivore = createHerbivore(engine, 50.0, 50.0, 1.0, Direction.NORTH);

        engine.tick();

        assertEquals(0.0, herbivore.getEnergy(), 1e-9);

        engine.getWorld().addFood(herbivore.getX(), herbivore.getY());

        engine.tick();

        assertEquals(99.0, herbivore.getEnergy(), 1e-9);

        assertEquals(1, engine.getWorld().getHerbivores().size());

        assertSame(herbivore, engine.getWorld().getHerbivores().get(0));
    }

    @Test
    void newerHerbivoreGetsPriorityInFoodConflict() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        // Prevent reproduction from affecting this interaction-order test.
        Genome genome = createGenome(2.0, 1.0);

        Herbivore older = createHerbivore(engine, 48.0, 50.0, 100.0, genome, Direction.NORTH);

        Herbivore newer = createHerbivore(engine, 52.0, 50.0, 100.0, genome, Direction.NORTH);

        engine.getWorld().addFood(50.0, 50.0);

        engine.tick();

        assertEquals(99.0, older.getEnergy(), 1e-9);

        assertEquals(199.0, newer.getEnergy(), 1e-9);

        assertTrue(engine.getWorld().getHerbivoreEggs().isEmpty());
    }

    @Test
    void hittingWallChangesPersistentExplorationDirection() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        double boundaryMargin = Creature.calculateMaximumRadius();

        Herbivore herbivore = createHerbivore(engine, 50.0, boundaryMargin, 100.0, Direction.NORTH);

        engine.tick();

        assertEquals(boundaryMargin, herbivore.getY(), 1e-9);

        assertNotEquals(Direction.NORTH, herbivore.getExplorationDirection());
    }

    @Test
    void sameSeedProducesSameMovementSequence() {
        SimulationEngine firstEngine = new SimulationEngine(createConfig(98765L));

        SimulationEngine secondEngine = new SimulationEngine(createConfig(98765L));

        Herbivore first = createHerbivore(firstEngine, 500.0, 400.0, 100.0, Direction.NORTH);

        Herbivore second = createHerbivore(secondEngine, 500.0, 400.0, 100.0, Direction.NORTH);

        for (int i = 0; i < 20; i++) {
            firstEngine.tick();
            secondEngine.tick();
        }

        assertEquals(first.getX(), second.getX(), 1e-9);

        assertEquals(first.getY(), second.getY(), 1e-9);

        assertEquals(first.getEnergy(), second.getEnergy(), 1e-9);

        assertEquals(first.getExplorationDirection(), second.getExplorationDirection());
    }

    @Test
    void herbivoreRemainsInsideWorldForThousandsOfTicks() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        // Threshold = 400. Energy is periodically replenished while
        // remaining below that threshold. This keeps reproduction out
        // of a test that exists only to verify movement bounds.
        Genome genome = createGenome(2.0, 1.0);

        Herbivore herbivore = createHerbivore(engine, 500.0, 400.0, 300.0, genome, Direction.NORTH);

        for (int i = 0; i < 10_000; i++) {
            engine.tick();

            double radius = herbivore.getRadius();

            assertTrue(herbivore.getX() >= radius && herbivore.getX() <= engine.getWorld().getWidth() - radius);

            assertTrue(herbivore.getY() >= radius && herbivore.getY() <= engine.getWorld().getHeight() - radius);

            if ((i + 1) % 200 == 0) {
                herbivore.addEnergy(200.0);
            }
        }
    }

    @Test
    void tickConsumesEnergyAccordingToGenome() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome genome = new Genome(
                4.0,   // speed
                1.0,   // size
                1.0,   // sense radius
                1.0, 1.0, 180.0);

        Herbivore herbivore = engine.createHerbivore(500.0, 400.0, 100.0, 1, genome, Direction.NORTH);

        engine.tick();

        assertEquals(98.0, herbivore.getEnergy(), 1e-9);
    }

    // ---------------------------------------------------------------------
    // M8 - Eggs and asexual reproduction
    // ---------------------------------------------------------------------

    @Test
    void herbivoreAtThresholdLaysEggAndPaysReproductionCost() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Herbivore parent = createHerbivore(engine, 500.0, 400.0, 200.0, Direction.NORTH);

        double layingX = parent.getX();
        double layingY = parent.getY();

        engine.tick();

        assertEquals(1, engine.getWorld().getHerbivoreEggs().size());

        Egg egg = engine.getWorld().getHerbivoreEggs().get(0);

        assertEquals(layingX, egg.getX(), 1e-9);

        assertEquals(layingY, egg.getY(), 1e-9);

        assertEquals(2, egg.getGeneration());

        // A newly laid Egg must not lose one hatch tick
        // during the tick in which it was created.
        assertEquals(Creature.calculateEggHatchTicks(egg.getGenome()), egg.getRemainingHatchTicks());

        // 200 - 80 reproduction cost - 1 normal tick cost.
        assertEquals(119.0, parent.getEnergy(), 1e-9);

        assertEquals(50, parent.getReproductionCooldownTicksRemaining());

        assertFalse(parent.isReproductionCooldownComplete());
    }

    @Test
    void reproductionCreatesIndependentMutatedChildGenome() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());
        Genome parentGenome = engine.getConfig().getInitialGenome();
        createHerbivore(engine, 500.0, 400.0, 200.0, parentGenome, Direction.NORTH);
        engine.tick();
        Genome childGenome = engine.getWorld().getHerbivoreEggs().get(0).getGenome();
        assertNotSame(parentGenome, childGenome);
        assertTraitsWithinMutationRange(parentGenome, childGenome);
    }

    @Test
    void herbivoreBelowThresholdDoesNotReproduce() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        createHerbivore(engine, 500.0, 400.0, 199.0, Direction.NORTH);

        engine.tick();

        assertTrue(engine.getWorld().getHerbivoreEggs().isEmpty());
    }

    @Test
    void eatingCanTriggerReproductionInSameTick() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Herbivore herbivore = createHerbivore(engine, 500.0, 400.0, 100.0, Direction.NORTH);

        // Exactly touching:
        // Herbivore radius 6 + Food radius 3 = 9.
        engine.getWorld().addFood(509.0, 400.0);

        engine.tick();

        assertEquals(1, engine.getWorld().getHerbivoreEggs().size());

        // 100 + 100 food - 80 reproduction - 1 tick cost.
        assertEquals(119.0, herbivore.getEnergy(), 1e-9);

        assertTrue(engine.getWorld().getFoods().isEmpty());
    }


    @Test
    void reproductionCooldownPreventsNewEggUntilFiftyTicksPass() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        // Long hatch time prevents eggs from hatching
        // while testing reproduction cooldown.
        Genome genome = createGenome(1.0, 4.0);

        Herbivore herbivore = createHerbivore(
                engine,
                500.0,
                400.0,
                1000.0,
                genome,
                Direction.NORTH
        );

        engine.tick();

        assertEquals(1, countTotalEggs(engine));
        assertEquals(
                50,
                herbivore.getReproductionCooldownTicksRemaining()
        );

        for (int i = 0; i < 49; i++) {
            engine.tick();
        }

        assertEquals(1, countTotalEggs(engine));
        assertEquals(
                1,
                herbivore.getReproductionCooldownTicksRemaining()
        );

        engine.tick();

        assertEquals(2, countTotalEggs(engine));
        assertEquals(
                50,
                herbivore.getReproductionCooldownTicksRemaining()
        );
    }

    private int countTotalEggs(SimulationEngine engine) {
        return engine.getWorld().getHerbivoreEggs().size()
                + engine.getWorld().getPredatorEggs().size();
    }


    @Test
    void eggHatchesAfterExactCountdown() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome genome = engine.getConfig().getInitialGenome();

        Egg egg = engine.getWorld().addHerbivoreEgg(500.0, 400.0, genome, 2, 2);

        engine.tick();

        assertEquals(1, egg.getRemainingHatchTicks());

        assertEquals(1, engine.getWorld().getHerbivoreEggs().size());

        assertTrue(engine.getWorld().getHerbivores().isEmpty());

        engine.tick();

        assertTrue(engine.getWorld().getHerbivoreEggs().isEmpty());

        assertEquals(1, engine.getWorld().getHerbivores().size());

        Herbivore child = engine.getWorld().getHerbivores().get(0);

        assertEquals(500.0, child.getX(), 1e-9);

        assertEquals(400.0, child.getY(), 1e-9);

        assertEquals(80.0, child.getEnergy(), 1e-9);

        assertEquals(2, child.getGeneration());

        assertSame(genome, child.getGenome());

        assertNotNull(child.getExplorationDirection());
    }

    @Test
    void hatchedHerbivoreDoesNotActUntilFollowingTick() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        engine.getWorld().addHerbivoreEgg(500.0, 400.0, engine.getConfig().getInitialGenome(), 2, 1);

        engine.tick();

        Herbivore child = engine.getWorld().getHerbivores().get(0);

        // If the child had been processed immediately after hatching,
        // it would already have paid its normal tick Energy cost.
        assertEquals(80.0, child.getEnergy(), 1e-9);

        assertEquals(500.0, child.getX(), 1e-9);

        assertEquals(400.0, child.getY(), 1e-9);

        engine.tick();

        assertEquals(79.0, child.getEnergy(), 1e-9);
    }

    @Test
    void parentDeathDoesNotRemoveItsEgg() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());
        Genome genome = createGenome(0.5, 1.0);
        Herbivore parent = createHerbivore(engine, 500.0, 400.0, 100.0, genome, Direction.NORTH);
        engine.tick();
        assertEquals(1, engine.getWorld().getHerbivoreEggs().size());
        assertEquals(19.0, parent.getEnergy(), 1e-9);
        Egg egg = engine.getWorld().getHerbivoreEggs().get(0);
        int initialHatchTicks = egg.getRemainingHatchTicks();
        // The parent dies on its 21st action tick, but its egg stays alive.
        for (int i = 0; i < 20; i++) engine.tick();
        assertTrue(engine.getWorld().getHerbivores().isEmpty());
        assertEquals(1, engine.getWorld().getHerbivoreEggs().size());
        assertSame(egg, engine.getWorld().getHerbivoreEggs().get(0));
        assertEquals(initialHatchTicks - 20, egg.getRemainingHatchTicks());
        int remaining = egg.getRemainingHatchTicks();
        for (int i = 0; i < remaining; i++) engine.tick();
        assertTrue(engine.getWorld().getHerbivoreEggs().isEmpty());
        assertEquals(1, engine.getWorld().getHerbivores().size());
        Herbivore child = engine.getWorld().getHerbivores().get(0);
        assertEquals(2, child.getGeneration());
        assertEquals(80.0, child.getEnergy(), 1e-9);
    }

    @Test
    void supportsMultipleGenerations() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());
        Genome genome = createGenome(0.5, 0.25);
        createHerbivore(engine, 500.0, 400.0, 100.0, genome, Direction.NORTH);

        engine.tick(); // Generation 1 lays Generation 2 egg.
        Egg generationTwoEgg = engine.getWorld().getHerbivoreEggs().get(0);
        assertEquals(2, generationTwoEgg.getGeneration());
        int firstHatchTicks = generationTwoEgg.getRemainingHatchTicks();
        for (int i = 0; i < firstHatchTicks; i++) engine.tick();

        Herbivore generationTwo = engine.getWorld().getHerbivores().stream().filter(h -> h.getGeneration() == 2).findFirst().orElseThrow();
        assertEquals(80.0, generationTwo.getEnergy(), 1e-9);
        engine.getWorld().addFood(generationTwo.getX(), generationTwo.getY());
        engine.tick(); // Generation 2 eats and lays Generation 3 egg.

        Egg generationThreeEgg = engine.getWorld().getHerbivoreEggs().stream().filter(e -> e.getGeneration() == 3).findFirst().orElseThrow();
        int secondHatchTicks = generationThreeEgg.getRemainingHatchTicks();
        for (int i = 0; i < secondHatchTicks; i++) engine.tick();

        assertTrue(engine.getWorld().getHerbivores().stream().anyMatch(h -> h.getGeneration() == 2));
        assertTrue(engine.getWorld().getHerbivores().stream().anyMatch(h -> h.getGeneration() == 3));
    }

    @Test
    void reproductionUsesMutatedChildGenome() {
        long seed = 12345L;
        double mutationStrength = 0.05;

        SimulationEngine engine = new SimulationEngine(createConfig(seed, 0, 0));

        Genome parentGenome = new Genome(1.0, 1.0, 1.0, 1.0, 1.0, 180.0);

        Herbivore parent = engine.createHerbivore(500.0, 400.0, 200.0, 1, parentGenome, Direction.EAST);

        /*
         * No initial entities were created, so the Engine's Random
         * has not been consumed yet. A separate Random with the same
         * seed should therefore produce the same child mutation.
         */
        Random expectedRandom = new Random(seed);

        Genome expectedChildGenome = parentGenome.mutate(mutationStrength, expectedRandom);

        engine.tick();

        assertEquals(1, engine.getWorld().getHerbivoreEggs().size());

        Genome actualChildGenome = engine.getWorld().getHerbivoreEggs().get(0).getGenome();

        assertNotSame(parentGenome, actualChildGenome);

        assertEquals(expectedChildGenome.getSpeed(), actualChildGenome.getSpeed(), 1e-9);

        assertEquals(expectedChildGenome.getSize(), actualChildGenome.getSize(), 1e-9);

        assertEquals(expectedChildGenome.getSenseRadius(), actualChildGenome.getSenseRadius(), 1e-9);

        assertEquals(expectedChildGenome.getReproductionThreshold(), actualChildGenome.getReproductionThreshold(),
                1e-9);

        assertEquals(expectedChildGenome.getEggHatchTime(), actualChildGenome.getEggHatchTime(), 1e-9);

        assertEquals(expectedChildGenome.getHue(), actualChildGenome.getHue(), 1e-9);
    }

    @Test
    void sameSeedProducesSameChildMutation() {
        long seed = 98765L;
        double mutationStrength = 0.05;

        SimulationEngine first = new SimulationEngine(createConfig(seed, 0, 0));

        SimulationEngine second = new SimulationEngine(createConfig(seed, 0, 0));

        Genome firstParentGenome = new Genome(1.0, 1.0, 1.0, 1.0, 1.0, 180.0);

        Genome secondParentGenome = new Genome(1.0, 1.0, 1.0, 1.0, 1.0, 180.0);

        first.createHerbivore(500.0, 400.0, 200.0, 1, firstParentGenome, Direction.EAST);

        second.createHerbivore(500.0, 400.0, 200.0, 1, secondParentGenome, Direction.EAST);

        first.tick();
        second.tick();

        Egg firstEgg = first.getWorld().getHerbivoreEggs().get(0);

        Egg secondEgg = second.getWorld().getHerbivoreEggs().get(0);

        Genome firstChild = firstEgg.getGenome();

        Genome secondChild = secondEgg.getGenome();

        assertEquals(firstChild.getSpeed(), secondChild.getSpeed());
        assertEquals(firstChild.getSize(), secondChild.getSize());
        assertEquals(firstChild.getSenseRadius(), secondChild.getSenseRadius());
        assertEquals(firstChild.getReproductionThreshold(), secondChild.getReproductionThreshold());
        assertEquals(firstChild.getEggHatchTime(), secondChild.getEggHatchTime());
        assertEquals(firstChild.getHue(), secondChild.getHue());

        assertEquals(firstEgg.getRemainingHatchTicks(), secondEgg.getRemainingHatchTicks());
    }

    @Test
    void eggHatchTimeIsDerivedFromChildGenome() {
        long seed = 12345L;
        double mutationStrength = 0.05;

        SimulationEngine engine = new SimulationEngine(createConfig(seed, 0, 0));

        Genome parentGenome = new Genome(1.0, 1.0, 1.0, 1.0, 1.0, 180.0);

        engine.createHerbivore(500.0, 400.0, 200.0, 1, parentGenome, Direction.EAST);

        engine.tick();

        Egg egg = engine.getWorld().getHerbivoreEggs().get(0);

        int expectedHatchTicks = Creature.calculateEggHatchTicks(egg.getGenome());

        assertEquals(expectedHatchTicks, egg.getRemainingHatchTicks());
    }

    @Test
    void sameSeedProducesSameInitialGenomes() {
        long seed = 12345L;
        double mutationStrength = 0.05;

        SimulationEngine first = new SimulationEngine(createConfig(seed, 10, 0));

        SimulationEngine second = new SimulationEngine(createConfig(seed, 10, 0));

        List<Herbivore> firstHerbivores = first.getWorld().getHerbivores();

        List<Herbivore> secondHerbivores = second.getWorld().getHerbivores();

        assertEquals(firstHerbivores.size(), secondHerbivores.size());

        for (int i = 0; i < firstHerbivores.size(); i++) {
            Genome firstGenome = firstHerbivores.get(i).getGenome();

            Genome secondGenome = secondHerbivores.get(i).getGenome();

            assertEquals(firstGenome.getSpeed(), secondGenome.getSpeed());

            assertEquals(firstGenome.getSize(), secondGenome.getSize());

            assertEquals(firstGenome.getSenseRadius(), secondGenome.getSenseRadius());

            assertEquals(firstGenome.getReproductionThreshold(), secondGenome.getReproductionThreshold());

            assertEquals(firstGenome.getEggHatchTime(), secondGenome.getEggHatchTime());

            assertEquals(firstGenome.getHue(), secondGenome.getHue());
        }
    }

    private SimulationConfig createSpawnConfig(long seed, int initialHerbivores,
                                               int initialFood, int foodSpawnMaxPerTick) {
        return new SimulationConfig(new Genome(1.0, 1.0, 1.0, 1.0, 1.0, 180.0),
                initialHerbivores, 0, initialFood, seed, foodSpawnMaxPerTick);
    }

    @Test
    void foodSpawningCanBeDisabled() {
        SimulationEngine engine = new SimulationEngine(createSpawnConfig(12345L, 0, 0, 0));

        engine.tick();

        assertEquals(0, engine.getWorld().getFoods().size());
    }

    @Test
    void foodSpawningUsesConfiguredRandomRange() {
        long seed = 12345L;
        int foodSpawnMaxPerTick = 10;

        SimulationEngine engine = new SimulationEngine(createSpawnConfig(seed, 0, 0, foodSpawnMaxPerTick));

        Random expectedRandom = new Random(seed);

        int expectedFood = expectedRandom.nextInt(foodSpawnMaxPerTick + 1);

        engine.tick();

        assertEquals(expectedFood, engine.getWorld().getFoods().size());
    }

    @Test
    void foodSpawningDoesNotExceedFoodCap() {
        SimulationEngine engine = new SimulationEngine(createSpawnConfig(12345L, 0, 4999, 100));
        for (int i = 0; i < 20; i++) engine.tick();
        assertEquals(5000, engine.getWorld().getFoods().size());
    }

    @Test
    void foodSpawningDoesNothingWhenFoodCapIsAlreadyReached() {
        SimulationEngine engine = new SimulationEngine(createSpawnConfig(12345L, 0, 5000, 100));
        engine.tick();
        assertEquals(5000, engine.getWorld().getFoods().size());
    }

    @Test
    void sameSeedProducesSameFoodSpawning() {
        SimulationConfig config = createSpawnConfig(12345L, 0, 0, 10);

        SimulationEngine first = new SimulationEngine(config);

        SimulationEngine second = new SimulationEngine(config);

        first.tick();
        second.tick();

        List<Food> firstFoods = first.getWorld().getFoods();

        List<Food> secondFoods = second.getWorld().getFoods();

        assertEquals(firstFoods.size(), secondFoods.size());

        for (int i = 0; i < firstFoods.size(); i++) {
            assertEquals(firstFoods.get(i).getX(), secondFoods.get(i).getX());

            assertEquals(firstFoods.get(i).getY(), secondFoods.get(i).getY());
        }
    }

    // ---------------------------------------------------------------------
    // M10 - Predators, predation, pursuit, avoidance and predator lifecycle
    // ---------------------------------------------------------------------

    @Test
    void createsConfiguredInitialPredators() {
        SimulationEngine engine = new SimulationEngine(createConfig(12345L, 20, 5, 100));

        assertEquals(20, engine.getWorld().getHerbivores().size());

        assertEquals(5, engine.getWorld().getPredators().size());

        assertEquals(100, engine.getWorld().getFoods().size());
    }

    @Test
    void initialPredatorsUseConfiguredTraitsAndInitialState() {
        SimulationEngine engine = new SimulationEngine(createConfig(12345L, 0, 20, 0));
        Genome baseline = engine.getConfig().getInitialGenome();
        for (Predator predator : engine.getWorld().getPredators()) {
            assertNotSame(baseline, predator.getGenome());
            assertTraitsWithinMutationRange(baseline, predator.getGenome());
            assertEquals(100.0, predator.getEnergy(), 1e-9);
            assertEquals(1, predator.getGeneration());
            assertNotNull(predator.getExplorationDirection());
        }
    }

    @Test
    void initialPredatorsAreCreatedInsideCreatureMovementBounds() {
        SimulationEngine engine = new SimulationEngine(createConfig(12345L, 0, 100, 0));

        double margin = Creature.calculateMaximumRadius();

        double worldWidth = engine.getWorld().getWidth();

        double worldHeight = engine.getWorld().getHeight();

        for (Predator predator :
                engine.getWorld().getPredators()) {

            assertTrue(predator.getX() >= margin && predator.getX() <= worldWidth - margin);

            assertTrue(predator.getY() >= margin && predator.getY() <= worldHeight - margin);
        }
    }

    @Test
    void creatureIdsRemainUniqueAcrossSpecies() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome genome = engine.getConfig().getInitialGenome();

        Herbivore first = createHerbivore(engine, 400.0, 400.0, 100.0, genome, Direction.NORTH);

        Predator second = createPredator(engine, 500.0, 400.0, 100.0, genome, Direction.NORTH);

        Herbivore third = createHerbivore(engine, 600.0, 400.0, 100.0, genome, Direction.NORTH);

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertEquals(3, third.getId());
    }

    @Test
    void overlappingLegalPreyIsEatenAndAddsEnergy() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome genome = new Genome(1.0, 1.0, 1.0, 2.0, 1.0, 180.0);

        Predator predator = createPredator(engine, 500.0, 400.0, 50.0, genome, Direction.NORTH);

        createHerbivore(engine, 500.0, 400.0, 100.0, genome, Direction.NORTH);

        engine.tick();

        assertTrue(engine.getWorld().getHerbivores().isEmpty());

        // 50 initial + 100 prey Energy - 1 baseline tick cost.
        assertEquals(149.0, predator.getEnergy(), 1e-9);
    }

    @Test
    void predatorAtExactMinimumSizeRatioCanEatHerbivore() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome predatorGenome = new Genome(1.0, 0.8, 1.0, 2.0, 1.0, 180.0);

        Genome herbivoreGenome = new Genome(1.0, 1.0, 1.0, 2.0, 1.0, 180.0);

        createPredator(engine, 500.0, 400.0, 50.0, predatorGenome, Direction.NORTH);

        createHerbivore(engine, 500.0, 400.0, 100.0, herbivoreGenome, Direction.NORTH);

        engine.tick();

        assertTrue(engine.getWorld().getHerbivores().isEmpty());
    }

    @Test
    void predatorBelowMinimumSizeRatioCannotEatHerbivore() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome predatorGenome = new Genome(1.0, 0.79, 1.0, 2.0, 1.0, 180.0);

        Genome herbivoreGenome = new Genome(1.0, 1.0, 1.0, 2.0, 1.0, 180.0);

        createPredator(engine, 500.0, 400.0, 50.0, predatorGenome, Direction.NORTH);

        Herbivore herbivore = createHerbivore(engine, 500.0, 400.0, 100.0, herbivoreGenome, Direction.NORTH);

        engine.tick();

        assertEquals(1, engine.getWorld().getHerbivores().size());

        assertSame(herbivore, engine.getWorld().getHerbivores().get(0));
    }

    @Test
    void predatorEatsPreyExactlyOnContactBoundary() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome genome = new Genome(1.0, 1.0, 1.0, 2.0, 1.0, 180.0);

        Predator predator = createPredator(engine, 500.0, 400.0, 50.0, genome, Direction.NORTH);

        double contactDistance = predator.getRadius() + Creature.calculateRadius(genome);

        createHerbivore(engine, 500.0 + contactDistance, 400.0, 100.0, genome, Direction.NORTH);

        engine.tick();

        assertTrue(engine.getWorld().getHerbivores().isEmpty());
    }

    @Test
    void predatorDoesNotEatPreyOutsideContactBoundaryInCurrentTick() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome genome = new Genome(1.0, 1.0, 1.0, 2.0, 1.0, 180.0);

        Predator predator = createPredator(engine, 500.0, 400.0, 50.0, genome, Direction.NORTH);

        double contactDistance = predator.getRadius() + Creature.calculateRadius(genome);

        createHerbivore(engine, 500.0 + contactDistance + 0.1, 400.0, 100.0, genome, Direction.NORTH);

        engine.tick();

        assertEquals(1, engine.getWorld().getHerbivores().size());
    }

    @Test
    void capturedHerbivoreDoesNotEatFoodBeforePredation() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome genome = new Genome(1.0, 1.0, 1.0, 2.0, 1.0, 180.0);

        createPredator(engine, 500.0, 400.0, 50.0, genome, Direction.NORTH);

        createHerbivore(engine, 500.0, 400.0, 100.0, genome, Direction.NORTH);

        Food food = engine.getWorld().addFood(500.0, 400.0);

        engine.tick();

        assertTrue(engine.getWorld().getHerbivores().isEmpty());

        assertEquals(1, engine.getWorld().getFoods().size());

        assertSame(food, engine.getWorld().getFoods().get(0));
    }

    @Test
    void predatorCanEatMultipleHerbivoresInSameTick() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome genome = new Genome(1.0, 1.0, 1.0, 2.0, 1.0, 180.0);

        Predator predator = createPredator(engine, 500.0, 400.0, 50.0, genome, Direction.NORTH);

        createHerbivore(engine, 500.0, 400.0, 100.0, genome, Direction.NORTH);

        createHerbivore(engine, 501.0, 400.0, 100.0, genome, Direction.NORTH);

        engine.tick();

        assertTrue(engine.getWorld().getHerbivores().isEmpty());

        assertEquals(249.0, predator.getEnergy(), 1e-9);
    }

    @Test
    void newerPredatorGetsPriorityWhenBothCanEatSameHerbivore() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome genome = new Genome(1.0, 1.0, 1.0, 2.0, 1.0, 180.0);

        Predator olderPredator = createPredator(engine, 500.0, 400.0, 50.0, genome, Direction.NORTH);

        Predator newerPredator = createPredator(engine, 500.0, 400.0, 50.0, genome, Direction.NORTH);

        createHerbivore(engine, 500.0, 400.0, 100.0, genome, Direction.NORTH);

        engine.tick();

        assertTrue(engine.getWorld().getHerbivores().isEmpty());

        assertEquals(49.0, olderPredator.getEnergy(), 1e-9);

        assertEquals(149.0, newerPredator.getEnergy(), 1e-9);
    }

    @Test
    void herbivoreAvoidsLegalPredator() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome genome = new Genome(1.0, 1.0, 1.0, 2.0, 1.0, 180.0);

        Herbivore herbivore = createHerbivore(engine, 500.0, 400.0, 100.0, genome, Direction.NORTH);

        createPredator(engine, 520.0, 400.0, 50.0, genome, Direction.NORTH);

        engine.tick();

        assertEquals(498.75, herbivore.getX(), 1e-9);

        assertEquals(400.0, herbivore.getY(), 1e-9);
    }

    @Test
    void herbivoreIgnoresPredatorThatCannotEatIt() {
        SimulationEngine control = new SimulationEngine(createDefaultConfig());

        SimulationEngine withSmallPredator = new SimulationEngine(createDefaultConfig());

        Genome herbivoreGenome = new Genome(1.0, 1.0, 1.0, 2.0, 1.0, 180.0);

        Genome smallPredatorGenome = new Genome(1.0, 0.79, 1.0, 2.0, 1.0, 180.0);

        Herbivore controlHerbivore = createHerbivore(control, 500.0, 400.0, 100.0, herbivoreGenome,
                Direction.NORTH);

        Herbivore testedHerbivore = createHerbivore(withSmallPredator, 500.0, 400.0, 100.0, herbivoreGenome,
                Direction.NORTH);

        createPredator(withSmallPredator, 520.0, 400.0, 50.0, smallPredatorGenome, Direction.NORTH);

        control.tick();
        withSmallPredator.tick();

        assertEquals(controlHerbivore.getX(), testedHerbivore.getX(), 1e-9);

        assertEquals(controlHerbivore.getY(), testedHerbivore.getY(), 1e-9);
    }

    @Test
    void predationOccursOnFollowingTickAfterMovementCreatesOverlap() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome predatorGenome = new Genome(4.0, 1.0, 1.0, 2.0, 1.0, 180.0);

        Genome herbivoreGenome = new Genome(0.25, 1.0, 1.0, 2.0, 1.0, 180.0);

        createPredator(engine, 500.0, 400.0, 50.0, predatorGenome, Direction.EAST);

        Herbivore herbivore = createHerbivore(engine, 515.0, 400.0, 100.0, herbivoreGenome, Direction.EAST);

        engine.tick();

        // They overlap only after both have moved, so the herbivore
        // survives until the next tick's resolvePredation().
        assertTrue(engine.getWorld().getHerbivores().contains(herbivore));

        engine.tick();

        assertFalse(engine.getWorld().getHerbivores().contains(herbivore));
    }

    @Test
    void predatorReachingZeroEnergyDiesOnFollowingTickWithoutPrey() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome genome = new Genome(1.0, 1.0, 1.0, 2.0, 1.0, 180.0);

        Predator predator = createPredator(engine, 500.0, 400.0, 1.0, genome, Direction.NORTH);

        engine.tick();

        assertEquals(0.0, predator.getEnergy(), 1e-9);

        assertTrue(engine.getWorld().getPredators().contains(predator));

        engine.tick();

        assertFalse(engine.getWorld().getPredators().contains(predator));
    }

    @Test
    void predatorAtZeroEnergyCanBeRescuedByOverlappingPrey() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome genome = new Genome(1.0, 1.0, 1.0, 2.0, 1.0, 180.0);

        Predator predator = createPredator(engine, 500.0, 400.0, 1.0, genome, Direction.NORTH);

        engine.tick();

        assertEquals(0.0, predator.getEnergy(), 1e-9);

        createHerbivore(engine, predator.getX(), predator.getY(), 100.0, genome, Direction.NORTH);

        engine.tick();

        assertTrue(engine.getWorld().getPredators().contains(predator));

        assertEquals(99.0, predator.getEnergy(), 1e-9);
    }

    @Test
    void predatorDoesNotEatHerbivoreEggs() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome genome = new Genome(1.0, 1.0, 1.0, 2.0, 1.0, 180.0);

        createPredator(engine, 500.0, 400.0, 50.0, genome, Direction.NORTH);

        Egg egg = engine.getWorld().addHerbivoreEgg(500.0, 400.0, genome, 2, 2);

        engine.tick();

        assertEquals(1, engine.getWorld().getHerbivoreEggs().size());

        assertSame(egg, engine.getWorld().getHerbivoreEggs().get(0));

        assertEquals(1, egg.getRemainingHatchTicks());
    }

    @Test
    void predatorAtThresholdLaysEggAndPaysReproductionCost() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome genome = new Genome(1.0, 1.0, 1.0, 1.0, 1.0, 180.0);

        Predator parent = createPredator(engine, 500.0, 400.0, 200.0, genome, Direction.NORTH);

        double layingX = parent.getX();

        double layingY = parent.getY();

        engine.tick();

        assertEquals(1, engine.getWorld().getPredatorEggs().size());

        Egg egg = engine.getWorld().getPredatorEggs().get(0);

        assertEquals(layingX, egg.getX(), 1e-9);

        assertEquals(layingY, egg.getY(), 1e-9);

        assertEquals(2, egg.getGeneration());

        assertEquals(Creature.calculateEggHatchTicks(egg.getGenome()), egg.getRemainingHatchTicks());

        assertEquals(119.0, parent.getEnergy(), 1e-9);

        assertEquals(50, parent.getReproductionCooldownTicksRemaining());
    }

    @Test
    void predatorBelowThresholdDoesNotReproduce() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome genome = new Genome(1.0, 1.0, 1.0, 1.0, 1.0, 180.0);

        createPredator(engine, 500.0, 400.0, 199.0, genome, Direction.NORTH);

        engine.tick();

        assertTrue(engine.getWorld().getPredatorEggs().isEmpty());
    }

    @Test
    void predatorReproductionCreatesIndependentMutatedChildGenome() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());
        Genome parentGenome = engine.getConfig().getInitialGenome();
        createPredator(engine, 500.0, 400.0, 200.0, parentGenome, Direction.NORTH);
        engine.tick();
        Genome childGenome = engine.getWorld().getPredatorEggs().get(0).getGenome();
        assertNotSame(parentGenome, childGenome);
        assertTraitsWithinMutationRange(parentGenome, childGenome);
    }

    @Test
    void predatorEggHatchesAfterExactCountdown() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        Genome genome = engine.getConfig().getInitialGenome();

        Egg egg = engine.getWorld().addPredatorEgg(500.0, 400.0, genome, 2, 2);

        engine.tick();

        assertEquals(1, egg.getRemainingHatchTicks());

        assertEquals(1, engine.getWorld().getPredatorEggs().size());

        assertTrue(engine.getWorld().getPredators().isEmpty());

        engine.tick();

        assertTrue(engine.getWorld().getPredatorEggs().isEmpty());

        assertEquals(1, engine.getWorld().getPredators().size());

        Predator child = engine.getWorld().getPredators().get(0);

        assertEquals(500.0, child.getX(), 1e-9);

        assertEquals(400.0, child.getY(), 1e-9);

        assertEquals(80.0, child.getEnergy(), 1e-9);

        assertEquals(2, child.getGeneration());

        assertSame(genome, child.getGenome());

        assertNotNull(child.getExplorationDirection());
    }

    @Test
    void hatchedPredatorDoesNotActUntilFollowingTick() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());

        engine.getWorld().addPredatorEgg(500.0, 400.0, engine.getConfig().getInitialGenome(), 2, 1);

        engine.tick();

        Predator child = engine.getWorld().getPredators().get(0);

        assertEquals(80.0, child.getEnergy(), 1e-9);

        assertEquals(500.0, child.getX(), 1e-9);

        assertEquals(400.0, child.getY(), 1e-9);

        engine.tick();

        assertEquals(79.0, child.getEnergy(), 1e-9);
    }

    @Test
    void predatorReproductionUsesMutatedChildGenome() {
        long seed = 24680L;
        double mutationStrength = 0.05;

        SimulationEngine engine = new SimulationEngine(createConfig(seed, 0, 0));

        Genome parentGenome = new Genome(1.0, 1.0, 1.0, 1.0, 1.0, 180.0);

        createPredator(engine, 500.0, 400.0, 200.0, parentGenome, Direction.EAST);

        Random expectedRandom = new Random(seed);

        Genome expectedChildGenome = parentGenome.mutate(mutationStrength, expectedRandom);

        engine.tick();

        Genome actualChildGenome = engine.getWorld().getPredatorEggs().get(0).getGenome();

        assertEquals(expectedChildGenome.getSpeed(), actualChildGenome.getSpeed(), 1e-9);

        assertEquals(expectedChildGenome.getSize(), actualChildGenome.getSize(), 1e-9);

        assertEquals(expectedChildGenome.getSenseRadius(), actualChildGenome.getSenseRadius(), 1e-9);

        assertEquals(expectedChildGenome.getReproductionThreshold(), actualChildGenome.getReproductionThreshold(),
                1e-9);

        assertEquals(expectedChildGenome.getEggHatchTime(), actualChildGenome.getEggHatchTime(), 1e-9);

        assertEquals(expectedChildGenome.getHue(), actualChildGenome.getHue(), 1e-9);
    }

    @Test
    void sameSeedProducesSameInitialPredators() {
        SimulationEngine first = new SimulationEngine(createConfig(98765L, 0, 10, 0));

        SimulationEngine second = new SimulationEngine(createConfig(98765L, 0, 10, 0));

        List<Predator> firstPredators = first.getWorld().getPredators();

        List<Predator> secondPredators = second.getWorld().getPredators();

        assertEquals(firstPredators.size(), secondPredators.size());

        for (int i = 0; i < firstPredators.size(); i++) {
            Predator firstPredator = firstPredators.get(i);

            Predator secondPredator = secondPredators.get(i);

            assertEquals(firstPredator.getId(), secondPredator.getId());

            assertEquals(firstPredator.getX(), secondPredator.getX(), 1e-9);

            assertEquals(firstPredator.getY(), secondPredator.getY(), 1e-9);

            assertEquals(firstPredator.getExplorationDirection(), secondPredator.getExplorationDirection());

            assertEquals(firstPredator.getGenome().getSpeed(), secondPredator.getGenome().getSpeed());

            assertEquals(firstPredator.getGenome().getSize(), secondPredator.getGenome().getSize());

            assertEquals(firstPredator.getGenome().getSenseRadius(), secondPredator.getGenome().getSenseRadius()
            );

            assertEquals(firstPredator.getGenome().getReproductionThreshold(),
                    secondPredator.getGenome().getReproductionThreshold());

            assertEquals(firstPredator.getGenome().getEggHatchTime(), secondPredator.getGenome().getEggHatchTime()
            );

            assertEquals(firstPredator.getGenome().getHue(), secondPredator.getGenome().getHue());
        }
    }

    @Test
    void sameSeedProducesSamePredatorMovementSequence() {
        SimulationEngine first = new SimulationEngine(createConfig(98765L));

        SimulationEngine second = new SimulationEngine(createConfig(98765L));

        Genome genome = new Genome(1.0, 1.0, 1.0, 2.0, 1.0, 180.0);

        Predator firstPredator = createPredator(first, 500.0, 400.0, 100.0, genome, Direction.NORTH);

        Predator secondPredator = createPredator(second, 500.0, 400.0, 100.0, genome, Direction.NORTH);

        for (int i = 0; i < 20; i++) {
            first.tick();
            second.tick();
        }

        assertEquals(firstPredator.getX(), secondPredator.getX(), 1e-9);

        assertEquals(firstPredator.getY(), secondPredator.getY(), 1e-9);

        assertEquals(firstPredator.getEnergy(), secondPredator.getEnergy(), 1e-9);

        assertEquals(firstPredator.getExplorationDirection(), secondPredator.getExplorationDirection());
    }
    // M11 - Engine-level limits. Food cap is not a SimulationConfig validation.
    @Test
    void rejectsInitialFoodAboveEngineCap() {
        SimulationConfig config = createConfig(12345L, 0, 5001);
        assertThrows(IllegalArgumentException.class, () -> new SimulationEngine(config));
    }

    @Test
    void acceptsInitialFoodExactlyAtEngineCap() {
        SimulationEngine engine = new SimulationEngine(createConfig(12345L, 0, 5000));
        assertEquals(5000, engine.getWorld().getFoods().size());
    }

    @Test
    void fullPopulationPreventsHerbivoreReproductionWithoutExtraCost() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());
        Herbivore parent = createHerbivore(engine, 500.0, 400.0, 200.0, Direction.NORTH);
        Genome eggGenome = createGenome(1.0, 1.0);
        for (int i = 0; i < 9999; i++) {
            engine.getWorld().addHerbivoreEgg(500.0, 400.0, eggGenome, 2, 100);
        }
        engine.tick();
        assertEquals(9999, engine.getWorld().getHerbivoreEggs().size());
        assertEquals(199.0, parent.getEnergy(), 1e-9); // Only normal tick cost.
        assertEquals(0, parent.getReproductionCooldownTicksRemaining());
    }

    @Test
    void fullPopulationPreventsPredatorReproductionWithoutExtraCost() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());
        Predator parent = createPredator(engine, 500.0, 400.0, 200.0, createGenome(1.0, 1.0), Direction.NORTH);
        Genome eggGenome = createGenome(1.0, 1.0);
        for (int i = 0; i < 9999; i++) {
            engine.getWorld().addPredatorEgg(500.0, 400.0, eggGenome, 2, 100);
        }
        engine.tick();
        assertEquals(9999, engine.getWorld().getPredatorEggs().size());
        assertEquals(199.0, parent.getEnergy(), 1e-9);
        assertEquals(0, parent.getReproductionCooldownTicksRemaining());
    }

    @Test
    void reproductionCanReachThePopulationCapExactly() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());
        Herbivore parent = createHerbivore(engine, 500.0, 400.0, 200.0, Direction.NORTH);
        Genome eggGenome = createGenome(1.0, 1.0);
        for (int i = 0; i < 9998; i++) {
            engine.getWorld().addHerbivoreEgg(500.0, 400.0, eggGenome, 2, 100);
        }
        engine.tick();
        assertEquals(9999, engine.getWorld().getHerbivoreEggs().size());
        assertEquals(119.0, parent.getEnergy(), 1e-9);
        assertEquals(50, parent.getReproductionCooldownTicksRemaining());
    }

    @Test
    void eggCanHatchWhenPopulationIsAtCapacity() {
        SimulationEngine engine = new SimulationEngine(createDefaultConfig());
        Genome genome = createGenome(1.0, 1.0);
        for (int i = 0; i < 9999; i++) {
            engine.getWorld().addHerbivoreEgg(500.0, 400.0, genome, 2, 100);
        }
        engine.getWorld().addHerbivoreEgg(500.0, 400.0, genome, 2, 1);
        engine.tick();
        assertEquals(9999, engine.getWorld().getHerbivoreEggs().size());
        assertEquals(1, engine.getWorld().getHerbivores().size());
    }


    // ---------------------------------------------------------------------
    // M13 - Statistics history and circular buffer
    // ---------------------------------------------------------------------

    @Test
    void statisticsHistoryIsEmptyBeforeFirstTick() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        StatisticsBatch batch = engine.getStatisticsFromIndex(0);

        assertTrue(batch.snapshots().isEmpty());
        assertEquals(0, batch.nextSampleIndex());

        engine.tick();

        StatisticsBatch afterTick = engine.getStatisticsFromIndex(0);

        assertEquals(1, afterTick.snapshots().size());
        assertEquals(1, afterTick.nextSampleIndex());
    }

    @Test
    void firstSnapshotCapturesWorldBeforeTickProcessing() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        Genome genome = createGenome(2.0, 1.0);

        createHerbivore(
                engine, 500.0, 400.0, 100.0,
                genome, Direction.NORTH
        );

        // This food will be eaten during the first tick.
        engine.getWorld().addFood(500.0, 400.0);

        engine.tick();

        SimulationSnapshot snapshot =
                engine.getStatisticsFromIndex(0).snapshots().get(0);

        assertEquals(1, snapshot.population().herbivores());
        assertEquals(0, snapshot.population().predators());
        assertEquals(1, snapshot.population().food());

        assertNotNull(snapshot.herbivores());
        assertNull(snapshot.predators());

        // The world changed after the snapshot was captured.
        assertTrue(engine.getWorld().getFoods().isEmpty());
    }

    @Test
    void samplesOnlyAtConfiguredTickIntervals() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        int interval = engine.getStatisticsIntervalTicks();

        assertTrue(interval > 0);

        // First tick creates the snapshot for Tick 0.
        engine.tick();

        assertEquals(
                1,
                engine.getStatisticsFromIndex(0).snapshots().size()
        );

        // Complete the remaining ticks up to Tick 50
        // (or the configured interval).
        for (int i = 1; i < interval; i++) {
            engine.tick();
        }

        assertEquals(interval, engine.getTickCount());

        // Tick 50 snapshot has not been captured yet.
        assertEquals(
                1,
                engine.getStatisticsFromIndex(0).snapshots().size()
        );

        // The next call captures the snapshot for Tick 50.
        engine.tick();

        assertEquals(
                2,
                engine.getStatisticsFromIndex(0).snapshots().size()
        );

        assertEquals(2, engine.getStatisticsFromIndex(0).nextSampleIndex());
    }

    @Test
    void returnsOnlySnapshotsFromRequestedIndex() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        int interval = engine.getStatisticsIntervalTicks();

        // Generate snapshots at Tick 0 and Tick 'interval'.
        for (int i = 0; i <= interval; i++) {
            engine.tick();
        }

        StatisticsBatch all = engine.getStatisticsFromIndex(0);
        StatisticsBatch newOnly = engine.getStatisticsFromIndex(1);
        StatisticsBatch none = engine.getStatisticsFromIndex(2);

        assertEquals(2, all.snapshots().size());
        assertEquals(2, all.nextSampleIndex());

        assertEquals(1, newOnly.snapshots().size());
        assertEquals(2, newOnly.nextSampleIndex());

        assertTrue(none.snapshots().isEmpty());
        assertEquals(2, none.nextSampleIndex());

        assertEquals(
                all.snapshots().get(1),
                newOnly.snapshots().get(0)
        );
    }

    @Test
    void rejectsInvalidStatisticsIndices() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> engine.getStatisticsFromIndex(-1)
        );

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> engine.getStatisticsFromIndex(1)
        );

        engine.tick();

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> engine.getStatisticsFromIndex(2)
        );

        // Index equal to nextSampleIndex is valid.
        StatisticsBatch batch = engine.getStatisticsFromIndex(1);

        assertTrue(batch.snapshots().isEmpty());
        assertEquals(1, batch.nextSampleIndex());
    }

    @Test
    void circularBufferKeepsOnlyNewestSnapshotsInOrder() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        int capacity = engine.getStatisticsHistoryCapacity();
        int interval = engine.getStatisticsIntervalTicks();

        assertEquals(
                10_000 / interval + 1,
                capacity
        );

        int totalSamples = capacity + 3;
        Food activeFood = null;

        for (int sample = 0; sample < totalSamples; sample++) {

            // Alternate between 1 and 0 food items
            // so we can verify chronological order.
            if (sample % 2 == 0) {
                activeFood = engine.getWorld().addFood(300, 300);
            } else {
                engine.getWorld().removeFood(activeFood);
                activeFood = null;
            }

            for (int tick = 0; tick < interval; tick++) {
                engine.tick();
            }
        }

        StatisticsBatch batch = engine.getStatisticsFromIndex(0);

        // Only the newest 'capacity' samples survive.
        assertEquals(capacity, batch.snapshots().size());
        assertEquals(totalSamples, batch.nextSampleIndex());

        int oldestAvailableIndex = totalSamples - capacity;

        // Verify all returned samples are chronologically ordered.
        for (int i = 0; i < capacity; i++) {
            int sampleIndex = oldestAvailableIndex + i;
            int expectedFood = sampleIndex % 2 == 0 ? 1 : 0;

            assertEquals(
                    expectedFood,
                    batch.snapshots().get(i).population().food(),
                    "Wrong food count at sample index " + sampleIndex
            );
        }

        // Requesting an overwritten index skips to oldest available.
        StatisticsBatch skipped = engine.getStatisticsFromIndex(1);

        assertEquals(capacity, skipped.snapshots().size());
        assertEquals(totalSamples, skipped.nextSampleIndex());

        // Requesting the last two samples returns exactly two.
        StatisticsBatch lastTwo =
                engine.getStatisticsFromIndex(totalSamples - 2);

        assertEquals(2, lastTwo.snapshots().size());
        assertEquals(totalSamples, lastTwo.nextSampleIndex());
    }

    @Test
    void returnedStatisticsBatchIsIndependentOfFutureUpdates() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        engine.tick();

        StatisticsBatch original = engine.getStatisticsFromIndex(0);

        assertEquals(1, original.snapshots().size());
        assertEquals(0, original.snapshots().get(0).population().food());

        engine.getWorld().addFood(300, 300);

        int interval = engine.getStatisticsIntervalTicks();

        for (int i = 0; i < interval; i++) {
            engine.tick();
        }

        // The previously returned batch must remain unchanged.
        assertEquals(1, original.snapshots().size());
        assertEquals(1, original.nextSampleIndex());
        assertEquals(0, original.snapshots().get(0).population().food());

        StatisticsBatch latest = engine.getStatisticsFromIndex(1);

        assertEquals(1, latest.snapshots().size());
        assertEquals(1, latest.snapshots().get(0).population().food());

        assertThrows(
                UnsupportedOperationException.class,
                () -> original.snapshots().clear()
        );
    }

    @Test
    void sameSeedProducesIdenticalStatisticsHistory() {
        SimulationConfig config =
                createConfig(98765L, 2, 1, 10);

        SimulationEngine first = new SimulationEngine(config);
        SimulationEngine second = new SimulationEngine(config);

        int ticks = first.getStatisticsIntervalTicks() * 2 + 1;

        for (int i = 0; i < ticks; i++) {
            first.tick();
            second.tick();
        }

        StatisticsBatch firstBatch = first.getStatisticsFromIndex(0);
        StatisticsBatch secondBatch = second.getStatisticsFromIndex(0);

        assertEquals(
                firstBatch.nextSampleIndex(),
                secondBatch.nextSampleIndex()
        );

        assertEquals(
                firstBatch.snapshots(),
                secondBatch.snapshots()
        );
    }


}
