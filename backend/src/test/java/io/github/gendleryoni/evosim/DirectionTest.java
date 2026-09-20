package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionTest {

    @Test
    void northHasExpectedVector() {
        Vector2D vector = Direction.NORTH.getVector();

        assertEquals(0.0, vector.x(), 1e-9);
        assertEquals(-1.0, vector.y(), 1e-9);
    }

    @Test
    void eastHasExpectedVector() {
        Vector2D vector = Direction.EAST.getVector();

        assertEquals(1.0, vector.x(), 1e-9);
        assertEquals(0.0, vector.y(), 1e-9);
    }

    @Test
    void northEastIsNormalized() {
        Vector2D vector = Direction.NORTH_EAST.getVector();

        assertEquals(1.0, vector.length(), 1e-9);
        assertEquals(
                Math.sqrt(0.5),
                vector.x(),
                1e-9
        );
        assertEquals(
                -Math.sqrt(0.5),
                vector.y(),
                1e-9
        );
    }

    @Test
    void allDirectionsUseUnitVectors() {
        for (Direction direction : Direction.values()) {
            assertEquals(
                    1.0,
                    direction.getVector().length(),
                    1e-9
            );
        }
    }
}