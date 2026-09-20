package io.github.gendleryoni.evosim;

public record Vector2D(double x, double y) {

    public Vector2D {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException(
                    "Vector components must be finite"
            );
        }
    }

    public double length() {
        return Math.hypot(x, y);
    }

    public Vector2D normalized() {
        double length = length();

        if (length == 0.0) {
            return new Vector2D(0.0, 0.0);
        }

        return new Vector2D(
                x / length,
                y / length
        );
    }

    public Vector2D scale(double factor) {
        return new Vector2D(
                x * factor,
                y * factor
        );
    }

    public Vector2D add(Vector2D other) {
        if (other == null) {
            throw new IllegalArgumentException("Other vector cannot be null");
        }

        return new Vector2D(
                x + other.x(),
                y + other.y()
        );
    }
}