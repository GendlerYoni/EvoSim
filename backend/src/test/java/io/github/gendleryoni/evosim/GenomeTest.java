package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GenomeTest {

    @Test
    void createsGenomeWithValidTraits() {
        Genome genome = new Genome(
                1.0,
                1.5,
                0.75,
                2.0,
                1.25,
                180.0
        );

        assertEquals(1.0, genome.getSpeed());
        assertEquals(1.5, genome.getSize());
        assertEquals(0.75, genome.getSenseRadius());
        assertEquals(2.0, genome.getReproductionThreshold());
        assertEquals(1.25, genome.getEggHatchTime());
        assertEquals(180.0, genome.getHue());
    }

    @Test
    void acceptsTraitBoundaryValues() {
        assertDoesNotThrow(() -> new Genome(
                0.25,
                4.0,
                0.25,
                4.0,
                0.25,
                0.0
        ));
    }

    @Test
    void rejectsTraitsOutsideAllowedRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Genome(0.24, 1.0, 1.0, 1.0, 1.0, 180.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Genome(1.0, 4.01, 1.0, 1.0, 1.0, 180.0)
        );
    }

    @Test
    void rejectsHueOutsideAllowedRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Genome(1.0, 1.0, 1.0, 1.0, 1.0, -0.1)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Genome(1.0, 1.0, 1.0, 1.0, 1.0, 360.0)
        );
    }

    @Test
    void rejectsNonFiniteValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Genome(Double.NaN, 1.0, 1.0, 1.0, 1.0, 180.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Genome(1.0, 1.0, 1.0, 1.0, 1.0, Double.POSITIVE_INFINITY)
        );
    }
}