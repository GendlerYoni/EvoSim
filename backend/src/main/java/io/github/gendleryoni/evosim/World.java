package io.github.gendleryoni.evosim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class World {

    private final double width;
    private final double height;

    private final List<Food> foods = new ArrayList<>();
    private final List<Herbivore> herbivores = new ArrayList<>();

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
        return x >= 0 && x <= width
                && y >= 0 && y <= height;
    }

    public List<Food> findFoodWithinRadius(
            double x,
            double y,
            double radius
    ) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException(
                    "Query position must be finite"
            );
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
        Food food = new Food(x, y);

        if (!isCircleInBounds(
                food.getX(),
                food.getY(),
                food.getRadius()
        )) {
            throw new IllegalArgumentException(
                    "Food must be fully inside world bounds"
            );
        }

        foods.add(food);

        return food;
    }

    public void removeFood(Food food) {
        if (food == null) {
            throw new IllegalArgumentException(
                    "Food cannot be null"
            );
        }

        if (!foods.remove(food)) {
            throw new IllegalStateException(
                    "Food does not exist in world"
            );
        }
    }

    public void addHerbivore(Herbivore herbivore) {
        if (herbivore == null) {
            throw new IllegalArgumentException(
                    "Herbivore cannot be null"
            );
        }

        if (!isCircleInBounds(
                herbivore.getX(),
                herbivore.getY(),
                herbivore.getRadius()
        )) {
            throw new IllegalArgumentException(
                    "Herbivore must be fully inside world bounds"
            );
        }

        herbivores.add(herbivore);
    }

    public void removeHerbivore(Herbivore herbivore) {
        if (herbivore == null) {
            throw new IllegalArgumentException(
                    "Herbivore cannot be null"
            );
        }

        if (!herbivores.remove(herbivore)) {
            throw new IllegalStateException(
                    "Herbivore does not exist in world"
            );
        }
    }

    public List<Herbivore> getHerbivores() {
        return Collections.unmodifiableList(herbivores);
    }

    private boolean isCircleInBounds(
            double x,
            double y,
            double radius
    ) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException(
                    "Circle position must be finite"
            );
        }

        if (!Double.isFinite(radius) || radius < 0) {
            throw new IllegalArgumentException(
                    "Circle radius must be non-negative and finite"
            );
        }

        return x >= radius
                && x <= width - radius
                && y >= radius
                && y <= height - radius;
    }

    boolean moveCreature(
            Creature creature,
            Vector2D direction,
            double distance
    ) {
        if (creature == null) {
            throw new IllegalArgumentException(
                    "Creature cannot be null"
            );
        }

        if (direction == null) {
            throw new IllegalArgumentException(
                    "Movement direction cannot be null"
            );
        }

        if (!Double.isFinite(distance) || distance < 0) {
            throw new IllegalArgumentException(
                    "Movement distance must be non-negative and finite"
            );
        }

        if (!herbivores.contains(creature)) {
            throw new IllegalStateException(
                    "Creature does not exist in world"
            );
        }

        Vector2D normalizedDirection = direction.normalized();

        double proposedX =
                creature.getX()
                        + normalizedDirection.x() * distance;

        double proposedY =
                creature.getY()
                        + normalizedDirection.y() * distance;

        double radius = creature.getRadius();

        double minX = radius;
        double maxX = width - radius;
        double minY = radius;
        double maxY = height - radius;

        boolean hitWall =
                proposedX < minX
                        || proposedX > maxX
                        || proposedY < minY
                        || proposedY > maxY;

        double finalX = Math.max(
                minX,
                Math.min(proposedX, maxX)
        );

        double finalY = Math.max(
                minY,
                Math.min(proposedY, maxY)
        );

        creature.moveTo(finalX, finalY);

        return hitWall;
    }

    public List<Food> getFoods() {
        return Collections.unmodifiableList(foods);
    }
}