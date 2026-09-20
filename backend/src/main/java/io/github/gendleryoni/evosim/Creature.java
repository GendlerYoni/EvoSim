package io.github.gendleryoni.evosim;

public abstract class Creature {

    private static final double BASE_CREATURE_RADIUS = 1.0;
    private static final double BASE_SPEED = 5.0;
    private static final double BASE_SENSE_RADIUS = 15.0;

    private final int id;
    private double x;
    private double y;
    private double energy;
    private final int generation;
    private final Genome genome;

    private Direction explorationDirection;

    // TODO: Consider adding separate lineage metadata
    // (e.g. "7_18_59") if lineage tracking is needed later.

    protected Creature(
            int id,
            double x,
            double y,
            double energy,
            int generation,
            Genome genome,
            Direction explorationDirection
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Creature id must be positive"
            );
        }

        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException(
                    "Creature position must be finite"
            );
        }

        if (!Double.isFinite(energy) || energy <= 0) {
            throw new IllegalArgumentException(
                    "Creature energy must be positive and finite"
            );
        }

        if (generation < 1) {
            throw new IllegalArgumentException(
                    "Creature generation must be at least 1"
            );
        }

        if (genome == null) {
            throw new IllegalArgumentException(
                    "Creature genome cannot be null"
            );
        }

        if (explorationDirection == null) {
            throw new IllegalArgumentException(
                    "Exploration direction cannot be null"
            );
        }

        this.id = id;
        this.x = x;
        this.y = y;
        this.energy = energy;
        this.generation = generation;
        this.genome = genome;
        this.explorationDirection = explorationDirection;
    }

    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getEnergy() {
        return energy;
    }

    public int getGeneration() {
        return generation;
    }

    public Genome getGenome() {
        return genome;
    }

    public Direction getExplorationDirection() {
        return explorationDirection;
    }

    public double getRadius() {
        return BASE_CREATURE_RADIUS * genome.getSize();
    }

    public double getSpeed() {
        return BASE_SPEED * genome.getSpeed();
    }

    public double getSenseRadius() {
        return BASE_SENSE_RADIUS * genome.getSenseRadius();
    }

    double getMovementDistance(double tickDurationSeconds) {
        if (!Double.isFinite(tickDurationSeconds)
                || tickDurationSeconds < 0) {
            throw new IllegalArgumentException(
                    "Tick duration must be non-negative and finite"
            );
        }

        return getSpeed() * tickDurationSeconds;
    }

    void moveTo(double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException(
                    "Creature position must be finite"
            );
        }

        this.x = x;
        this.y = y;
    }

    void changeExplorationDirection(Direction direction) {
        if (direction == null) {
            throw new IllegalArgumentException(
                    "Exploration direction cannot be null"
            );
        }

        this.explorationDirection = direction;
    }
}