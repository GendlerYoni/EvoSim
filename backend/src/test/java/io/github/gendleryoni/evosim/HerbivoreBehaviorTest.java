package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class HerbivoreBehaviorTest {

    private Herbivore createHerbivore(double x, double y) {
        Genome genome = new Genome(
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                180.0
        );

        return new Herbivore(
                1,
                x,
                y,
                100.0,
                1,
                genome,
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
    void foodToRightCreatesRightwardAttraction() {
        Herbivore herbivore = createHerbivore(50.0, 50.0);
        Food food = new Food(60.0, 50.0);

        HerbivoreBehavior behavior = new HerbivoreBehavior();
        Random random = new Random(12345L);

        Vector2D direction = behavior.chooseDirection(
                herbivore,
                List.of(food),
                random
        );

        assertEquals(0.01, direction.x(), 1e-9);
        assertEquals(0.0, direction.y(), 1e-9);
    }

    @Test
    void foodAboveCreatesUpwardAttraction() {
        Herbivore herbivore = createHerbivore(50.0, 50.0);
        Food food = new Food(50.0, 40.0);

        HerbivoreBehavior behavior = new HerbivoreBehavior();
        Random random = new Random(12345L);

        Vector2D direction = behavior.chooseDirection(
                herbivore,
                List.of(food),
                random
        );

        assertEquals(0.0, direction.x(), 1e-9);
        assertEquals(-0.01, direction.y(), 1e-9);
    }

    @Test
    void closerFoodCreatesStrongerAttraction() {
        Herbivore herbivore = createHerbivore(50.0, 50.0);

        Food closeFood = new Food(52.0, 50.0);
        Food farFood = new Food(60.0, 50.0);

        HerbivoreBehavior behavior = new HerbivoreBehavior();

        Vector2D closeDirection = behavior.chooseDirection(
                herbivore,
                List.of(closeFood),
                new Random(12345L)
        );

        Vector2D farDirection = behavior.chooseDirection(
                herbivore,
                List.of(farFood),
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
    void combinesFoodAttractionVectors() {
        Herbivore herbivore = createHerbivore(50.0, 50.0);

        Food rightFood = new Food(60.0, 50.0);
        Food topFood = new Food(50.0, 40.0);

        HerbivoreBehavior behavior = new HerbivoreBehavior();

        Vector2D direction = behavior.chooseDirection(
                herbivore,
                List.of(rightFood, topFood),
                new Random(12345L)
        );

        assertEquals(0.01, direction.x(), 1e-9);
        assertEquals(-0.01, direction.y(), 1e-9);
    }

    @Test
    void oppositeFoodAttractionsFallBackToExploration() {
        Herbivore herbivore = createHerbivore(50.0, 50.0);

        Food leftFood = new Food(40.0, 50.0);
        Food rightFood = new Food(60.0, 50.0);

        HerbivoreBehavior behavior = new HerbivoreBehavior();

        Random random = new ControlledRandom(
                0.10,
                0
        );

        Vector2D direction = behavior.chooseDirection(
                herbivore,
                List.of(leftFood, rightFood),
                random
        );

        assertEquals(
                Direction.NORTH.getVector(),
                direction
        );
    }

    @Test
    void noFoodFallsBackToPersistentExplorationDirection() {
        Herbivore herbivore = createHerbivore(50.0, 50.0);

        HerbivoreBehavior behavior = new HerbivoreBehavior();

        Random random = new ControlledRandom(
                0.10,
                0
        );

        Vector2D direction = behavior.chooseDirection(
                herbivore,
                List.of(),
                random
        );

        assertEquals(
                Direction.NORTH.getVector(),
                direction
        );
    }

    @Test
    void explorationCanChooseAlternativeDirection() {
        Herbivore herbivore = createHerbivore(50.0, 50.0);

        HerbivoreBehavior behavior = new HerbivoreBehavior();

        Random random = new ControlledRandom(
                0.90,
                0
        );

        Vector2D direction = behavior.chooseDirection(
                herbivore,
                List.of(),
                random
        );

        assertEquals(
                Direction.NORTH_EAST.getVector(),
                direction
        );
    }

    @Test
    void explorationDeviationDoesNotChangePersistentDirection() {
        Herbivore herbivore = createHerbivore(50.0, 50.0);

        HerbivoreBehavior behavior = new HerbivoreBehavior();

        Random random = new ControlledRandom(
                0.90,
                0
        );

        Vector2D direction = behavior.chooseDirection(
                herbivore,
                List.of(),
                random
        );

        assertEquals(
                Direction.NORTH_EAST.getVector(),
                direction
        );

        assertEquals(
                Direction.NORTH,
                herbivore.getExplorationDirection()
        );
    }

    @Test
    void foodAtExactHerbivorePositionFallsBackToExploration() {
        Herbivore herbivore = createHerbivore(50.0, 50.0);
        Food food = new Food(50.0, 50.0);

        HerbivoreBehavior behavior = new HerbivoreBehavior();

        Random random = new ControlledRandom(
                0.10,
                0
        );

        Vector2D direction = behavior.chooseDirection(
                herbivore,
                List.of(food),
                random
        );

        assertEquals(
                Direction.NORTH.getVector(),
                direction
        );
    }

    @Test
    void rejectsNullHerbivore() {
        HerbivoreBehavior behavior = new HerbivoreBehavior();

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
    void rejectsNullFoodList() {
        Herbivore herbivore = createHerbivore(50.0, 50.0);
        HerbivoreBehavior behavior = new HerbivoreBehavior();

        assertThrows(
                IllegalArgumentException.class,
                () -> behavior.chooseDirection(
                        herbivore,
                        null,
                        new Random(12345L)
                )
        );
    }

    @Test
    void rejectsNullRandom() {
        Herbivore herbivore = createHerbivore(50.0, 50.0);
        HerbivoreBehavior behavior = new HerbivoreBehavior();

        assertThrows(
                IllegalArgumentException.class,
                () -> behavior.chooseDirection(
                        herbivore,
                        List.of(),
                        null
                )
        );
    }

    @Test
    void rejectsNullFoodInsideList() {
        Herbivore herbivore = createHerbivore(50.0, 50.0);
        HerbivoreBehavior behavior = new HerbivoreBehavior();

        List<Food> foods = Arrays.asList(
                new Food(60.0, 50.0),
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> behavior.chooseDirection(
                        herbivore,
                        foods,
                        new Random(12345L)
                )
        );
    }
}