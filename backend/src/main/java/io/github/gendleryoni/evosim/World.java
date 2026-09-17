package io.github.gendleryoni.evosim;

import java.util.ArrayList;
import java.util.List;

public class World {
    private final double width;
    private final double height;
    private final List<Food> foods = new ArrayList<>();

    public World(double width, double height) {
        if (!Double.isFinite(width)
                || !Double.isFinite(height)
                || width <= 0
                || height <= 0) {
            throw new IllegalArgumentException(
                    "World dimensions must be positive and finite"
            );
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

    public List<Food> findFoodWithinRadius(
            double x,
            double y,
            double radius
    ) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Query position must be finite");
        }

        if (!Double.isFinite(radius) || radius < 0) {
            throw new IllegalArgumentException(
                    "Radius must be non-negative and finite"
            );
        }

        List<Food> nearbyFoods = new ArrayList<>();

        for (Food food : foods) {
            double dx = food.getX() - x;
            double dy = food.getY() - y;

            if (dx * dx + dy * dy <= radius * radius) {
                nearbyFoods.add(food);
            }
        }

        return nearbyFoods;
    }

    public Food addFood(double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Food position must be finite");
        }

        if (!isInBounds(x, y)) {
            throw new IllegalArgumentException("Food must be inside world bounds");
        }

        Food food = new Food(x, y);
        foods.add(food);

        return food;
    }

    public void removeFood(Food food) {
        if (food == null) {
            throw new IllegalArgumentException("Food cannot be null");
        }

        if (!foods.remove(food)) {
            throw new IllegalStateException("Food does not exist in world");
        }
    }
}
