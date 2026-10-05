package io.github.gendleryoni.evosim.viewer;

import io.github.gendleryoni.evosim.Food;
import io.github.gendleryoni.evosim.Genome;
import io.github.gendleryoni.evosim.Herbivore;
import io.github.gendleryoni.evosim.SimulationConfig;
import io.github.gendleryoni.evosim.SimulationEngine;
import io.github.gendleryoni.evosim.Egg;

class JavaFxSimulationRunner {

    private static final double WORLD_WIDTH = 1000.0;
    private static final double WORLD_HEIGHT = 800.0;

    private static final int INITIAL_HERBIVORES = 20;
    private static final int INITIAL_FOOD = 100;

    private static final long SEED = 12345L;

    private static final double MUTATION_STRENGTH = 0.05;

    private static final long NANOS_PER_SECOND =
            1_000_000_000L;

    private final SimulationEngine engine;
    private final long tickIntervalNanos;

    private long previousTimeNanos = -1L;
    private long accumulatorNanos = 0L;

    JavaFxSimulationRunner() {
        Genome initialGenome = new Genome(
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                180.0
        );

        SimulationConfig config = new SimulationConfig(
                WORLD_WIDTH,
                WORLD_HEIGHT,
                initialGenome,
                INITIAL_HERBIVORES,
                INITIAL_FOOD,
                SEED,
                MUTATION_STRENGTH

        );

        this.engine = new SimulationEngine(config);

        this.tickIntervalNanos = Math.round(
                engine.getTickDurationSeconds()
                        * NANOS_PER_SECOND
        );
    }

    double getWorldWidth() {
        return engine.getWorld().getWidth();
    }

    double getWorldHeight() {
        return engine.getWorld().getHeight();
    }

    void update(
            long now,
            JavaFxViewer viewer
    ) {
        if (previousTimeNanos < 0L) {
            previousTimeNanos = now;
            return;
        }

        long elapsedNanos =
                now - previousTimeNanos;

        previousTimeNanos = now;

        accumulatorNanos += elapsedNanos;

        boolean tickOccurred = false;

        while (accumulatorNanos >= tickIntervalNanos) {
            engine.tick();

            accumulatorNanos -= tickIntervalNanos;

            tickOccurred = true;
        }

        if (tickOccurred) {
            drawCurrentState(viewer);
        }
    }

    void drawCurrentState(JavaFxViewer viewer) {
        viewer.clear();

        drawFoods(viewer);
        drawEggs(viewer);
        drawHerbivores(viewer);
    }

    private void drawFoods(JavaFxViewer viewer) {
        for (Food food : engine.getWorld().getFoods()) {
            viewer.drawFood(
                    food.getX(),
                    food.getY(),
                    food.getRadius()
            );
        }
    }

    private void drawEggs(JavaFxViewer viewer) {
        for (Egg egg : engine.getWorld().getHerbivoreEggs()) {
            viewer.drawEgg(
                    egg.getX(),
                    egg.getY(),
                    egg.getRadius()
            );
        }
    }

    private void drawHerbivores(JavaFxViewer viewer) {
        for (Herbivore herbivore :
                engine.getWorld().getHerbivores()) {

            viewer.drawHerbivore(
                    herbivore.getX(),
                    herbivore.getY(),
                    herbivore.getRadius(),
                    herbivore.getGenome().getHue()
            );
        }
    }
}