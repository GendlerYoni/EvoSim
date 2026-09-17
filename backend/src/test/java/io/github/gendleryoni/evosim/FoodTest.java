package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FoodTest {

    @Test
    void createsFoodWithValidPosition() {
        Food food = new Food(10.0, 20.0);

        assertEquals(10.0, food.getX());
        assertEquals(20.0, food.getY());
    }

    @Test
    void rejectsNonFinitePosition() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Food(Double.NaN, 20.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Food(10.0, Double.POSITIVE_INFINITY)
        );
    }
}