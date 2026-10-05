package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreatureTest {

    private Genome createGenome() {
        return new Genome(
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                180.0
        );
    }

    private Herbivore createHerbivore() {
        return new Herbivore(
                1,
                50.0,
                75.0,
                100.0,
                1,
                createGenome(),
                Direction.NORTH
        );
    }

    @Test
    void createsHerbivoreWithValidState() {
        Genome genome = createGenome();

        Herbivore herbivore = new Herbivore(
                1,
                50.0,
                75.0,
                100.0,
                1,
                genome,
                Direction.NORTH
        );

        assertEquals(1, herbivore.getId());
        assertEquals(50.0, herbivore.getX());
        assertEquals(75.0, herbivore.getY());
        assertEquals(100.0, herbivore.getEnergy());
        assertEquals(1, herbivore.getGeneration());
        assertSame(genome, herbivore.getGenome());

        assertEquals(
                Direction.NORTH,
                herbivore.getExplorationDirection()
        );
    }

    @Test
    void calculatesPhysicalRadiusFromGenomeSize() {
        Herbivore herbivore = createHerbivore();

        assertEquals(
                6.0,
                herbivore.getRadius(),
                1e-9
        );
    }

    @Test
    void calculatesPhysicalSpeedFromGenomeSpeed() {
        Herbivore herbivore = createHerbivore();

        assertEquals(
                25.0,
                herbivore.getSpeed(),
                1e-9
        );
    }

    @Test
    void calculatesPhysicalSenseRadiusFromGenome() {
        Genome genome = new Genome(
                1.0,
                1.0,
                2.0,
                1.0,
                1.0,
                180.0
        );

        Herbivore herbivore = new Herbivore(
                1,
                50.0,
                75.0,
                100.0,
                1,
                genome,
                Direction.NORTH
        );

        assertEquals(
                200.0,
                herbivore.getSenseRadius(),
                1e-9
        );
    }

    @Test
    void calculatesMovementDistanceFromTickDuration() {
        Herbivore herbivore = createHerbivore();

        double distance =
                herbivore.getMovementDistance(0.05);

        assertEquals(
                1.25,
                distance,
                1e-9
        );
    }

    @Test
    void movesToNewPosition() {
        Herbivore herbivore = createHerbivore();

        herbivore.moveTo(60.0, 80.0);

        assertEquals(60.0, herbivore.getX());
        assertEquals(80.0, herbivore.getY());
    }

    @Test
    void changesExplorationDirection() {
        Herbivore herbivore = createHerbivore();

        herbivore.changeExplorationDirection(
                Direction.SOUTH_EAST
        );

        assertEquals(
                Direction.SOUTH_EAST,
                herbivore.getExplorationDirection()
        );
    }

    @Test
    void rejectsNonPositiveId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Herbivore(
                        0,
                        50.0,
                        75.0,
                        100.0,
                        1,
                        createGenome(),
                        Direction.NORTH
                )
        );
    }

    @Test
    void rejectsNonFinitePosition() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Herbivore(
                        1,
                        Double.NaN,
                        75.0,
                        100.0,
                        1,
                        createGenome(),
                        Direction.NORTH
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Herbivore(
                        1,
                        50.0,
                        Double.POSITIVE_INFINITY,
                        100.0,
                        1,
                        createGenome(),
                        Direction.NORTH
                )
        );
    }

    @Test
    void rejectsNonPositiveOrNonFiniteEnergy() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Herbivore(
                        1,
                        50.0,
                        75.0,
                        0.0,
                        1,
                        createGenome(),
                        Direction.NORTH
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Herbivore(
                        1,
                        50.0,
                        75.0,
                        Double.NaN,
                        1,
                        createGenome(),
                        Direction.NORTH
                )
        );
    }

    @Test
    void rejectsGenerationBelowOne() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Herbivore(
                        1,
                        50.0,
                        75.0,
                        100.0,
                        0,
                        createGenome(),
                        Direction.NORTH
                )
        );
    }

    @Test
    void rejectsNullGenome() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Herbivore(
                        1,
                        50.0,
                        75.0,
                        100.0,
                        1,
                        null,
                        Direction.NORTH
                )
        );
    }

    @Test
    void rejectsNullExplorationDirection() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Herbivore(
                        1,
                        50.0,
                        75.0,
                        100.0,
                        1,
                        createGenome(),
                        null
                )
        );
    }

    @Test
    void rejectsInvalidMovementDistanceTickDuration() {
        Herbivore herbivore = createHerbivore();

        assertThrows(
                IllegalArgumentException.class,
                () -> herbivore.getMovementDistance(-0.01)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> herbivore.getMovementDistance(Double.NaN)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> herbivore.getMovementDistance(
                        Double.POSITIVE_INFINITY
                )
        );
    }

    @Test
    void rejectsMovingToNonFinitePosition() {
        Herbivore herbivore = createHerbivore();

        assertThrows(
                IllegalArgumentException.class,
                () -> herbivore.moveTo(
                        Double.NaN,
                        50.0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> herbivore.moveTo(
                        50.0,
                        Double.POSITIVE_INFINITY
                )
        );
    }

    @Test
    void rejectsNullExplorationDirectionChange() {
        Herbivore herbivore = createHerbivore();

        assertThrows(
                IllegalArgumentException.class,
                () -> herbivore.changeExplorationDirection(null)
        );
    }

    @Test
    void addsEnergy() {
        Herbivore herbivore = createHerbivore();

        herbivore.addEnergy(50.0);

        assertEquals(
                150.0,
                herbivore.getEnergy(),
                1e-9
        );
    }

    @Test
    void consumesEnergy() {
        Herbivore herbivore = createHerbivore();

        herbivore.consumeEnergy(25.0);

        assertEquals(
                75.0,
                herbivore.getEnergy(),
                1e-9
        );
    }

    @Test
    void energyCanFallBelowZero() {
        Herbivore herbivore = createHerbivore();

        herbivore.consumeEnergy(150.0);

        assertEquals(
                -50.0,
                herbivore.getEnergy(),
                1e-9
        );
    }

    @Test
    void creatureIsDeadWhenEnergyReachesZero() {
        Herbivore herbivore = createHerbivore();

        herbivore.consumeEnergy(100.0);

        assertTrue(herbivore.isDead());
    }

    @Test
    void creatureIsDeadWhenEnergyFallsBelowZero() {
        Herbivore herbivore = createHerbivore();

        herbivore.consumeEnergy(101.0);

        assertTrue(herbivore.isDead());
    }

    @Test
    void creatureIsAliveWithPositiveEnergy() {
        Herbivore herbivore = createHerbivore();

        assertFalse(herbivore.isDead());
    }

    @Test
    void deadCreatureCanRecoverByGainingEnergy() {
        Herbivore herbivore = createHerbivore();

        herbivore.consumeEnergy(101.0);

        assertTrue(herbivore.isDead());

        herbivore.addEnergy(100.0);

        assertEquals(
                99.0,
                herbivore.getEnergy(),
                1e-9
        );

        assertFalse(herbivore.isDead());
    }

    @Test
    void rejectsInvalidEnergyGain() {
        Herbivore herbivore = createHerbivore();

        assertThrows(
                IllegalArgumentException.class,
                () -> herbivore.addEnergy(-1.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> herbivore.addEnergy(Double.NaN)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> herbivore.addEnergy(
                        Double.POSITIVE_INFINITY
                )
        );
    }

    @Test
    void rejectsInvalidEnergyCost() {
        Herbivore herbivore = createHerbivore();

        assertThrows(
                IllegalArgumentException.class,
                () -> herbivore.consumeEnergy(-1.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> herbivore.consumeEnergy(Double.NaN)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> herbivore.consumeEnergy(
                        Double.POSITIVE_INFINITY
                )
        );
    }

    @Test
    void calculatesReproductionThresholdFromGenome() {
        Genome genome = new Genome(
                1.0,
                1.0,
                1.0,
                2.0,
                1.0,
                180.0
        );

        Herbivore herbivore = new Herbivore(
                1,
                50.0,
                75.0,
                100.0,
                1,
                genome,
                Direction.NORTH
        );

        assertEquals(
                400.0,
                herbivore.getReproductionThreshold(),
                1e-9
        );
    }

    @Test
    void newCreatureHasNoReproductionCooldown() {
        Herbivore herbivore = createHerbivore();

        assertEquals(
                0,
                herbivore.getReproductionCooldownTicksRemaining()
        );

        assertTrue(
                herbivore.isReproductionCooldownComplete()
        );
    }

    @Test
    void startsReproductionCooldown() {
        Herbivore herbivore = createHerbivore();

        herbivore.startReproductionCooldown();

        assertEquals(
                50,
                herbivore.getReproductionCooldownTicksRemaining()
        );

        assertFalse(
                herbivore.isReproductionCooldownComplete()
        );
    }

    @Test
    void advancesReproductionCooldown() {
        Herbivore herbivore = createHerbivore();

        herbivore.startReproductionCooldown();
        herbivore.advanceReproductionCooldown();

        assertEquals(
                49,
                herbivore.getReproductionCooldownTicksRemaining()
        );
    }

    @Test
    void reproductionCooldownDoesNotGoBelowZero() {
        Herbivore herbivore = createHerbivore();

        herbivore.advanceReproductionCooldown();

        assertEquals(
                0,
                herbivore.getReproductionCooldownTicksRemaining()
        );
    }

    @Test
    void canReproduceWhenEnergyReachesThresholdAndCooldownIsComplete() {
        Genome genome = createGenome();

        Herbivore herbivore = new Herbivore(
                1,
                50.0,
                75.0,
                200.0,
                1,
                genome,
                Direction.NORTH
        );

        assertTrue(herbivore.canReproduce());
    }

    @Test
    void cannotReproduceBelowEnergyThreshold() {
        Herbivore herbivore = createHerbivore();

        assertFalse(herbivore.canReproduce());
    }

    @Test
    void cannotReproduceDuringCooldown() {
        Genome genome = createGenome();

        Herbivore herbivore = new Herbivore(
                1,
                50.0,
                75.0,
                200.0,
                1,
                genome,
                Direction.NORTH
        );

        herbivore.startReproductionCooldown();

        assertFalse(herbivore.canReproduce());
    }

    @Test
    void calculatesEggHatchTicksFromGenome() {
        Genome genome = new Genome(
                1.0,
                1.0,
                1.0,
                1.0,
                2.0,
                180.0
        );

        Herbivore herbivore = new Herbivore(
                1,
                50.0,
                75.0,
                100.0,
                1,
                genome,
                Direction.NORTH
        );

        assertEquals(
                100,
                herbivore.getEggHatchTicks()
        );
    }
}