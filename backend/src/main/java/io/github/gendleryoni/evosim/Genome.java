package io.github.gendleryoni.evosim;

import java.util.Random;

public class Genome {

    private static final double MIN_TRAIT_VALUE = 0.25;
    private static final double MAX_TRAIT_VALUE = 4.0;

    private final double speed;
    private final double size;
    private final double senseRadius;
    private final double reproductionThreshold;
    private final double eggHatchTime;
    private final double hue;

    public Genome(
            double speed,
            double size,
            double senseRadius,
            double reproductionThreshold,
            double eggHatchTime,
            double hue
    ) {
        validateTrait(speed, "speed");
        validateTrait(size, "size");
        validateTrait(senseRadius, "senseRadius");
        validateTrait(reproductionThreshold, "reproductionThreshold");
        validateTrait(eggHatchTime, "eggHatchTime");
        validateHue(hue);

        this.speed = speed;
        this.size = size;
        this.senseRadius = senseRadius;
        this.reproductionThreshold = reproductionThreshold;
        this.eggHatchTime = eggHatchTime;
        this.hue = hue;
    }

    public Genome mutate(double mutationStrength, Random random) {
        if (!Double.isFinite(mutationStrength) || mutationStrength < 0.0) {
            throw new IllegalArgumentException(
                    "Mutation strength must be non-negative and finite"
            );
        }

        if (random == null) {
            throw new IllegalArgumentException(
                    "Random cannot be null"
            );
        }

        double mutatedSpeed =
                mutateTrait(speed, mutationStrength, random);

        double mutatedSize =
                mutateTrait(size, mutationStrength, random);

        double mutatedSenseRadius =
                mutateTrait(senseRadius, mutationStrength, random);

        double mutatedReproductionThreshold =
                mutateTrait(reproductionThreshold, mutationStrength, random);

        double mutatedEggHatchTime =
                mutateTrait(eggHatchTime, mutationStrength, random);

        double mutatedHue =
                mutateHue(hue, mutationStrength, random);

        return new Genome(
                mutatedSpeed,
                mutatedSize,
                mutatedSenseRadius,
                mutatedReproductionThreshold,
                mutatedEggHatchTime,
                mutatedHue
        );
    }

    private static void validateTrait(double value, String traitName) {
        if (!Double.isFinite(value)
                || value < MIN_TRAIT_VALUE
                || value > MAX_TRAIT_VALUE) {
            throw new IllegalArgumentException(traitName + " must be between "
                            + MIN_TRAIT_VALUE + " and " + MAX_TRAIT_VALUE);
        }
    }

    private static void validateHue(double hue) {
        if (!Double.isFinite(hue) || hue < 0.0 || hue >= 360.0) {
            throw new IllegalArgumentException(
                    "hue must be between 0 inclusive and 360 exclusive"
            );
        }
    }

    private static double mutateTrait(
            double value,
            double mutationStrength,
            Random random
    ) {
        if (mutationStrength == 0.0) {
            return value;
        }

        double mutationPercentage =
                (random.nextDouble() * 2.0 - 1.0) * mutationStrength;

        double mutatedValue =
                value * (1.0 + mutationPercentage);

        return Math.max(
                MIN_TRAIT_VALUE,
                Math.min(MAX_TRAIT_VALUE, mutatedValue)
        );
    }

    private static double mutateHue(
            double hue,
            double mutationStrength,
            Random random
    ) {
        if (mutationStrength == 0.0) {
            return hue;
        }

        double maxHueChange = 360.0 * mutationStrength;

        double hueChange =
                (random.nextDouble() * 2.0 - 1.0) * maxHueChange;

        double mutatedHue = hue + hueChange;

        return ((mutatedHue % 360.0) + 360.0) % 360.0;
    }



    public double getSpeed() {
        return speed;
    }

    public double getSize() {
        return size;
    }

    static double getMaxSize() {
        return MAX_TRAIT_VALUE;
    }

    public double getSenseRadius() {
        return senseRadius;
    }

    public double getReproductionThreshold() {
        return reproductionThreshold;
    }

    public double getEggHatchTime() {
        return eggHatchTime;
    }

    public double getHue() {
        return hue;
    }
}