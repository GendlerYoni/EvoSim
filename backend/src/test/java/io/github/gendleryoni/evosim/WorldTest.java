package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {

    @Test
    void storesConfiguredDimensions() {
        World world = new World(100.0, 200.0);

        assertEquals(100.0, world.getWidth());
        assertEquals(200.0, world.getHeight());

        assertTrue(world.isInBounds(50.0, 100.0));
        assertTrue(world.isInBounds(0.0, 0.0));
        assertTrue(world.isInBounds(100.0, 200.0));
    }

    @Test
    void rejectsPositionsOutsideWorldBounds() {
        World world = new World(100.0, 200.0);

        assertFalse(world.isInBounds(-0.1, 100.0));
        assertFalse(world.isInBounds(100.1, 100.0));
        assertFalse(world.isInBounds(50.0, -0.1));
        assertFalse(world.isInBounds(50.0, 200.1));
    }

    @Test
    void rejectsInvalidDimensions() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new World(0.0, 100.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new World(100.0, 0.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new World(-1.0, 100.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new World(100.0, -1.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new World(Double.NaN, 100.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new World(100.0, Double.POSITIVE_INFINITY)
        );
    }

    @Test
    void addsFoodInsideWorldBounds() {
        World world = new World(100.0, 100.0);

        Food food = world.addFood(25.0, 30.0);

        List<Food> nearbyFoods =
                world.findFoodWithinRadius(25.0, 30.0, 0.0);

        assertEquals(1, nearbyFoods.size());
        assertSame(food, nearbyFoods.get(0));
    }

    @Test
    void rejectsFoodOutsideWorldBounds() {
        World world = new World(100.0, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> world.addFood(-1.0, 50.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.addFood(101.0, 50.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.addFood(50.0, -1.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.addFood(50.0, 101.0)
        );
    }

    @Test
    void rejectsFoodWithNonFinitePosition() {
        World world = new World(100.0, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> world.addFood(Double.NaN, 50.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.addFood(50.0, Double.POSITIVE_INFINITY)
        );
    }

    @Test
    void removesExistingFood() {
        World world = new World(100.0, 100.0);

        Food food = world.addFood(25.0, 30.0);

        world.removeFood(food);

        List<Food> nearbyFoods =
                world.findFoodWithinRadius(25.0, 30.0, 0.0);

        assertTrue(nearbyFoods.isEmpty());
    }

    @Test
    void rejectsRemovingFoodThatDoesNotExistInWorld() {
        World world = new World(100.0, 100.0);

        Food food = new Food(25.0, 30.0);

        assertThrows(
                IllegalStateException.class,
                () -> world.removeFood(food)
        );
    }

    @Test
    void rejectsRemovingNullFood() {
        World world = new World(100.0, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> world.removeFood(null)
        );
    }

    @Test
    void findsOnlyFoodWithinRadius() {
        World world = new World(100.0, 100.0);

        Food closeFood = world.addFood(53.0, 54.0);
        Food farFood = world.addFood(70.0, 70.0);

        List<Food> nearbyFoods =
                world.findFoodWithinRadius(50.0, 50.0, 5.0);

        assertEquals(1, nearbyFoods.size());
        assertSame(closeFood, nearbyFoods.get(0));
        assertFalse(nearbyFoods.contains(farFood));
    }

    @Test
    void includesFoodExactlyOnRadiusBoundary() {
        World world = new World(100.0, 100.0);

        Food food = world.addFood(53.0, 54.0);

        List<Food> nearbyFoods =
                world.findFoodWithinRadius(50.0, 50.0, 5.0);

        assertEquals(1, nearbyFoods.size());
        assertSame(food, nearbyFoods.get(0));
    }

    @Test
    void zeroRadiusFindsFoodAtExactPosition() {
        World world = new World(100.0, 100.0);

        Food exactFood = world.addFood(50.0, 50.0);
        world.addFood(50.1, 50.0);

        List<Food> nearbyFoods =
                world.findFoodWithinRadius(50.0, 50.0, 0.0);

        assertEquals(1, nearbyFoods.size());
        assertSame(exactFood, nearbyFoods.get(0));
    }

    @Test
    void rejectsInvalidRadiusQuery() {
        World world = new World(100.0, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> world.findFoodWithinRadius(50.0, 50.0, -1.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.findFoodWithinRadius(
                        50.0,
                        50.0,
                        Double.NaN
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.findFoodWithinRadius(
                        50.0,
                        50.0,
                        Double.POSITIVE_INFINITY
                )
        );
    }

    @Test
    void rejectsNonFiniteQueryPosition() {
        World world = new World(100.0, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> world.findFoodWithinRadius(
                        Double.NaN,
                        50.0,
                        10.0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.findFoodWithinRadius(
                        50.0,
                        Double.POSITIVE_INFINITY,
                        10.0
                )
        );
    }

    @Test
    void returnedFoodListDoesNotExposeWorldStorage() {
        World world = new World(100.0, 100.0);

        Food food = world.addFood(50.0, 50.0);

        List<Food> nearbyFoods =
                world.findFoodWithinRadius(50.0, 50.0, 10.0);

        nearbyFoods.clear();

        List<Food> secondQuery =
                world.findFoodWithinRadius(50.0, 50.0, 10.0);

        assertEquals(1, secondQuery.size());
        assertSame(food, secondQuery.get(0));
    }
}