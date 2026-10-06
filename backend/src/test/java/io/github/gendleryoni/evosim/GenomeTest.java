package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import java.util.Random;

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
                2.0,
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

    @Test
    void mutationWithZeroStrengthKeepsGenomeValuesUnchanged() {
        Genome genome = new Genome(
                1.0,
                1.5,
                0.75,
                2.0,
                1.25,
                180.0
        );

        Genome mutated = genome.mutate(0.0, new Random(12345L));

        assertNotSame(genome, mutated);

        assertEquals(genome.getSpeed(), mutated.getSpeed());
        assertEquals(genome.getSize(), mutated.getSize());
        assertEquals(genome.getSenseRadius(), mutated.getSenseRadius());
        assertEquals(
                genome.getReproductionThreshold(),
                mutated.getReproductionThreshold()
        );
        assertEquals(genome.getEggHatchTime(), mutated.getEggHatchTime());
        assertEquals(genome.getHue(), mutated.getHue());
    }

    @Test
    void mutationUsesPercentageOfCurrentTraitValue() {
        Genome genome = new Genome(
                1.0,
                2.0,
                1.0,
                1.0,
                1.0,
                180.0
        );

        Random random = new SequenceRandom(
                0.8,  // Speed: +3%
                0.3,  // Size: -2%
                0.5,  // Sense: 0%
                0.9,  // Reproduction threshold: +4%
                0.1,  // Hatch time: -4%
                0.75  // Hue: +9 degrees
        );

        Genome mutated = genome.mutate(0.05, random);

        assertEquals(1.03, mutated.getSpeed(), 1e-9);
        assertEquals(1.96, mutated.getSize(), 1e-9);
        assertEquals(1.0, mutated.getSenseRadius(), 1e-9);
        assertEquals(1.04, mutated.getReproductionThreshold(), 1e-9);
        assertEquals(0.96, mutated.getEggHatchTime(), 1e-9);
        assertEquals(189.0, mutated.getHue(), 1e-9);
    }

    @Test
    void mutationClampsFunctionalTraitsToAllowedRange() {
        Genome minimumGenome = new Genome(
                0.25,
                0.25,
                0.25,
                0.5,
                0.25,
                180.0
        );

        Genome maximumGenome = new Genome(
                4.0,
                4.0,
                4.0,
                2.0,
                4.0,
                180.0
        );

        Genome mutatedMinimum = minimumGenome.mutate(
                1.0,
                new SequenceRandom(
                        0.0, 0.0, 0.0, 0.0, 0.0,
                        0.5
                )
        );

        Genome mutatedMaximum = maximumGenome.mutate(
                1.0,
                new SequenceRandom(
                        0.75, 0.75, 0.75, 0.75, 0.75,
                        0.5
                )
        );

        assertEquals(0.25, mutatedMinimum.getSpeed());
        assertEquals(0.25, mutatedMinimum.getSize());
        assertEquals(0.25, mutatedMinimum.getSenseRadius());
        assertEquals(0.5, mutatedMinimum.getReproductionThreshold());
        assertEquals(0.25, mutatedMinimum.getEggHatchTime());

        assertEquals(4.0, mutatedMaximum.getSpeed());
        assertEquals(4.0, mutatedMaximum.getSize());
        assertEquals(4.0, mutatedMaximum.getSenseRadius());
        assertEquals(2.0, mutatedMaximum.getReproductionThreshold());
        assertEquals(4.0, mutatedMaximum.getEggHatchTime());
    }

    @Test
    void hueMutationWrapsAroundColorWheel() {
        Genome nearUpperBoundary = new Genome(
                1.0, 1.0, 1.0, 1.0, 1.0, 355.0
        );

        Genome nearLowerBoundary = new Genome(
                1.0, 1.0, 1.0, 1.0, 1.0, 5.0
        );

        Genome wrappedAbove = nearUpperBoundary.mutate(
                0.05,
                new SequenceRandom(
                        0.5, 0.5, 0.5, 0.5, 0.5,
                        0.75
                )
        );

        Genome wrappedBelow = nearLowerBoundary.mutate(
                0.05,
                new SequenceRandom(
                        0.5, 0.5, 0.5, 0.5, 0.5,
                        0.25
                )
        );

        assertEquals(4.0, wrappedAbove.getHue(), 1e-9);
        assertEquals(356.0, wrappedBelow.getHue(), 1e-9);
    }

    @Test
    void sameSeedProducesSameMutation() {
        Genome genome = new Genome(
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                180.0
        );

        Genome first = genome.mutate(
                0.05,
                new Random(12345L)
        );

        Genome second = genome.mutate(
                0.05,
                new Random(12345L)
        );

        assertEquals(first.getSpeed(), second.getSpeed());
        assertEquals(first.getSize(), second.getSize());
        assertEquals(first.getSenseRadius(), second.getSenseRadius());
        assertEquals(
                first.getReproductionThreshold(),
                second.getReproductionThreshold()
        );
        assertEquals(first.getEggHatchTime(), second.getEggHatchTime());
        assertEquals(first.getHue(), second.getHue());
    }

    @Test
    void rejectsInvalidMutationInputs() {
        Genome genome = new Genome(
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                180.0
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> genome.mutate(-0.01, new Random())
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> genome.mutate(Double.NaN, new Random())
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> genome.mutate(
                        Double.POSITIVE_INFINITY,
                        new Random()
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> genome.mutate(0.05, null)
        );
    }

    private static class SequenceRandom extends Random {

        private final double[] values;
        private int index;

        SequenceRandom(double... values) {
            this.values = values;
        }

        @Override
        public double nextDouble() {
            return values[index++];
        }
    }
}