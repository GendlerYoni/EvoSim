package io.github.gendleryoni.evosim.statistics;

public record GenomeAverages(
        double speed,
        double size,
        double senseRadius,
        double reproductionThreshold,
        double eggHatchTime
) {
    public GenomeAverages {
        validateAverage("speed", speed, 0.25, 4.0);
        validateAverage("size", size, 0.25, 4.0);
        validateAverage("senseRadius", senseRadius, 0.25, 4.0);
        validateAverage("reproductionThreshold", reproductionThreshold, 0.5, 2.0);
        validateAverage("eggHatchTime", eggHatchTime, 0.25, 4.0);
    }

    private static void validateAverage(
            String trait,
            double value,
            double min,
            double max
    ) {
        if (!Double.isFinite(value) || value < min || value > max) {
            throw new IllegalArgumentException(
                    trait + " average must be between " + min + " and " + max
            );
        }
    }
}