package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorldTest {

    @Test
    void storesConfiguredDimensions() {
        World world = new World(100.0, 200.0);

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
    void rejectsNonPositiveDimensions() {
        assertThrows(IllegalArgumentException.class,
                () -> new World(0.0, 100.0));

        assertThrows(IllegalArgumentException.class,
                () -> new World(100.0, 0.0));

        assertThrows(IllegalArgumentException.class,
                () -> new World(-1.0, 100.0));

        assertThrows(IllegalArgumentException.class,
                () -> new World(100.0, -1.0));
    }
}