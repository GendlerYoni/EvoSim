package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Vector2DTest {

    @Test
    void storesVectorComponents() {
        Vector2D vector = new Vector2D(3.0, 4.0);

        assertEquals(3.0, vector.x());
        assertEquals(4.0, vector.y());
    }

    @Test
    void calculatesLength() {
        Vector2D vector = new Vector2D(3.0, 4.0);

        assertEquals(5.0, vector.length());
    }

    @Test
    void normalizesVector() {
        Vector2D vector = new Vector2D(3.0, 4.0);

        Vector2D normalized = vector.normalized();

        assertEquals(0.6, normalized.x(), 1e-9);
        assertEquals(0.8, normalized.y(), 1e-9);
        assertEquals(1.0, normalized.length(), 1e-9);
    }

    @Test
    void normalizingZeroVectorReturnsZeroVector() {
        Vector2D vector = new Vector2D(0.0, 0.0);

        Vector2D normalized = vector.normalized();

        assertEquals(new Vector2D(0.0, 0.0), normalized);
    }

    @Test
    void scalesVector() {
        Vector2D vector = new Vector2D(0.6, 0.8);

        Vector2D scaled = vector.scale(5.0);

        assertEquals(3.0, scaled.x(), 1e-9);
        assertEquals(4.0, scaled.y(), 1e-9);
    }

    @Test
    void addsVectors() {
        Vector2D first = new Vector2D(1.0, 2.0);
        Vector2D second = new Vector2D(3.0, -1.0);

        Vector2D result = first.add(second);

        assertEquals(4.0, result.x(), 1e-9);
        assertEquals(1.0, result.y(), 1e-9);
    }

    @Test
    void rejectsNonFiniteComponents() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Vector2D(Double.NaN, 0.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Vector2D(0.0, Double.POSITIVE_INFINITY)
        );
    }

    @Test
    void rejectsNullVectorWhenAdding() {
        Vector2D vector = new Vector2D(1.0, 2.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> vector.add(null)
        );
    }
}