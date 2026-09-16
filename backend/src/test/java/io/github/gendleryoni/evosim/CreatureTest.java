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

    @Test
    void createsHerbivoreWithValidState() {
        Genome genome = createGenome();

        Herbivore herbivore = new Herbivore(
                1,
                50.0,
                75.0,
                100.0,
                1,
                genome
        );

        assertEquals(1, herbivore.getId());
        assertEquals(50.0, herbivore.getX());
        assertEquals(75.0, herbivore.getY());
        assertEquals(100.0, herbivore.getEnergy());
        assertEquals(1, herbivore.getGeneration());
        assertSame(genome, herbivore.getGenome());
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
                        createGenome()
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
                        createGenome()
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
                        createGenome()
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
                        createGenome()
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
                        createGenome()
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
                        createGenome()
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
                        null
                )
        );
    }
}