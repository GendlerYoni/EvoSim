
package io.github.gendleryoni.evosim;

import io.github.gendleryoni.evosim.statistics.StatisticsBatch;

import java.util.concurrent.ThreadLocalRandom;
import java.util.List;

public class SimulationSession {

    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private static final int MIN_SPEED_MULTIPLIER = 1;
    private static final int MAX_SPEED_MULTIPLIER = 100;

    public enum State {
        RUNNING,
        PAUSED,
        FINISHED
    }

    private SimulationConfig config;
    private SimulationEngine engine;
    private State state;

    private final long tickIntervalNanos;
    private long previousUpdateNanos = -1L;
    private long accumulatorNanos = 0L;

    private int speedMultiplier = MIN_SPEED_MULTIPLIER;

    public SimulationSession(SimulationConfig config) {
        if (config == null) {
            throw new IllegalArgumentException(
                    "SimulationConfig cannot be null"
            );
        }

        this.config = config;
        this.engine = new SimulationEngine(config);
        this.state = State.RUNNING;

        this.tickIntervalNanos = Math.round(
                engine.getTickDurationSeconds() * NANOS_PER_SECOND
        );
    }

    public State getState() {
        return state;
    }

    public int getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void pause(long nowNanos) {
        if (state != State.RUNNING) {
            return;
        }

        // Account for elapsed time at the current speed before pausing.
        update(nowNanos);

        if (state == State.RUNNING) {
            state = State.PAUSED;
        }
    }

    public void resume() {
        if (state == State.PAUSED) {
            previousUpdateNanos = -1L;
            state = State.RUNNING;
        }
    }

    public boolean update(long nowNanos) {
        if (state != State.RUNNING) {
            return false;
        }

        if (previousUpdateNanos == -1L) {
            previousUpdateNanos = nowNanos;
            return false;
        }

        if (nowNanos < previousUpdateNanos) {
            throw new IllegalArgumentException(
                    "Time cannot move backwards"
            );
        }

        long elapsedNanos = Math.subtractExact(
                nowNanos, previousUpdateNanos
        );

        long scaledElapsedNanos = Math.multiplyExact(
                elapsedNanos, speedMultiplier
        );

        long updatedAccumulator = Math.addExact(
                accumulatorNanos, scaledElapsedNanos
        );

        previousUpdateNanos = nowNanos;
        accumulatorNanos = updatedAccumulator;

        boolean tickOccurred = false;

        while (accumulatorNanos >= tickIntervalNanos) {
            if (isExtinct()) {
                state = State.FINISHED;
                return true;
            }

            engine.tick();
            accumulatorNanos -= tickIntervalNanos;

            tickOccurred = true;
        }

        if (tickOccurred && isExtinct()) {
            state = State.FINISHED;
        }

        return tickOccurred;
    }

    public void changeSpeed(int delta, long nowNanos) {
        if (state == State.RUNNING) {
            // Process elapsed time at the previous speed first.
            update(nowNanos);
        }

        long requestedSpeed = (long) speedMultiplier + delta;

        speedMultiplier = (int) Math.max(
                MIN_SPEED_MULTIPLIER,
                Math.min(MAX_SPEED_MULTIPLIER, requestedSpeed)
        );
    }

    public void reset() {
        long newSeed;

        do {
            newSeed = ThreadLocalRandom.current().nextLong();
        } while (newSeed == config.getSeed());

        // Reset currently creates a new run with a different seed.
        // For a future same-seed reset, recreate SimulationEngine
        // using the existing config without changing its seed.
        config = new SimulationConfig(
                config.getInitialGenome(),
                config.getInitialHerbivores(),
                config.getInitialPredators(),
                config.getInitialFood(),
                newSeed,
                config.getFoodSpawnMaxPerTick()
        );

        engine = new SimulationEngine(config);

        state = State.RUNNING;
        previousUpdateNanos = -1L;
        accumulatorNanos = 0L;

        // Keep the current speed multiplier.
    }

    public StatisticsBatch getStatisticsFromIndex(int fromIndex) {
        return engine.getStatisticsFromIndex(fromIndex);
    }

    public int getStatisticsIntervalTicks() {
        return engine.getStatisticsIntervalTicks();
    }

    public int getStatisticsHistoryCapacity() {
        return engine.getStatisticsHistoryCapacity();
    }

    private boolean isExtinct() {
        World world = engine.getWorld();

        return world.getHerbivores().isEmpty()
                && world.getPredators().isEmpty()
                && world.getHerbivoreEggs().isEmpty()
                && world.getPredatorEggs().isEmpty();
    }


    public long getTickCount() {
        return engine.getTickCount();
    }

    public SimulationConfig getConfig() {
        return config;
    }

    public double getWorldWidth() {
        return engine.getWorld().getWidth();
    }

    public double getWorldHeight() {
        return engine.getWorld().getHeight();
    }

    public List<Food> getFoods() {
        return engine.getWorld().getFoods();
    }

    public List<Herbivore> getHerbivores() {
        return engine.getWorld().getHerbivores();
    }

    public List<Predator> getPredators() {
        return engine.getWorld().getPredators();
    }

    public List<Egg> getHerbivoreEggs() {
        return engine.getWorld().getHerbivoreEggs();
    }

    public List<Egg> getPredatorEggs() {
        return engine.getWorld().getPredatorEggs();
    }
}
