package io.github.gendleryoni.evosim;

public class Food {

    private final double x;
    private final double y;

    public Food(double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Food position must be finite");
        }

        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}