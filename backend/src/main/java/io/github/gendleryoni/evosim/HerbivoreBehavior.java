package io.github.gendleryoni.evosim;

import java.util.List;

public class HerbivoreBehavior {

    public Vector2D chooseDirection(
            Herbivore herbivore,
            List<Food> sensedFoods
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

        Vector2D total = new Vector2D(0.0, 0.0);

        for (Food food : sensedFoods) {
            if (food == null) {
                throw new IllegalArgumentException(
                        "Sensed food list cannot contain null"
                );
            }

            double dx = food.getX() - herbivore.getX();
            double dy = food.getY() - herbivore.getY();

            Vector2D directionToFood =
                    new Vector2D(dx, dy);

            double distance = directionToFood.length();

            if (distance == 0.0) {
                continue;
            }

            Vector2D normalizedDirection =
                    directionToFood.normalized();

            // Tuning decision: inverse-square attraction is subject to change.
            double weight = 1.0 / (distance * distance);

            Vector2D contribution =
                    normalizedDirection.scale(weight);

            total = total.add(contribution);
        }

        return total;
    }
}