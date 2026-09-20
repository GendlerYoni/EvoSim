package io.github.gendleryoni.evosim;

public class Herbivore extends Creature {

    public Herbivore(
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