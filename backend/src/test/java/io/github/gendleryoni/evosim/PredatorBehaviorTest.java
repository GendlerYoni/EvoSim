package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class PredatorBehaviorTest {

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

    private Predator createPredator(double x, double y) {
        return new Predator(
                1,
                x,
                y,
                100.0,
                1,
                createGenome(),
                Direction.NORTH
        );
    }

    private Herbivore createHerbivore(
            int id,
            double x,
            double y
    ) {
        return new Herbivore(
                id,
                x,
                y,
                100.0,
                1,
                createGenome(),
                Direction.NORTH
        );
    }

    private static class ControlledRandom extends Random {

        private final double nextDoubleValue;
        private final int nextIntValue;

        ControlledRandom(
                double nextDoubleValue,
                int nextIntValue
        ) {
            this.nextDoubleValue = nextDoubleValue;
            this.nextIntValue = nextIntValue;
        }

        @Override
        public double nextDouble() {
            return nextDoubleValue;
        }

        @Override
        public int nextInt(int bound) {
            return nextIntValue;
        }
    }

    @Test
    void herbivoreToRightCreatesRightwardAttraction() {
        Predator predator =
                createPredator(
                        50.0,
                        50.0
                );

        Herbivore herbivore =
                createHerbivore(
                        2,
                        60.0,
                        50.0
                );

        PredatorBehavior behavior =
                new PredatorBehavior();

        Vector2D direction =
                behavior.chooseDirection(
                        predator,
                        List.of(herbivore),
                        new Random(12345L)
                );

        assertEquals(
                0.01,
                direction.x(),
                1e-9
        );

        assertEquals(
                0.0,
                direction.y(),
                1e-9
        );
    }

    @Test
    void herbivoreAboveCreatesUpwardAttraction() {
        Predator predator =
                createPredator(
                        50.0,
                        50.0
                );

        Herbivore herbivore =
                createHerbivore(
                        2,
                        50.0,
                        40.0
                );

        PredatorBehavior behavior =
                new PredatorBehavior();

        Vector2D direction =
                behavior.chooseDirection(
                        predator,
                        List.of(herbivore),
                        new Random(12345L)
                );

        assertEquals(
                0.0,
                direction.x(),
                1e-9
        );

        assertEquals(
                -0.01,
                direction.y(),
                1e-9
        );
    }

    @Test
    void closerHerbivoreCreatesStrongerAttraction() {
        Predator predator =
                createPredator(
                        50.0,
                        50.0
                );

        Herbivore closeHerbivore =
                createHerbivore(
                        2,
                        52.0,
                        50.0
                );

        Herbivore farHerbivore =
                createHerbivore(
                        3,
                        60.0,
                        50.0
                );

        PredatorBehavior behavior =
                new PredatorBehavior();

        Vector2D closeDirection =
                behavior.chooseDirection(
                        predator,
                        List.of(closeHerbivore),
                        new Random(12345L)
                );

        Vector2D farDirection =
                behavior.chooseDirection(
                        predator,
                        List.of(farHerbivore),
                        new Random(12345L)
                );

        assertEquals(
                0.25,
                closeDirection.length(),
                1e-9
        );

        assertEquals(
                0.01,
                farDirection.length(),
                1e-9
        );

        assertTrue(
                closeDirection.length()
                        > farDirection.length()
        );
    }

    @Test
    void combinesPreyAttractionVectors() {
        Predator predator =
                createPredator(
                        50.0,
                        50.0
                );

        Herbivore rightHerbivore =
                createHerbivore(
                        2,
                        60.0,
                        50.0
                );

        Herbivore topHerbivore =
                createHerbivore(
                        3,
                        50.0,
                        40.0
                );

        PredatorBehavior behavior =
                new PredatorBehavior();

        Vector2D direction =
                behavior.chooseDirection(
                        predator,
                        List.of(
                                rightHerbivore,
                                topHerbivore
                        ),
                        new Random(12345L)
                );

        assertEquals(
                0.01,
                direction.x(),
                1e-9
        );

        assertEquals(
                -0.01,
                direction.y(),
                1e-9
        );
    }

    @Test
    void oppositePreyAttractionsFallBackToExploration() {
        Predator predator =
                createPredator(
                        50.0,
                        50.0
                );

        Herbivore leftHerbivore =
                createHerbivore(
                        2,
                        40.0,
                        50.0
                );

        Herbivore rightHerbivore =
                createHerbivore(
                        3,
                        60.0,
                        50.0
                );

        PredatorBehavior behavior =
                new PredatorBehavior();

        Random random =
                new ControlledRandom(
                        0.10,
                        0
                );

        Vector2D direction =
                behavior.chooseDirection(
                        predator,
                        List.of(
                                leftHerbivore,
                                rightHerbivore
                        ),
                        random
                );

        assertEquals(
                Direction.NORTH.getVector(),
                direction
        );
    }

    @Test
    void noPreyFallsBackToPersistentExplorationDirection() {
        Predator predator =
                createPredator(
                        50.0,
                        50.0
                );

        PredatorBehavior behavior =
                new PredatorBehavior();

        Random random =
                new ControlledRandom(
                        0.10,
                        0
                );

        Vector2D direction =
                behavior.chooseDirection(
                        predator,
                        List.of(),
                        random
                );

        assertEquals(
                Direction.NORTH.getVector(),
                direction
        );
    }

    @Test
    void herbivoreAtExactPredatorPositionFallsBackToExploration() {
        Predator predator =
                createPredator(
                        50.0,
                        50.0
                );

        Herbivore herbivore =
                createHerbivore(
                        2,
                        50.0,
                        50.0
                );

        PredatorBehavior behavior =
                new PredatorBehavior();

        Random random =
                new ControlledRandom(
                        0.10,
                        0
                );

        Vector2D direction =
                behavior.chooseDirection(
                        predator,
                        List.of(herbivore),
                        random
                );

        assertEquals(
                Direction.NORTH.getVector(),
                direction
        );
    }

    @Test
    void rejectsNullPredator() {
        PredatorBehavior behavior =
                new PredatorBehavior();

        assertThrows(
                IllegalArgumentException.class,
                () -> behavior.chooseDirection(
                        null,
                        List.of(),
                        new Random(12345L)
                )
        );
    }

    @Test
    void rejectsNullHerbivoreList() {
        Predator predator =
                createPredator(
                        50.0,
                        50.0
                );

        PredatorBehavior behavior =
                new PredatorBehavior();

        assertThrows(
                IllegalArgumentException.class,
                () -> behavior.chooseDirection(
                        predator,
                        null,
                        new Random(12345L)
                )
        );
    }

    @Test
    void rejectsNullRandom() {
        Predator predator =
                createPredator(
                        50.0,
                        50.0
                );

        PredatorBehavior behavior =
                new PredatorBehavior();

        assertThrows(
                IllegalArgumentException.class,
                () -> behavior.chooseDirection(
                        predator,
                        List.of(),
                        null
                )
        );
    }

    @Test
    void rejectsNullHerbivoreInsideList() {
        Predator predator =
                createPredator(
                        50.0,
                        50.0
                );

        PredatorBehavior behavior =
                new PredatorBehavior();

        List<Herbivore> herbivores =
                Arrays.asList(
                        createHerbivore(
                                2,
                                60.0,
                                50.0
                        ),
                        null
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> behavior.chooseDirection(
                        predator,
                        herbivores,
                        new Random(12345L)
                )
        );
    }
}
