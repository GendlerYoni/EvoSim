package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    void foodToRightCreatesRightwardAttraction() {
        Herbivore herbivore = createHerbivore(50.0, 50.0);
        Food food = new Food(60.0, 50.0);

        HerbivoreBehavior behavior = new HerbivoreBehavior();

        Vector2D direction = behavior.chooseDirection(
                herbivore,
                List.of(food)
        );

        assertEquals(0.01, direction.x(), 1e-9);
        assertEquals(0.0, direction.y(), 1e-9);
    }

    @Test
    void foodAboveCreatesUpwardAttraction() {
        Herbivore herbivore = createHerbivore(50.0, 50.0);
        Food food = new Food(50.0, 40.0);

        HerbivoreBehavior behavior = new HerbivoreBehavior();

        Vector2D direction = behavior.chooseDirection(
                herbivore,
                List.of(food)
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
                List.of(closeFood)
        );

        Vector2D farDirection = behavior.chooseDirection(
                herbivore,
                List.of(farFood)
        );

        assertEquals(0.25, closeDirection.length(), 1e-9);
        assertEquals(0.01, farDirection.length(), 1e-9);

        assertTrue(
                closeDirection.length() > farDirection.length()
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
                List.of(rightFood, topFood)
        );

        assertEquals(0.01, direction.x(), 1e-9);
        assertEquals(-0.01, direction.y(), 1e-9);
    }

    @Test
    void equalOppositeFoodAttractionsCancelOut() {
        Herbivore herbivore = createHerbivore(50.0, 50.0);

        Food leftFood = new Food(40.0, 50.0);
        Food rightFood = new Food(60.0, 50.0);

        HerbivoreBehavior behavior = new HerbivoreBehavior();

        Vector2D direction = behavior.chooseDirection(
                herbivore,
                List.of(leftFood, rightFood)
        );

        assertEquals(0.0, direction.x(), 1e-9);
        assertEquals(0.0, direction.y(), 1e-9);
    }

    @Test
    void returnsZeroVectorWhenNoFoodIsSensed() {
        Herbivore herbivore = createHerbivore(50.0, 50.0);

        HerbivoreBehavior behavior = new HerbivoreBehavior();

        Vector2D direction = behavior.chooseDirection(
                herbivore,
                List.of()
        );

        assertEquals(0.0, direction.x(), 1e-9);
        assertEquals(0.0, direction.y(), 1e-9);
    }

    @Test
    void ignoresFoodAtExactHerbivorePosition() {
        Herbivore herbivore = createHerbivore(50.0, 50.0);
        Food food = new Food(50.0, 50.0);

        HerbivoreBehavior behavior = new HerbivoreBehavior();

        Vector2D direction = behavior.chooseDirection(
                herbivore,
                List.of(food)
        );

        assertEquals(0.0, direction.x(), 1e-9);
        assertEquals(0.0, direction.y(), 1e-9);
    }

    @Test
    void rejectsNullHerbivore() {
        HerbivoreBehavior behavior = new HerbivoreBehavior();

        assertThrows(
                IllegalArgumentException.class,
                () -> behavior.chooseDirection(
                        null,
                        List.of()
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
                        null
                )
        );
    }

    @Test
    void rejectsNullFoodInsideList() {
        Herbivore herbivore = createHerbivore(50.0, 50.0);
        HerbivoreBehavior behavior = new HerbivoreBehavior();

        List<Food> foods =
                java.util.Arrays.asList(
                        new Food(60.0, 50.0),
                        null
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> behavior.chooseDirection(
                        herbivore,
                        foods
                )
        );
    }
}