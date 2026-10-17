package io.github.gendleryoni.evosim.statistics;

import io.github.gendleryoni.evosim.World;
import io.github.gendleryoni.evosim.Creature;
import io.github.gendleryoni.evosim.Genome;

import java.util.List;
import java.util.Objects;
public class StatisticsCalculator {

    public SimulationSnapshot calculate(World world) {
        Objects.requireNonNull(world, "World cannot be null");

        PopulationStatistics population = calculatePopulation(world);

        GenomeAverages herbivoreAverages =
                calculateGenomeAverages(world.getHerbivores());

        GenomeAverages predatorAverages =
                calculateGenomeAverages(world.getPredators());

        return new SimulationSnapshot(
                population,
                herbivoreAverages,
                predatorAverages
        );
    }


    private PopulationStatistics calculatePopulation(World world) {
        return new PopulationStatistics(
                world.getHerbivores().size(),
                world.getPredators().size(),
                world.getFoods().size()
        );
    }

    private GenomeAverages calculateGenomeAverages(
            List<? extends Creature> creatures
    ) {
        if (creatures.isEmpty()) {
            return null;
        }

        double totalSpeed = 0;
        double totalSize = 0;
        double totalSenseRadius = 0;
        double totalReproductionThreshold = 0;
        double totalEggHatchTime = 0;

        for (Creature creature : creatures) {
            Genome genome = creature.getGenome();

            totalSpeed += genome.getSpeed();
            totalSize += genome.getSize();
            totalSenseRadius += genome.getSenseRadius();
            totalReproductionThreshold += genome.getReproductionThreshold();
            totalEggHatchTime += genome.getEggHatchTime();
        }

        int count = creatures.size();

        return new GenomeAverages(
                totalSpeed / count,
                totalSize / count,
                totalSenseRadius / count,
                totalReproductionThreshold / count,
                totalEggHatchTime / count
        );
    }
}