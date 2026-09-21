package io.github.gendleryoni.evosim;

import java.util.List;
import java.util.Random;

public class HerbivoreBehavior {

    private static final double PERSISTENT_DIRECTION_PROBABILITY = 0.65;

    public Vector2D chooseDirection(
            Herbivore herbivore,
            List<Food> sensedFoods,
            Random random
    ) {
        if (herbivore == null) {
            throw new IllegalArgumentException(
                    "Herbivore cannot be null"
            );
        }

        if (sensedFoods == null) {
            throw new IllegalArgumentException(
                    "Sensed food list cannot be null"
            );
        }

        if (random == null) {
            throw new IllegalArgumentException(
                    "Random cannot be null"
            );
        }

        Vector2D foodAttraction =
                calculateFoodAttraction(
                        herbivore,
                        sensedFoods
                );

        if (foodAttraction.length() > 0.0) {
            return foodAttraction;
        }

        return chooseExplorationDirection(
                herbivore,
                random
        );
    }

    private Vector2D calculateFoodAttraction(
            Herbivore herbivore,
            List<Food> sensedFoods
    ) {
        Vector2D total = new Vector2D(0.0, 0.0);

        for (Food food : sensedFoods) {
            if (food == null) {
                throw new IllegalArgumentException(
                        "Sensed food list cannot contain null"
                );
            }

            double dx =
                    food.getX() - herbivore.getX();

            double dy =
                    food.getY() - herbivore.getY();

            Vector2D directionToFood =
                    new Vector2D(dx, dy);

            double distance =
                    directionToFood.length();

            if (distance == 0.0) {
                continue;
            }

            Vector2D normalizedDirection =
                    directionToFood.normalized();

            // Tuning decision:
            // inverse-square attraction is subject to change.
            double weight =
                    1.0 / (distance * distance);

            Vector2D contribution =
                    normalizedDirection.scale(weight);

            total = total.add(contribution);
        }

        return total;
    }

    private Vector2D chooseExplorationDirection(
            Herbivore herbivore,
            Random random
    ) {
        Direction persistentDirection =
                herbivore.getExplorationDirection();

        if (random.nextDouble()
                < PERSISTENT_DIRECTION_PROBABILITY) {
            return persistentDirection.getVector();
        }

        Direction[] directions = Direction.values();

        int alternativeIndex =
                random.nextInt(directions.length - 1);

        for (Direction direction : directions) {
            if (direction == persistentDirection) {
                continue;
            }

            if (alternativeIndex == 0) {
                return direction.getVector();
            }

            alternativeIndex--;
        }

        throw new IllegalStateException(
                "Failed to select exploration direction"
        );
    }
}