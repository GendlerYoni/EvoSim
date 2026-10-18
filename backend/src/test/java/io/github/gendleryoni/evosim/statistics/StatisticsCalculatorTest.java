
package io.github.gendleryoni.evosim.statistics;

import io.github.gendleryoni.evosim.Direction;
import io.github.gendleryoni.evosim.Genome;
import io.github.gendleryoni.evosim.Herbivore;
import io.github.gendleryoni.evosim.Predator;
import io.github.gendleryoni.evosim.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsCalculatorTest {

    private static final double DELTA = 1e-9;

    private final StatisticsCalculator calculator =
            new StatisticsCalculator();

    private final World world = new World(1000, 800);

    private Genome createGenome(
            double speed,
            double size,
            double senseRadius,
            double reproductionThreshold,
            double eggHatchTime
    ) {
        return new Genome(
                speed,
                size,
                senseRadius,
                reproductionThreshold,
                eggHatchTime,
                180.0
        );
    }

    private Genome defaultGenome() {
        return createGenome(1.0, 1.0, 1.0, 1.0, 1.0);
    }

    private Herbivore createHerbivore(int id, Genome genome) {
        return new Herbivore(
                id,
                100.0,
                100.0,
                100.0,
                1,
                genome,
                Direction.NORTH
        );
    }

    private Predator createPredator(int id, Genome genome) {
        return new Predator(
                id,
                200.0,
                200.0,
                100.0,
                1,
                genome,
                Direction.NORTH
        );
    }

    @Test
    void emptyWorldProducesZeroPopulationsAndNullAverages() {
        SimulationSnapshot snapshot = calculator.calculate(world);

        assertEquals(
                new PopulationStatistics(0, 0, 0),
                snapshot.population()
        );

        assertNull(snapshot.herbivores());
        assertNull(snapshot.predators());
    }

    @Test
    void correctlyCountsAllPopulations() {
        Genome genome = defaultGenome();

        world.addHerbivore(createHerbivore(1, genome));
        world.addHerbivore(createHerbivore(2, genome));

        world.addPredator(createPredator(3, genome));

        world.addFood(300, 300);
        world.addFood(350, 350);
        world.addFood(400, 400);

        SimulationSnapshot snapshot = calculator.calculate(world);

        assertEquals(2, snapshot.population().herbivores());
        assertEquals(1, snapshot.population().predators());
        assertEquals(3, snapshot.population().food());
    }

    @Test
    void calculatesAllGenomeAveragesForBothSpecies() {
        Genome h1 = createGenome(1.0, 2.0, 3.0, 0.5, 1.0);
        Genome h2 = createGenome(3.0, 4.0, 1.0, 1.5, 3.0);

        Genome p1 = createGenome(0.5, 0.5, 0.25, 1.0, 1.5);
        Genome p2 = createGenome(1.5, 1.5, 0.75, 2.0, 2.5);

        world.addHerbivore(createHerbivore(1, h1));
        world.addHerbivore(createHerbivore(2, h2));

        world.addPredator(createPredator(3, p1));
        world.addPredator(createPredator(4, p2));

        SimulationSnapshot snapshot = calculator.calculate(world);

        GenomeAverages herbivores = snapshot.herbivores();
        GenomeAverages predators = snapshot.predators();

        assertNotNull(herbivores);
        assertNotNull(predators);

        assertAll(
                () -> assertEquals(2.0, herbivores.speed(), DELTA),
                () -> assertEquals(3.0, herbivores.size(), DELTA),
                () -> assertEquals(2.0, herbivores.senseRadius(), DELTA),
                () -> assertEquals(
                        1.0,
                        herbivores.reproductionThreshold(),
                        DELTA
                ),
                () -> assertEquals(
                        2.0,
                        herbivores.eggHatchTime(),
                        DELTA
                )
        );

        assertAll(
                () -> assertEquals(1.0, predators.speed(), DELTA),
                () -> assertEquals(1.0, predators.size(), DELTA),
                () -> assertEquals(0.5, predators.senseRadius(), DELTA),
                () -> assertEquals(
                        1.5,
                        predators.reproductionThreshold(),
                        DELTA
                ),
                () -> assertEquals(
                        2.0,
                        predators.eggHatchTime(),
                        DELTA
                )
        );
    }

    @Test
    void ignoresEggsInPopulationAndGenomeAverages() {
        Genome genome = defaultGenome();

        world.addHerbivoreEgg(300, 300, genome, 1, 20);
        world.addPredatorEgg(400, 400, genome, 1, 20);

        world.addFood(500, 500);

        SimulationSnapshot snapshot = calculator.calculate(world);

        assertEquals(0, snapshot.population().herbivores());
        assertEquals(0, snapshot.population().predators());
        assertEquals(1, snapshot.population().food());

        assertNull(snapshot.herbivores());
        assertNull(snapshot.predators());
    }

    @Test
    void calculatesHerbivoreAveragesWhenPredatorsAreAbsent() {
        Genome genome = createGenome(2.5, 1.5, 3.0, 1.2, 2.0);

        world.addHerbivore(createHerbivore(1, genome));

        SimulationSnapshot snapshot = calculator.calculate(world);

        assertNotNull(snapshot.herbivores());
        assertNull(snapshot.predators());

        assertEquals(
                2.5,
                snapshot.herbivores().speed(),
                DELTA
        );
    }

    @Test
    void calculatesPredatorAveragesWhenHerbivoresAreAbsent() {
        Genome genome = createGenome(1.5, 3.0, 2.0, 1.0, 1.5);

        world.addPredator(createPredator(1, genome));

        SimulationSnapshot snapshot = calculator.calculate(world);

        assertNull(snapshot.herbivores());
        assertNotNull(snapshot.predators());

        assertEquals(
                3.0,
                snapshot.predators().size(),
                DELTA
        );
    }

    @Test
    void recalculatesAfterWorldChanges() {
        Herbivore herbivore = createHerbivore(1, defaultGenome());

        world.addHerbivore(herbivore);

        SimulationSnapshot first = calculator.calculate(world);

        world.removeHerbivore(herbivore);

        SimulationSnapshot second = calculator.calculate(world);

        assertEquals(1, first.population().herbivores());
        assertNotNull(first.herbivores());

        assertEquals(0, second.population().herbivores());
        assertNull(second.herbivores());
    }

    @Test
    void rejectsNullWorld() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate(null)
        );
    }
}
