
package io.github.gendleryoni.evosim.viewer;

import io.github.gendleryoni.evosim.Egg;
import io.github.gendleryoni.evosim.Food;
import io.github.gendleryoni.evosim.Genome;
import io.github.gendleryoni.evosim.Herbivore;
import io.github.gendleryoni.evosim.Predator;
import io.github.gendleryoni.evosim.SimulationConfig;
import io.github.gendleryoni.evosim.SimulationSession;

import java.util.concurrent.ThreadLocalRandom;

class JavaFxSimulationRunner {

    private static final int INITIAL_HERBIVORES = 1000;
    private static final int INITIAL_PREDATORS = 100;
    private static final int INITIAL_FOOD = 200;
    private static final int FOOD_SPAWN_MAX = 7;

    private final SimulationSession session;

    JavaFxSimulationRunner() {
        Genome initialGenome = new Genome(
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                180.0
        );

        long seed = ThreadLocalRandom.current().nextLong();

        SimulationConfig config = new SimulationConfig(
                initialGenome,
                INITIAL_HERBIVORES,
                INITIAL_PREDATORS,
                INITIAL_FOOD,
                seed,
                FOOD_SPAWN_MAX
        );

        this.session = new SimulationSession(config);
    }

    double getWorldWidth() {
        return session.getWorldWidth();
    }

    double getWorldHeight() {
        return session.getWorldHeight();
    }

    void update(long now, JavaFxViewer viewer) {
        if (session.update(now)) {
            drawCurrentState(viewer);
        }
    }

    void drawCurrentState(JavaFxViewer viewer) {
        viewer.clear();

        drawFoods(viewer);
        drawHerbivoreEggs(viewer);
        drawPredatorEggs(viewer);
        drawHerbivores(viewer);
        drawPredators(viewer);
    }

    private void drawFoods(JavaFxViewer viewer) {
        for (Food food : session.getFoods()) {
            viewer.drawFood(
                    food.getX(),
                    food.getY(),
                    food.getRadius()
            );
        }
    }

    private void drawHerbivoreEggs(JavaFxViewer viewer) {
        for (Egg egg : session.getHerbivoreEggs()) {
            viewer.drawEgg(
                    egg.getX(),
                    egg.getY(),
                    egg.getRadius()
            );
        }
    }

    private void drawPredatorEggs(JavaFxViewer viewer) {
        for (Egg egg : session.getPredatorEggs()) {
            viewer.drawEgg(
                    egg.getX(),
                    egg.getY(),
                    egg.getRadius()
            );
        }
    }

    private void drawHerbivores(JavaFxViewer viewer) {
        for (Herbivore herbivore : session.getHerbivores()) {
            viewer.drawHerbivore(
                    herbivore.getX(),
                    herbivore.getY(),
                    herbivore.getRadius(),
                    herbivore.getGenome().getHue()
            );
        }
    }

    private void drawPredators(JavaFxViewer viewer) {
        for (Predator predator : session.getPredators()) {
            viewer.drawPredator(
                    predator.getX(),
                    predator.getY(),
                    predator.getRadius(),
                    predator.getGenome().getHue()
            );
        }
    }

    void pause(long nowNanos) {
        session.pause(nowNanos);
    }

    void resume() {
        session.resume();
    }

    void reset() {
        session.reset();
    }

    void changeSpeed(int delta, long nowNanos) {
        session.changeSpeed(delta, nowNanos);
    }

    SimulationSession.State getState() {
        return session.getState();
    }

    int getSpeedMultiplier() {
        return session.getSpeedMultiplier();
    }
}
