package io.github.gendleryoni.evosim;

final class EnergyModel {

    private static final double INITIAL_ENERGY = 100.0;
    private static final double FOOD_ENERGY_GAIN = 100.0;
    private static final double REPRODUCTION_ENERGY_COST = 80.0;
    private static final double HATCH_ENERGY = 80.0;

    private static final double TRAIT_COST_WEIGHT = 1.0 / 3.0;

    private static final double SENSE_COST_EXPONENT = 0.5;
    private static final double SPEED_COST_EXPONENT = 1.0;
    private static final double SIZE_COST_EXPONENT = 1.5;

    double initialEnergy() {
        return INITIAL_ENERGY;
    }

    double foodEnergyGain() {
        return FOOD_ENERGY_GAIN;
    }

    double calculateTickCost(Genome genome) {
        double senseCost =
                TRAIT_COST_WEIGHT
                        * Math.pow(
                        genome.getSenseRadius(),
                        SENSE_COST_EXPONENT
                );

        double speedCost =
                TRAIT_COST_WEIGHT
                        * Math.pow(
                        genome.getSpeed(),
                        SPEED_COST_EXPONENT
                );

        double sizeCost =
                TRAIT_COST_WEIGHT
                        * Math.pow(
                        genome.getSize(),
                        SIZE_COST_EXPONENT
                );

        return senseCost + speedCost + sizeCost;
    }

    double reproductionEnergyCost() {
        return REPRODUCTION_ENERGY_COST;
    }

    double hatchEnergy() {
        return HATCH_ENERGY;
    }
}