package io.github.gendleryoni.evosim;

public class World {
    private final double width;
    private final double height;

    public World(double width, double height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("World dimensions must be positive");
        }
        this.width = width;
        this.height = height;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public boolean isInBounds(double x, double y) {
        return x >= 0 && x <= width &&
                y >= 0 && y <= height;
    }
}
