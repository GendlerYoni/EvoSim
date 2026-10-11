package io.github.gendleryoni.evosim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.ToDoubleFunction;

public class World {

    private final double width;
    private final double height;

    private final List<Food> foods = new ArrayList<>();
    private final List<Herbivore> herbivores = new ArrayList<>();
    private final List<Predator> predators = new ArrayList<>();
    private final List<Egg> herbivoreEggs = new ArrayList<>();
    private final List<Egg> predatorEggs = new ArrayList<>();

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
        return findEntitiesWithinRadius(
                foods,
                x,
                y,
                radius,
                Food::getX,
                Food::getY
        );
    }

    public List<Herbivore> findHerbivoresWithinRadius(
            double x,
            double y,
            double radius
    ) {
        return findEntitiesWithinRadius(
                herbivores,
                x,
                y,
                radius,
                Herbivore::getX,
                Herbivore::getY
        );
    }

    public List<Predator> findPredatorsWithinRadius(
            double x,
            double y,
            double radius
    ) {
        return findEntitiesWithinRadius(
                predators,
                x,
                y,
                radius,
                Predator::getX,
                Predator::getY
        );
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
        addCreature(herbivores, herbivore);
    }

    public void removeHerbivore(Herbivore herbivore) {
        removeCreature(herbivores, herbivore);
    }

    public void addPredator(Predator predator) {
        addCreature(predators, predator);
    }

    public void removePredator(Predator predator) {
        removeCreature(predators, predator);
    }

    public Egg addHerbivoreEgg(
            double x,
            double y,
            Genome genome,
            int generation,
            int remainingHatchTicks
    ) {
        return addEgg(
                herbivoreEggs,
                x,
                y,
                genome,
                generation,
                remainingHatchTicks
        );
    }

    public Egg addPredatorEgg(
            double x,
            double y,
            Genome genome,
            int generation,
            int remainingHatchTicks
    ) {
        return addEgg(
                predatorEggs,
                x,
                y,
                genome,
                generation,
                remainingHatchTicks
        );
    }

    public void removeHerbivoreEgg(Egg egg) {
        removeEgg(herbivoreEggs, egg);
    }

    public void removePredatorEgg(Egg egg) {
        removeEgg(predatorEggs, egg);
    }

    public List<Herbivore> getHerbivores() {
        return Collections.unmodifiableList(herbivores);
    }

    public List<Predator> getPredators() {
        return Collections.unmodifiableList(predators);
    }

    public List<Food> getFoods() {
        return Collections.unmodifiableList(foods);
    }

    public List<Egg> getHerbivoreEggs() {
        return Collections.unmodifiableList(herbivoreEggs);
    }

    public List<Egg> getPredatorEggs() {
        return Collections.unmodifiableList(predatorEggs);
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

        if (!herbivores.contains(creature)
                && !predators.contains(creature)) {
            throw new IllegalStateException(
                    "Creature does not exist in world"
            );
        }

        Vector2D normalizedDirection =
                direction.normalized();

        double proposedX =
                creature.getX()
                        + normalizedDirection.x() * distance;

        double proposedY =
                creature.getY()
                        + normalizedDirection.y() * distance;

        double boundaryMargin =
                Creature.calculateMaximumRadius();

        double minX = boundaryMargin;
        double maxX = width - boundaryMargin;
        double minY = boundaryMargin;
        double maxY = height - boundaryMargin;

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

    private <T extends Creature> void addCreature(
            List<T> creatures,
            T creature
    ) {
        if (creature == null) {
            throw new IllegalArgumentException(
                    "Creature cannot be null"
            );
        }

        if (!isCircleInBounds(
                creature.getX(),
                creature.getY(),
                Creature.calculateMaximumRadius()
        )) {
            throw new IllegalArgumentException(
                    "Creature must be inside creature movement bounds"
            );
        }

        creatures.add(creature);
    }

    private <T extends Creature> void removeCreature(
            List<T> creatures,
            T creature
    ) {
        if (creature == null) {
            throw new IllegalArgumentException(
                    "Creature cannot be null"
            );
        }

        if (!creatures.remove(creature)) {
            throw new IllegalStateException(
                    "Creature does not exist in world"
            );
        }
    }

    private Egg addEgg(
            List<Egg> eggs,
            double x,
            double y,
            Genome genome,
            int generation,
            int remainingHatchTicks
    ) {
        Egg egg = new Egg(
                x,
                y,
                genome,
                generation,
                remainingHatchTicks
        );

        if (!isCircleInBounds(
                egg.getX(),
                egg.getY(),
                Creature.calculateMaximumRadius()
        )) {
            throw new IllegalArgumentException(
                    "Egg must be fully inside world bounds"
            );
        }

        eggs.add(egg);

        return egg;
    }

    private void removeEgg(
            List<Egg> eggs,
            Egg egg
    ) {
        if (egg == null) {
            throw new IllegalArgumentException(
                    "Egg cannot be null"
            );
        }

        if (!eggs.remove(egg)) {
            throw new IllegalStateException(
                    "Egg does not exist in world"
            );
        }
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

    private <T> List<T> findEntitiesWithinRadius(
            List<T> entities,
            double x,
            double y,
            double radius,
            ToDoubleFunction<T> xExtractor,
            ToDoubleFunction<T> yExtractor
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

        List<T> nearbyEntities = new ArrayList<>();

        double radiusSquared = radius * radius;

        for (T entity : entities) {
            double dx =
                    xExtractor.applyAsDouble(entity) - x;

            double dy =
                    yExtractor.applyAsDouble(entity) - y;

            if (dx * dx + dy * dy <= radiusSquared) {
                nearbyEntities.add(entity);
            }
        }

        return nearbyEntities;
    }
}