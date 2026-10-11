package io.github.gendleryoni.evosim;

import java.util.List;
import java.util.Random;

public class PredatorBehavior {

    private static final double PERSISTENT_DIRECTION_PROBABILITY = 1.0;

    public Vector2D chooseDirection(
            Predator predator,
            List<Herbivore> sensedHerbivores,
            Random random
    ) {
        if (predator == null) {
            throw new IllegalArgumentException(
                    "Predator cannot be null"
            );
        }

        if (sensedHerbivores == null) {
            throw new IllegalArgumentException(
                    "Sensed herbivore list cannot be null"
            );
        }

        if (random == null) {
            throw new IllegalArgumentException(
                    "Random cannot be null"
            );
        }

        Vector2D preyAttraction =
                calculatePreyAttraction(
                        predator,
                        sensedHerbivores
                );

        if (preyAttraction.length() > 0.0) {
            return preyAttraction;
        }

        return chooseExplorationDirection(
                predator,
                random
        );
    }

    private Vector2D calculatePreyAttraction(
            Predator predator,
            List<Herbivore> sensedHerbivores
    ) {
        Vector2D total =
                new Vector2D(0.0, 0.0);

        for (Herbivore herbivore : sensedHerbivores) {
            if (herbivore == null) {
                throw new IllegalArgumentException(
                        "Sensed herbivore list cannot contain null"
                );
            }

            double dx =
                    herbivore.getX()
                            - predator.getX();

            double dy =
                    herbivore.getY()
                            - predator.getY();

            Vector2D directionToHerbivore =
                    new Vector2D(dx, dy);

            double distance =
                    directionToHerbivore.length();

            if (distance == 0.0) {
                continue;
            }

            Vector2D normalizedDirection =
                    directionToHerbivore.normalized();

            double weight =
                    1.0 / (distance * distance);

            Vector2D contribution =
                    normalizedDirection.scale(weight);

            total =
                    total.add(contribution);
        }

        return total;
    }

    private Vector2D chooseExplorationDirection(
            Predator predator,
            Random random
    ) {
        Direction persistentDirection =
                predator.getExplorationDirection();

        if (random.nextDouble()
                < PERSISTENT_DIRECTION_PROBABILITY) {
            return persistentDirection.getVector();
        }

        Direction[] directions =
                Direction.values();

        int alternativeIndex =
                random.nextInt(
                        directions.length - 1
                );

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