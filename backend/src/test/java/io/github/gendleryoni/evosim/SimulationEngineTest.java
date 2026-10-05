package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SimulationEngineTest {

    private SimulationConfig createDefaultConfig() {
        return createConfig(12345L);
    }

    private SimulationConfig createConfig(long seed) {
        return createConfig(
                seed,
                0,
                0
        );
    }

    private SimulationConfig createConfig(
            long seed,
            int initialHerbivores,
            int initialFood
    ) {
        Genome initialGenome = new Genome(
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                180.0
        );

        return new SimulationConfig(
                1000.0,
                800.0,
                initialGenome,
                initialHerbivores,
                initialFood,
                seed,
                0.0
        );
    }

    private Genome createGenome(
            double reproductionThreshold,
            double eggHatchTime
    ) {
        return new Genome(
                1.0,
                1.0,
                1.0,
                reproductionThreshold,
                eggHatchTime,
                180.0
        );
    }

    private Herbivore createHerbivore(
            SimulationEngine engine,
            double x,
            double y,
            double energy,
            Direction explorationDirection
    ) {
        return createHerbivore(
                engine,
                x,
                y,
                energy,
                engine.getConfig().getInitialGenome(),
                explorationDirection
        );
    }

    private Herbivore createHerbivore(
            SimulationEngine engine,
            double x,
            double y,
            double energy,
            Genome genome,
            Direction explorationDirection
    ) {
        return engine.createHerbivore(
                x,
                y,
                energy,
                1,
                genome,
                explorationDirection
        );
    }

    @Test
    void startsAtTickZero() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        assertEquals(
                0,
                engine.getTickCount()
        );
    }

    @Test
    void tickAdvancesTickCount() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        engine.tick();

        assertEquals(
                1,
                engine.getTickCount()
        );
    }

    @Test
    void multipleTicksAdvanceConsistently() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        for (int i = 0; i < 10_000; i++) {
            engine.tick();
        }

        assertEquals(
                10_000,
                engine.getTickCount()
        );
    }

    @Test
    void createsWorldFromConfig() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        assertEquals(
                1000.0,
                engine.getWorld().getWidth()
        );

        assertEquals(
                800.0,
                engine.getWorld().getHeight()
        );
    }

    @Test
    void rejectsNullConfig() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationEngine(null)
        );
    }

    @Test
    void simulationTimeAdvancesWithTicks() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        for (int i = 0; i < 20; i++) {
            engine.tick();
        }

        assertEquals(
                1.0,
                engine.getSimulationTimeSeconds(),
                1e-9
        );
    }

    @Test
    void createsConfiguredInitialPopulation() {
        SimulationEngine engine =
                new SimulationEngine(
                        createConfig(
                                12345L,
                                20,
                                100
                        )
                );

        assertEquals(
                20,
                engine.getWorld().getHerbivores().size()
        );

        assertEquals(
                100,
                engine.getWorld().getFoods().size()
        );
    }

    @Test
    void initialHerbivoresUseConfiguredTraitsAndInitialState() {
        SimulationEngine engine =
                new SimulationEngine(
                        createConfig(
                                12345L,
                                20,
                                0
                        )
                );

        Genome initialGenome =
                engine.getConfig().getInitialGenome();

        for (Herbivore herbivore :
                engine.getWorld().getHerbivores()) {

            Genome herbivoreGenome =
                    herbivore.getGenome();

            assertNotSame(
                    initialGenome,
                    herbivoreGenome
            );

            assertEquals(
                    initialGenome.getSpeed(),
                    herbivoreGenome.getSpeed(),
                    1e-9
            );

            assertEquals(
                    initialGenome.getSize(),
                    herbivoreGenome.getSize(),
                    1e-9
            );

            assertEquals(
                    initialGenome.getSenseRadius(),
                    herbivoreGenome.getSenseRadius(),
                    1e-9
            );

            assertEquals(
                    initialGenome.getReproductionThreshold(),
                    herbivoreGenome.getReproductionThreshold(),
                    1e-9
            );

            assertEquals(
                    initialGenome.getEggHatchTime(),
                    herbivoreGenome.getEggHatchTime(),
                    1e-9
            );

            assertTrue(
                    herbivoreGenome.getHue() >= 0.0
                            && herbivoreGenome.getHue() < 360.0
            );

            assertEquals(
                    100.0,
                    herbivore.getEnergy(),
                    1e-9
            );

            assertEquals(
                    1,
                    herbivore.getGeneration()
            );

            assertNotNull(
                    herbivore.getExplorationDirection()
            );
        }
    }

    @Test
    void initialHerbivoresAreCreatedInsideWorldBounds() {
        SimulationEngine engine =
                new SimulationEngine(
                        createConfig(
                                12345L,
                                100,
                                0
                        )
                );

        double worldWidth =
                engine.getWorld().getWidth();

        double worldHeight =
                engine.getWorld().getHeight();

        for (Herbivore herbivore :
                engine.getWorld().getHerbivores()) {

            double radius =
                    herbivore.getRadius();

            assertTrue(
                    herbivore.getX() >= radius
                            && herbivore.getX()
                            <= worldWidth - radius
            );

            assertTrue(
                    herbivore.getY() >= radius
                            && herbivore.getY()
                            <= worldHeight - radius
            );
        }
    }

    @Test
    void initialFoodIsCreatedInsideWorldBounds() {
        SimulationEngine engine =
                new SimulationEngine(
                        createConfig(
                                12345L,
                                0,
                                100
                        )
                );

        double worldWidth =
                engine.getWorld().getWidth();

        double worldHeight =
                engine.getWorld().getHeight();

        for (Food food : engine.getWorld().getFoods()) {
            double radius =
                    food.getRadius();

            assertTrue(
                    food.getX() >= radius
                            && food.getX()
                            <= worldWidth - radius
            );

            assertTrue(
                    food.getY() >= radius
                            && food.getY()
                            <= worldHeight - radius
            );
        }
    }

    @Test
    void sameSeedProducesSameInitialWorld() {
        SimulationEngine firstEngine =
                new SimulationEngine(
                        createConfig(
                                98765L,
                                20,
                                100
                        )
                );

        SimulationEngine secondEngine =
                new SimulationEngine(
                        createConfig(
                                98765L,
                                20,
                                100
                        )
                );

        List<Herbivore> firstHerbivores =
                firstEngine.getWorld().getHerbivores();

        List<Herbivore> secondHerbivores =
                secondEngine.getWorld().getHerbivores();

        assertEquals(
                firstHerbivores.size(),
                secondHerbivores.size()
        );

        for (int i = 0; i < firstHerbivores.size(); i++) {
            Herbivore first =
                    firstHerbivores.get(i);

            Herbivore second =
                    secondHerbivores.get(i);

            assertEquals(
                    first.getId(),
                    second.getId()
            );

            assertEquals(
                    first.getX(),
                    second.getX(),
                    1e-9
            );

            assertEquals(
                    first.getY(),
                    second.getY(),
                    1e-9
            );

            assertEquals(
                    first.getExplorationDirection(),
                    second.getExplorationDirection()
            );
        }

        List<Food> firstFoods =
                firstEngine.getWorld().getFoods();

        List<Food> secondFoods =
                secondEngine.getWorld().getFoods();

        assertEquals(
                firstFoods.size(),
                secondFoods.size()
        );

        for (int i = 0; i < firstFoods.size(); i++) {
            Food first =
                    firstFoods.get(i);

            Food second =
                    secondFoods.get(i);

            assertEquals(
                    first.getX(),
                    second.getX(),
                    1e-9
            );

            assertEquals(
                    first.getY(),
                    second.getY(),
                    1e-9
            );
        }
    }

    @Test
    void createsHerbivoreAndAddsItToWorld() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        Herbivore herbivore = createHerbivore(
                engine,
                50.0,
                50.0,
                100.0,
                Direction.NORTH
        );

        assertEquals(
                1,
                engine.getWorld().getHerbivores().size()
        );

        assertSame(
                herbivore,
                engine.getWorld().getHerbivores().get(0)
        );
    }

    @Test
    void assignsUniqueSequentialCreatureIds() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        Herbivore first = createHerbivore(
                engine,
                50.0,
                50.0,
                100.0,
                Direction.NORTH
        );

        Herbivore second = createHerbivore(
                engine,
                60.0,
                60.0,
                100.0,
                Direction.SOUTH
        );

        assertEquals(
                1,
                first.getId()
        );

        assertEquals(
                2,
                second.getId()
        );
    }

    @Test
    void failedCreatureCreationDoesNotConsumeId() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        assertThrows(
                IllegalArgumentException.class,
                () -> createHerbivore(
                        engine,
                        0.0,
                        0.0,
                        100.0,
                        Direction.NORTH
                )
        );

        Herbivore herbivore = createHerbivore(
                engine,
                50.0,
                50.0,
                100.0,
                Direction.NORTH
        );

        assertEquals(
                1,
                herbivore.getId()
        );
    }

    @Test
    void foodInsideSenseRadiusInfluencesMovement() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        Herbivore herbivore = createHerbivore(
                engine,
                50.0,
                50.0,
                100.0,
                Direction.NORTH
        );

        engine.getWorld().addFood(
                60.0,
                50.0
        );

        engine.tick();

        assertEquals(
                51.25,
                herbivore.getX(),
                1e-9
        );

        assertEquals(
                50.0,
                herbivore.getY(),
                1e-9
        );
    }

    @Test
    void foodExactlyOnSenseBoundaryInfluencesMovement() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        Herbivore herbivore = createHerbivore(
                engine,
                50.0,
                50.0,
                100.0,
                Direction.NORTH
        );

        // BASE_SENSE_RADIUS = 100.0 for a baseline genome.
        engine.getWorld().addFood(
                150.0,
                50.0
        );

        engine.tick();

        assertEquals(
                51.25,
                herbivore.getX(),
                1e-9
        );

        assertEquals(
                50.0,
                herbivore.getY(),
                1e-9
        );
    }

    @Test
    void foodOutsideSenseRadiusDoesNotInfluenceMovement() {
        SimulationEngine engineWithoutFood =
                new SimulationEngine(createDefaultConfig());

        SimulationEngine engineWithFarFood =
                new SimulationEngine(createDefaultConfig());

        Herbivore withoutFood = createHerbivore(
                engineWithoutFood,
                50.0,
                50.0,
                100.0,
                Direction.NORTH
        );

        Herbivore withFarFood = createHerbivore(
                engineWithFarFood,
                50.0,
                50.0,
                100.0,
                Direction.NORTH
        );

        // Distance = 101, just outside the baseline sense radius of 100.
        engineWithFarFood.getWorld().addFood(
                151.0,
                50.0
        );

        engineWithoutFood.tick();
        engineWithFarFood.tick();

        assertEquals(
                withoutFood.getX(),
                withFarFood.getX(),
                1e-9
        );

        assertEquals(
                withoutFood.getY(),
                withFarFood.getY(),
                1e-9
        );

        assertEquals(
                1,
                engineWithFarFood
                        .getWorld()
                        .findFoodWithinRadius(
                                151.0,
                                50.0,
                                0.0
                        )
                        .size()
        );
    }

    @Test
    void overlappingFoodIsEatenAndAddsEnergy() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        // High reproduction threshold keeps this test focused only
        // on eating and Energy gain.
        Genome genome = createGenome(
                4.0,
                1.0
        );

        Herbivore herbivore = createHerbivore(
                engine,
                50.0,
                50.0,
                100.0,
                genome,
                Direction.NORTH
        );

        // Herbivore radius = 6, Food radius = 3.
        // Distance 9 means the two circles touch exactly.
        engine.getWorld().addFood(
                59.0,
                50.0
        );

        engine.tick();

        assertEquals(
                199.0,
                herbivore.getEnergy(),
                1e-9
        );

        assertTrue(
                engine.getWorld()
                        .findFoodWithinRadius(
                                59.0,
                                50.0,
                                0.0
                        )
                        .isEmpty()
        );

        assertTrue(
                engine.getWorld()
                        .getHerbivoreEggs()
                        .isEmpty()
        );
    }

    @Test
    void herbivoreReachingZeroEnergySurvivesUntilNextTick() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        Herbivore herbivore = createHerbivore(
                engine,
                50.0,
                50.0,
                1.0,
                Direction.NORTH
        );

        engine.tick();

        assertEquals(
                0.0,
                herbivore.getEnergy(),
                1e-9
        );

        assertEquals(
                1,
                engine.getWorld().getHerbivores().size()
        );

        engine.tick();

        assertTrue(
                engine.getWorld().getHerbivores().isEmpty()
        );
    }

    @Test
    void herbivoreAtZeroEnergyCanBeRescuedByOverlappingFood() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        Herbivore herbivore = createHerbivore(
                engine,
                50.0,
                50.0,
                1.0,
                Direction.NORTH
        );

        engine.tick();

        assertEquals(
                0.0,
                herbivore.getEnergy(),
                1e-9
        );

        engine.getWorld().addFood(
                herbivore.getX(),
                herbivore.getY()
        );

        engine.tick();

        assertEquals(
                99.0,
                herbivore.getEnergy(),
                1e-9
        );

        assertEquals(
                1,
                engine.getWorld().getHerbivores().size()
        );

        assertSame(
                herbivore,
                engine.getWorld().getHerbivores().get(0)
        );
    }

    @Test
    void newerHerbivoreGetsPriorityInFoodConflict() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        // Prevent reproduction from affecting this interaction-order test.
        Genome genome = createGenome(
                4.0,
                1.0
        );

        Herbivore older = createHerbivore(
                engine,
                48.0,
                50.0,
                100.0,
                genome,
                Direction.NORTH
        );

        Herbivore newer = createHerbivore(
                engine,
                52.0,
                50.0,
                100.0,
                genome,
                Direction.NORTH
        );

        engine.getWorld().addFood(
                50.0,
                50.0
        );

        engine.tick();

        assertEquals(
                99.0,
                older.getEnergy(),
                1e-9
        );

        assertEquals(
                199.0,
                newer.getEnergy(),
                1e-9
        );

        assertTrue(
                engine.getWorld()
                        .getHerbivoreEggs()
                        .isEmpty()
        );
    }

    @Test
    void hittingWallChangesPersistentExplorationDirection() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        Herbivore herbivore = createHerbivore(
                engine,
                50.0,
                6.0,
                100.0,
                Direction.NORTH
        );

        engine.tick();

        assertEquals(
                6.0,
                herbivore.getY(),
                1e-9
        );

        assertNotEquals(
                Direction.NORTH,
                herbivore.getExplorationDirection()
        );
    }

    @Test
    void sameSeedProducesSameMovementSequence() {
        SimulationEngine firstEngine =
                new SimulationEngine(
                        createConfig(98765L)
                );

        SimulationEngine secondEngine =
                new SimulationEngine(
                        createConfig(98765L)
                );

        Herbivore first = createHerbivore(
                firstEngine,
                500.0,
                400.0,
                100.0,
                Direction.NORTH
        );

        Herbivore second = createHerbivore(
                secondEngine,
                500.0,
                400.0,
                100.0,
                Direction.NORTH
        );

        for (int i = 0; i < 20; i++) {
            firstEngine.tick();
            secondEngine.tick();
        }

        assertEquals(
                first.getX(),
                second.getX(),
                1e-9
        );

        assertEquals(
                first.getY(),
                second.getY(),
                1e-9
        );

        assertEquals(
                first.getEnergy(),
                second.getEnergy(),
                1e-9
        );

        assertEquals(
                first.getExplorationDirection(),
                second.getExplorationDirection()
        );
    }

    @Test
    void herbivoreRemainsInsideWorldForThousandsOfTicks() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        // Threshold = 800, while Energy is periodically replenished
        // below that value. This keeps reproduction out of a test that
        // exists only to verify movement bounds.
        Genome genome = createGenome(
                4.0,
                1.0
        );

        Herbivore herbivore = createHerbivore(
                engine,
                500.0,
                400.0,
                700.0,
                genome,
                Direction.NORTH
        );

        for (int i = 0; i < 10_000; i++) {
            engine.tick();

            double radius =
                    herbivore.getRadius();

            assertTrue(
                    herbivore.getX() >= radius
                            && herbivore.getX()
                            <= engine.getWorld().getWidth()
                            - radius
            );

            assertTrue(
                    herbivore.getY() >= radius
                            && herbivore.getY()
                            <= engine.getWorld().getHeight()
                            - radius
            );

            if ((i + 1) % 500 == 0) {
                herbivore.addEnergy(500.0);
            }
        }
    }

    @Test
    void tickConsumesEnergyAccordingToGenome() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        Genome genome = new Genome(
                4.0,   // speed
                1.0,   // size
                1.0,   // sense radius
                1.0,
                1.0,
                180.0
        );

        Herbivore herbivore =
                engine.createHerbivore(
                        500.0,
                        400.0,
                        100.0,
                        1,
                        genome,
                        Direction.NORTH
                );

        engine.tick();

        assertEquals(
                98.0,
                herbivore.getEnergy(),
                1e-9
        );
    }

    // ---------------------------------------------------------------------
    // M8 - Eggs and asexual reproduction
    // ---------------------------------------------------------------------

    @Test
    void herbivoreAtThresholdLaysEggAndPaysReproductionCost() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        Herbivore parent = createHerbivore(
                engine,
                500.0,
                400.0,
                200.0,
                Direction.NORTH
        );

        double layingX = parent.getX();
        double layingY = parent.getY();

        engine.tick();

        assertEquals(
                1,
                engine.getWorld().getHerbivoreEggs().size()
        );

        Egg egg =
                engine.getWorld().getHerbivoreEggs().get(0);

        assertEquals(
                layingX,
                egg.getX(),
                1e-9
        );

        assertEquals(
                layingY,
                egg.getY(),
                1e-9
        );

        assertEquals(
                2,
                egg.getGeneration()
        );

        // A newly laid Egg must not lose one hatch tick
        // during the tick in which it was created.
        assertEquals(
                50,
                egg.getRemainingHatchTicks()
        );

        // 200 - 50 reproduction cost - 1 normal tick cost.
        assertEquals(
                149.0,
                parent.getEnergy(),
                1e-9
        );

        assertEquals(
                50,
                parent.getReproductionCooldownTicksRemaining()
        );

        assertFalse(
                parent.isReproductionCooldownComplete()
        );
    }

    @Test
    void reproductionCreatesIndependentChildGenomeWithSameValues() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        Genome parentGenome =
                engine.getConfig().getInitialGenome();

        createHerbivore(
                engine,
                500.0,
                400.0,
                200.0,
                parentGenome,
                Direction.NORTH
        );

        engine.tick();

        Egg egg =
                engine.getWorld().getHerbivoreEggs().get(0);

        Genome childGenome =
                egg.getGenome();

        assertNotSame(
                parentGenome,
                childGenome
        );

        assertEquals(
                parentGenome.getSpeed(),
                childGenome.getSpeed(),
                1e-9
        );

        assertEquals(
                parentGenome.getSize(),
                childGenome.getSize(),
                1e-9
        );

        assertEquals(
                parentGenome.getSenseRadius(),
                childGenome.getSenseRadius(),
                1e-9
        );

        assertEquals(
                parentGenome.getReproductionThreshold(),
                childGenome.getReproductionThreshold(),
                1e-9
        );

        assertEquals(
                parentGenome.getEggHatchTime(),
                childGenome.getEggHatchTime(),
                1e-9
        );

        assertEquals(
                parentGenome.getHue(),
                childGenome.getHue(),
                1e-9
        );
    }

    @Test
    void herbivoreBelowThresholdDoesNotReproduce() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        createHerbivore(
                engine,
                500.0,
                400.0,
                199.0,
                Direction.NORTH
        );

        engine.tick();

        assertTrue(
                engine.getWorld()
                        .getHerbivoreEggs()
                        .isEmpty()
        );
    }

    @Test
    void eatingCanTriggerReproductionInSameTick() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        Herbivore herbivore = createHerbivore(
                engine,
                500.0,
                400.0,
                100.0,
                Direction.NORTH
        );

        // Exactly touching:
        // Herbivore radius 6 + Food radius 3 = 9.
        engine.getWorld().addFood(
                509.0,
                400.0
        );

        engine.tick();

        assertEquals(
                1,
                engine.getWorld().getHerbivoreEggs().size()
        );

        // 100 + 100 food - 50 reproduction - 1 tick cost.
        assertEquals(
                149.0,
                herbivore.getEnergy(),
                1e-9
        );

        assertTrue(
                engine.getWorld().getFoods().isEmpty()
        );
    }

    @Test
    void reproductionCooldownPreventsNewEggUntilFiftyTicksPass() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        // Long hatch time prevents the first Egg from hatching
        // while this test checks only reproduction cooldown.
        Genome genome = createGenome(
                1.0,
                4.0
        );

        Herbivore herbivore = createHerbivore(
                engine,
                500.0,
                400.0,
                1000.0,
                genome,
                Direction.NORTH
        );

        engine.tick();

        assertEquals(
                1,
                engine.getWorld().getHerbivoreEggs().size()
        );

        assertEquals(
                50,
                herbivore.getReproductionCooldownTicksRemaining()
        );

        for (int i = 0; i < 49; i++) {
            engine.tick();
        }

        assertEquals(
                1,
                engine.getWorld().getHerbivoreEggs().size()
        );

        assertEquals(
                1,
                herbivore.getReproductionCooldownTicksRemaining()
        );

        engine.tick();

        assertEquals(
                2,
                engine.getWorld().getHerbivoreEggs().size()
        );

        assertEquals(
                50,
                herbivore.getReproductionCooldownTicksRemaining()
        );
    }

    @Test
    void eggHatchesAfterExactCountdown() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        Genome genome =
                engine.getConfig().getInitialGenome();

        Egg egg = engine.getWorld().addHerbivoreEgg(
                500.0,
                400.0,
                genome,
                2,
                2
        );

        engine.tick();

        assertEquals(
                1,
                egg.getRemainingHatchTicks()
        );

        assertEquals(
                1,
                engine.getWorld().getHerbivoreEggs().size()
        );

        assertTrue(
                engine.getWorld().getHerbivores().isEmpty()
        );

        engine.tick();

        assertTrue(
                engine.getWorld().getHerbivoreEggs().isEmpty()
        );

        assertEquals(
                1,
                engine.getWorld().getHerbivores().size()
        );

        Herbivore child =
                engine.getWorld().getHerbivores().get(0);

        assertEquals(
                500.0,
                child.getX(),
                1e-9
        );

        assertEquals(
                400.0,
                child.getY(),
                1e-9
        );

        assertEquals(
                100.0,
                child.getEnergy(),
                1e-9
        );

        assertEquals(
                2,
                child.getGeneration()
        );

        assertSame(
                genome,
                child.getGenome()
        );

        assertNotNull(
                child.getExplorationDirection()
        );
    }

    @Test
    void hatchedHerbivoreDoesNotActUntilFollowingTick() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        engine.getWorld().addHerbivoreEgg(
                500.0,
                400.0,
                engine.getConfig().getInitialGenome(),
                2,
                1
        );

        engine.tick();

        Herbivore child =
                engine.getWorld().getHerbivores().get(0);

        // If the child had been processed immediately after hatching,
        // it would already have paid its normal tick Energy cost.
        assertEquals(
                100.0,
                child.getEnergy(),
                1e-9
        );

        assertEquals(
                500.0,
                child.getX(),
                1e-9
        );

        assertEquals(
                400.0,
                child.getY(),
                1e-9
        );

        engine.tick();

        assertEquals(
                99.0,
                child.getEnergy(),
                1e-9
        );
    }

    @Test
    void parentDeathDoesNotRemoveItsEgg() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        // Threshold = 50 Energy.
        // Hatch time = round(50 * 0.25) = 13 ticks.
        Genome genome = createGenome(
                0.25,
                0.25
        );

        Herbivore parent = createHerbivore(
                engine,
                500.0,
                400.0,
                50.0,
                genome,
                Direction.NORTH
        );

        engine.tick();

        assertEquals(
                1,
                engine.getWorld().getHerbivoreEggs().size()
        );

        assertTrue(
                parent.isDead()
        );

        // The parent is removed on its next processing tick.
        engine.tick();

        assertTrue(
                engine.getWorld().getHerbivores().isEmpty()
        );

        assertEquals(
                1,
                engine.getWorld().getHerbivoreEggs().size()
        );

        // The Egg had 13 ticks initially and has now advanced once.
        assertEquals(
                12,
                engine.getWorld()
                        .getHerbivoreEggs()
                        .get(0)
                        .getRemainingHatchTicks()
        );

        for (int i = 0; i < 12; i++) {
            engine.tick();
        }

        assertTrue(
                engine.getWorld().getHerbivoreEggs().isEmpty()
        );

        assertEquals(
                1,
                engine.getWorld().getHerbivores().size()
        );

        Herbivore child =
                engine.getWorld().getHerbivores().get(0);

        assertEquals(
                2,
                child.getGeneration()
        );

        assertEquals(
                100.0,
                child.getEnergy(),
                1e-9
        );
    }

    @Test
    void supportsMultipleGenerations() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        // Low threshold and short hatch time make a deterministic,
        // compact multi-generation test possible.
        Genome genome = createGenome(
                0.25,
                0.25
        );

        createHerbivore(
                engine,
                500.0,
                400.0,
                50.0,
                genome,
                Direction.NORTH
        );

        // Generation 1 lays a Generation 2 Egg.
        engine.tick();

        assertEquals(
                2,
                engine.getWorld()
                        .getHerbivoreEggs()
                        .get(0)
                        .getGeneration()
        );

        // The Generation 2 Egg needs 13 later ticks to hatch.
        for (int i = 0; i < 13; i++) {
            engine.tick();
        }

        assertEquals(
                1,
                engine.getWorld().getHerbivores().size()
        );

        Herbivore generationTwo =
                engine.getWorld().getHerbivores().get(0);

        assertEquals(
                2,
                generationTwo.getGeneration()
        );

        // The newly hatched Generation 2 creature acts on the next tick.
        // It starts with 100 Energy and has a threshold of 50,
        // so it lays a Generation 3 Egg.
        engine.tick();

        assertEquals(
                1,
                engine.getWorld().getHerbivoreEggs().size()
        );

        assertEquals(
                3,
                engine.getWorld()
                        .getHerbivoreEggs()
                        .get(0)
                        .getGeneration()
        );

        // Hatch Generation 3.
        for (int i = 0; i < 13; i++) {
            engine.tick();
        }

        assertTrue(
                engine.getWorld().getHerbivoreEggs().isEmpty()
        );

        assertEquals(
                2,
                engine.getWorld().getHerbivores().size()
        );

        boolean foundGenerationTwo = false;
        boolean foundGenerationThree = false;

        for (Herbivore herbivore :
                engine.getWorld().getHerbivores()) {

            if (herbivore.getGeneration() == 2) {
                foundGenerationTwo = true;
            }

            if (herbivore.getGeneration() == 3) {
                foundGenerationThree = true;
            }
        }

        assertTrue(foundGenerationTwo);
        assertTrue(foundGenerationThree);
    }

    private SimulationConfig createConfig(
            long seed,
            int initialHerbivores,
            int initialFood,
            double mutationStrength
    ) {
        return new SimulationConfig(
                1000.0,
                800.0,
                new Genome(
                        1.0,
                        1.0,
                        1.0,
                        1.0,
                        1.0,
                        180.0
                ),
                initialHerbivores,
                initialFood,
                seed,
                mutationStrength
        );
    }

    @Test
    void reproductionUsesMutatedChildGenome() {
        long seed = 12345L;
        double mutationStrength = 0.05;

        SimulationEngine engine =
                new SimulationEngine(
                        createConfig(
                                seed,
                                0,
                                0,
                                mutationStrength
                        )
                );

        Genome parentGenome = new Genome(
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                180.0
        );

        Herbivore parent = engine.createHerbivore(
                500.0,
                400.0,
                200.0,
                1,
                parentGenome,
                Direction.EAST
        );

        /*
         * No initial entities were created, so the Engine's Random
         * has not been consumed yet. A separate Random with the same
         * seed should therefore produce the same child mutation.
         */
        Random expectedRandom = new Random(seed);

        Genome expectedChildGenome =
                parentGenome.mutate(
                        mutationStrength,
                        expectedRandom
                );

        engine.tick();

        assertEquals(
                1,
                engine.getWorld().getHerbivoreEggs().size()
        );

        Genome actualChildGenome =
                engine.getWorld()
                        .getHerbivoreEggs()
                        .get(0)
                        .getGenome();

        assertNotSame(
                parentGenome,
                actualChildGenome
        );

        assertEquals(
                expectedChildGenome.getSpeed(),
                actualChildGenome.getSpeed(),
                1e-9
        );

        assertEquals(
                expectedChildGenome.getSize(),
                actualChildGenome.getSize(),
                1e-9
        );

        assertEquals(
                expectedChildGenome.getSenseRadius(),
                actualChildGenome.getSenseRadius(),
                1e-9
        );

        assertEquals(
                expectedChildGenome.getReproductionThreshold(),
                actualChildGenome.getReproductionThreshold(),
                1e-9
        );

        assertEquals(
                expectedChildGenome.getEggHatchTime(),
                actualChildGenome.getEggHatchTime(),
                1e-9
        );

        assertEquals(
                expectedChildGenome.getHue(),
                actualChildGenome.getHue(),
                1e-9
        );
    }

    @Test
    void sameSeedProducesSameChildMutation() {
        long seed = 98765L;
        double mutationStrength = 0.05;

        SimulationEngine first =
                new SimulationEngine(
                        createConfig(
                                seed,
                                0,
                                0,
                                mutationStrength
                        )
                );

        SimulationEngine second =
                new SimulationEngine(
                        createConfig(
                                seed,
                                0,
                                0,
                                mutationStrength
                        )
                );

        Genome firstParentGenome = new Genome(
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                180.0
        );

        Genome secondParentGenome = new Genome(
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                180.0
        );

        first.createHerbivore(
                500.0,
                400.0,
                200.0,
                1,
                firstParentGenome,
                Direction.EAST
        );

        second.createHerbivore(
                500.0,
                400.0,
                200.0,
                1,
                secondParentGenome,
                Direction.EAST
        );

        first.tick();
        second.tick();

        Egg firstEgg =
                first.getWorld()
                        .getHerbivoreEggs()
                        .get(0);

        Egg secondEgg =
                second.getWorld()
                        .getHerbivoreEggs()
                        .get(0);

        Genome firstChild =
                firstEgg.getGenome();

        Genome secondChild =
                secondEgg.getGenome();

        assertEquals(firstChild.getSpeed(), secondChild.getSpeed());
        assertEquals(firstChild.getSize(), secondChild.getSize());
        assertEquals(firstChild.getSenseRadius(), secondChild.getSenseRadius());
        assertEquals(
                firstChild.getReproductionThreshold(),
                secondChild.getReproductionThreshold()
        );
        assertEquals(
                firstChild.getEggHatchTime(),
                secondChild.getEggHatchTime()
        );
        assertEquals(firstChild.getHue(), secondChild.getHue());

        assertEquals(
                firstEgg.getRemainingHatchTicks(),
                secondEgg.getRemainingHatchTicks()
        );
    }

    @Test
    void eggHatchTimeIsDerivedFromChildGenome() {
        long seed = 12345L;
        double mutationStrength = 0.05;

        SimulationEngine engine =
                new SimulationEngine(
                        createConfig(
                                seed,
                                0,
                                0,
                                mutationStrength
                        )
                );

        Genome parentGenome = new Genome(
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                180.0
        );

        engine.createHerbivore(
                500.0,
                400.0,
                200.0,
                1,
                parentGenome,
                Direction.EAST
        );

        engine.tick();

        Egg egg =
                engine.getWorld()
                        .getHerbivoreEggs()
                        .get(0);

        int expectedHatchTicks =
                Creature.calculateEggHatchTicks(
                        egg.getGenome()
                );

        assertEquals(
                expectedHatchTicks,
                egg.getRemainingHatchTicks()
        );
    }

    @Test
    void sameSeedProducesSameInitialGenomes() {
        long seed = 12345L;
        double mutationStrength = 0.05;

        SimulationEngine first =
                new SimulationEngine(
                        createConfig(
                                seed,
                                10,
                                0,
                                mutationStrength
                        )
                );

        SimulationEngine second =
                new SimulationEngine(
                        createConfig(
                                seed,
                                10,
                                0,
                                mutationStrength
                        )
                );

        List<Herbivore> firstHerbivores =
                first.getWorld().getHerbivores();

        List<Herbivore> secondHerbivores =
                second.getWorld().getHerbivores();

        assertEquals(
                firstHerbivores.size(),
                secondHerbivores.size()
        );

        for (int i = 0; i < firstHerbivores.size(); i++) {
            Genome firstGenome =
                    firstHerbivores.get(i).getGenome();

            Genome secondGenome =
                    secondHerbivores.get(i).getGenome();

            assertEquals(
                    firstGenome.getSpeed(),
                    secondGenome.getSpeed()
            );

            assertEquals(
                    firstGenome.getSize(),
                    secondGenome.getSize()
            );

            assertEquals(
                    firstGenome.getSenseRadius(),
                    secondGenome.getSenseRadius()
            );

            assertEquals(
                    firstGenome.getReproductionThreshold(),
                    secondGenome.getReproductionThreshold()
            );

            assertEquals(
                    firstGenome.getEggHatchTime(),
                    secondGenome.getEggHatchTime()
            );

            assertEquals(
                    firstGenome.getHue(),
                    secondGenome.getHue()
            );
        }
    }
}