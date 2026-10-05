package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EggTest {

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

    @Test
    void createsEggWithExpectedState() {
        Genome genome = createGenome();

        Egg egg = new Egg(
                100.0,
                200.0,
                genome,
                2,
                50
        );

        assertEquals(100.0, egg.getX());
        assertEquals(200.0, egg.getY());
        assertSame(genome, egg.getGenome());
        assertEquals(2, egg.getGeneration());
        assertEquals(50, egg.getRemainingHatchTicks());
        assertFalse(egg.isReadyToHatch());
    }

    @Test
    void advanceTickReducesRemainingHatchTicks() {
        Egg egg = new Egg(
                100.0,
                200.0,
                createGenome(),
                2,
                3
        );

        egg.advanceTick();

        assertEquals(2, egg.getRemainingHatchTicks());
    }

    @Test
    void eggIsReadyToHatchAtZeroTicks() {
        Egg egg = new Egg(
                100.0,
                200.0,
                createGenome(),
                2,
                1
        );

        egg.advanceTick();

        assertEquals(0, egg.getRemainingHatchTicks());
        assertTrue(egg.isReadyToHatch());
    }

    @Test
    void advanceTickDoesNotGoBelowZero() {
        Egg egg = new Egg(
                100.0,
                200.0,
                createGenome(),
                2,
                1
        );

        egg.advanceTick();
        egg.advanceTick();

        assertEquals(0, egg.getRemainingHatchTicks());
    }

    @Test
    void rejectsNonFinitePosition() {
        Genome genome = createGenome();

        assertThrows(
                IllegalArgumentException.class,
                () -> new Egg(
                        Double.NaN,
                        200.0,
                        genome,
                        2,
                        50
                )
        );
    }

    @Test
    void rejectsNullGenome() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Egg(
                        100.0,
                        200.0,
                        null,
                        2,
                        50
                )
        );
    }

    @Test
    void rejectsGenerationBelowOne() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Egg(
                        100.0,
                        200.0,
                        createGenome(),
                        0,
                        50
                )
        );
    }

    @Test
    void rejectsNonPositiveHatchTicks() {
        Genome genome = createGenome();

        assertThrows(
                IllegalArgumentException.class,
                () -> new Egg(
                        100.0,
                        200.0,
                        genome,
                        2,
                        0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Egg(
                        100.0,
                        200.0,
                        genome,
                        2,
                        -1
                )
        );
    }
    @Test
    void eggHasExpectedRadius() {
        Egg egg = new Egg(
                100.0,
                200.0,
                createGenome(),
                2,
                50
        );

        assertEquals(1.5, egg.getRadius());
    }
}