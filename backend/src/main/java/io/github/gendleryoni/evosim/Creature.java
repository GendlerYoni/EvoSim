package io.github.gendleryoni.evosim;

public abstract class Creature {

    private final int id;
    private double x;
    private double y;
    private double energy;
    private final int generation;
    private final Genome genome;

    // TODO: Consider adding separate lineage metadata
    // (e.g. "7_18_59") if lineage tracking is needed later.

    protected Creature(
            int id,
            double x,
            double y,
            double energy,
            int generation,
            Genome genome
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException("Creature id must be positive");
        }

        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Creature position must be finite");
        }

        if (!Double.isFinite(energy) || energy <= 0) {
            throw new IllegalArgumentException("Creature energy must be positive and finite");
        }

        if (generation < 1) {
            throw new IllegalArgumentException("Creature generation must be at least 1");
        }

        if (genome == null) {
            throw new IllegalArgumentException("Creature genome cannot be null");
        }

        this.id = id;
        this.x = x;
        this.y = y;
        this.energy = energy;
        this.generation = generation;
        this.genome = genome;
    }

    public long getId() {
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
}