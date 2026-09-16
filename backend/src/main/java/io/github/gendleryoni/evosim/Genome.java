package io.github.gendleryoni.evosim;

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

    public double getSpeed() {
        return speed;
    }

    public double getSize() {
        return size;
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