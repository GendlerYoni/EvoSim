package io.github.gendleryoni.evosim;

public enum Direction {

    NORTH(0.0, -1.0),
    NORTH_EAST(1.0, -1.0),
    EAST(1.0, 0.0),
    SOUTH_EAST(1.0, 1.0),
    SOUTH(0.0, 1.0),
    SOUTH_WEST(-1.0, 1.0),
    WEST(-1.0, 0.0),
    NORTH_WEST(-1.0, -1.0);

    private final Vector2D vector;

    Direction(double x, double y) {
        this.vector = new Vector2D(x, y).normalized();
    }

    public Vector2D getVector() {
        return vector;
    }
}