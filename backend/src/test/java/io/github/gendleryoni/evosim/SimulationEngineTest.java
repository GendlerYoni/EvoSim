package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimulationEngineTest {

    private SimulationConfig createDefaultConfig() {
        return createConfig(12345L);
    }

    private SimulationConfig createConfig(long seed) {
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
                seed
        );
    }

    private Herbivore createHerbivore(
            SimulationEngine engine,
            double x,
            double y,
            double energy,
            Direction explorationDirection
    ) {
        return engine.createHerbivore(
                x,
                y,
                energy,
                1,
                engine.getConfig().getInitialGenome(),
                explorationDirection
        );
    }

    @Test
    void startsAtTickZero() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        assertEquals(0, engine.getTickCount());
    }

    @Test
    void tickAdvancesTickCount() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        engine.tick();

        assertEquals(1, engine.getTickCount());
    }

    @Test
    void multipleTicksAdvanceConsistently() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        for (int i = 0; i < 10_000; i++) {
            engine.tick();
        }

        assertEquals(10_000, engine.getTickCount());
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

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
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

        assertEquals(1, herbivore.getId());
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
                50.25,
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

        // BASE_SENSE_RADIUS = 15.0 for a baseline genome.
        engine.getWorld().addFood(
                65.0,
                50.0
        );

        engine.tick();

        assertEquals(
                50.25,
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

        engineWithFarFood.getWorld().addFood(
                70.0,
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
                                70.0,
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

        Herbivore herbivore = createHerbivore(
                engine,
                50.0,
                50.0,
                100.0,
                Direction.NORTH
        );

        // Herbivore radius = 1, Food radius = 3.
        // Distance 4 means the two circles touch exactly.
        engine.getWorld().addFood(
                54.0,
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
                                54.0,
                                50.0,
                                0.0
                        )
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

        Herbivore older = createHerbivore(
                engine,
                48.0,
                50.0,
                100.0,
                Direction.NORTH
        );

        Herbivore newer = createHerbivore(
                engine,
                52.0,
                50.0,
                100.0,
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
    }

    @Test
    void hittingWallChangesPersistentExplorationDirection() {
        SimulationEngine engine =
                new SimulationEngine(createDefaultConfig());

        Herbivore herbivore = createHerbivore(
                engine,
                50.0,
                1.0,
                100.0,
                Direction.NORTH
        );

        engine.tick();

        assertEquals(
                1.0,
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
                new SimulationEngine(createConfig(98765L));

        SimulationEngine secondEngine =
                new SimulationEngine(createConfig(98765L));

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

        Herbivore herbivore = createHerbivore(
                engine,
                500.0,
                400.0,
                20_000.0,
                Direction.NORTH
        );

        for (int i = 0; i < 10_000; i++) {
            engine.tick();

            double radius = herbivore.getRadius();

            assertTrue(
                    herbivore.getX() >= radius
                            && herbivore.getX()
                            <= engine.getWorld().getWidth() - radius
            );

            assertTrue(
                    herbivore.getY() >= radius
                            && herbivore.getY()
                            <= engine.getWorld().getHeight() - radius
            );
        }
    }
}