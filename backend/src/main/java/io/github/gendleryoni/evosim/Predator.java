package io.github.gendleryoni.evosim;

public class Predator extends Creature {

    public Predator(
            int id,
            double x,
            double y,
            double energy,
            int generation,
            Genome genome,
            Direction explorationDirection
    ) {
        super(
                id,
                x,
                y,
                energy,
                generation,
                genome,
                explorationDirection
        );
    }
}