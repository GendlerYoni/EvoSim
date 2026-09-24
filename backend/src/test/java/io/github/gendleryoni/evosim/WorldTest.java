package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {

    private Genome createGenome() {
        return new Genome(
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                180.0
        );
    }

    private Herbivore createHerbivore(double x, double y) {
        return new Herbivore(
                1,
                x,
                y,
                100.0,
                1,
                createGenome(),
                Direction.NORTH
        );
    }

    @Test
    void storesConfiguredDimensions() {
        World world = new World(100.0, 200.0);

        assertEquals(100.0, world.getWidth());
        assertEquals(200.0, world.getHeight());

        assertTrue(world.isInBounds(50.0, 100.0));
        assertTrue(world.isInBounds(0.0, 0.0));
        assertTrue(world.isInBounds(100.0, 200.0));
    }

    @Test
    void rejectsPositionsOutsideWorldBounds() {
        World world = new World(100.0, 200.0);

        assertFalse(world.isInBounds(-0.1, 100.0));
        assertFalse(world.isInBounds(100.1, 100.0));
        assertFalse(world.isInBounds(50.0, -0.1));
        assertFalse(world.isInBounds(50.0, 200.1));
    }

    @Test
    void rejectsInvalidDimensions() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new World(0.0, 100.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new World(100.0, 0.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new World(-1.0, 100.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new World(100.0, -1.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new World(Double.NaN, 100.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new World(100.0, Double.POSITIVE_INFINITY)
        );
    }

    @Test
    void addsFoodInsideWorldBounds() {
        World world = new World(100.0, 100.0);

        Food food = world.addFood(25.0, 30.0);

        List<Food> nearbyFoods =
                world.findFoodWithinRadius(25.0, 30.0, 0.0);

        assertEquals(1, nearbyFoods.size());
        assertSame(food, nearbyFoods.get(0));
    }

    @Test
    void allowsFoodExactlyOnPhysicalWorldBoundary() {
        World world = new World(100.0, 100.0);

        Food topLeft = world.addFood(3.0, 3.0);
        Food bottomRight = world.addFood(97.0, 97.0);

        assertNotNull(topLeft);
        assertNotNull(bottomRight);
    }

    @Test
    void rejectsFoodWhoseBodyExtendsOutsideWorldBounds() {
        World world = new World(100.0, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> world.addFood(2.9, 50.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.addFood(97.1, 50.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.addFood(50.0, 2.9)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.addFood(50.0, 97.1)
        );
    }

    @Test
    void rejectsFoodWithNonFinitePosition() {
        World world = new World(100.0, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> world.addFood(Double.NaN, 50.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.addFood(
                        50.0,
                        Double.POSITIVE_INFINITY
                )
        );
    }

    @Test
    void removesExistingFood() {
        World world = new World(100.0, 100.0);

        Food food = world.addFood(25.0, 30.0);

        world.removeFood(food);

        List<Food> nearbyFoods =
                world.findFoodWithinRadius(25.0, 30.0, 0.0);

        assertTrue(nearbyFoods.isEmpty());
    }

    @Test
    void rejectsRemovingFoodThatDoesNotExistInWorld() {
        World world = new World(100.0, 100.0);

        Food food = new Food(25.0, 30.0);

        assertThrows(
                IllegalStateException.class,
                () -> world.removeFood(food)
        );
    }

    @Test
    void rejectsRemovingNullFood() {
        World world = new World(100.0, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> world.removeFood(null)
        );
    }

    @Test
    void findsOnlyFoodWithinRadius() {
        World world = new World(100.0, 100.0);

        Food closeFood = world.addFood(53.0, 54.0);
        Food farFood = world.addFood(70.0, 70.0);

        List<Food> nearbyFoods =
                world.findFoodWithinRadius(
                        50.0,
                        50.0,
                        5.0
                );

        assertEquals(1, nearbyFoods.size());
        assertSame(closeFood, nearbyFoods.get(0));
        assertFalse(nearbyFoods.contains(farFood));
    }

    @Test
    void includesFoodExactlyOnRadiusBoundary() {
        World world = new World(100.0, 100.0);

        Food food = world.addFood(53.0, 54.0);

        List<Food> nearbyFoods =
                world.findFoodWithinRadius(
                        50.0,
                        50.0,
                        5.0
                );

        assertEquals(1, nearbyFoods.size());
        assertSame(food, nearbyFoods.get(0));
    }

    @Test
    void zeroRadiusFindsFoodAtExactPosition() {
        World world = new World(100.0, 100.0);

        Food exactFood = world.addFood(50.0, 50.0);
        world.addFood(50.1, 50.0);

        List<Food> nearbyFoods =
                world.findFoodWithinRadius(
                        50.0,
                        50.0,
                        0.0
                );

        assertEquals(1, nearbyFoods.size());
        assertSame(exactFood, nearbyFoods.get(0));
    }

    @Test
    void rejectsInvalidRadiusQuery() {
        World world = new World(100.0, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> world.findFoodWithinRadius(
                        50.0,
                        50.0,
                        -1.0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.findFoodWithinRadius(
                        50.0,
                        50.0,
                        Double.NaN
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.findFoodWithinRadius(
                        50.0,
                        50.0,
                        Double.POSITIVE_INFINITY
                )
        );
    }

    @Test
    void rejectsNonFiniteQueryPosition() {
        World world = new World(100.0, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> world.findFoodWithinRadius(
                        Double.NaN,
                        50.0,
                        10.0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.findFoodWithinRadius(
                        50.0,
                        Double.POSITIVE_INFINITY,
                        10.0
                )
        );
    }

    @Test
    void returnedFoodListDoesNotExposeWorldStorage() {
        World world = new World(100.0, 100.0);

        Food food = world.addFood(50.0, 50.0);

        List<Food> nearbyFoods =
                world.findFoodWithinRadius(
                        50.0,
                        50.0,
                        10.0
                );

        nearbyFoods.clear();

        List<Food> secondQuery =
                world.findFoodWithinRadius(
                        50.0,
                        50.0,
                        10.0
                );

        assertEquals(1, secondQuery.size());
        assertSame(food, secondQuery.get(0));
    }

    @Test
    void foodListCannotBeModifiedExternally() {
        World world = new World(100.0, 100.0);

        Food food = world.addFood(
                50.0,
                50.0
        );

        List<Food> foods = world.getFoods();

        assertThrows(
                UnsupportedOperationException.class,
                foods::clear
        );

        assertEquals(
                1,
                world.getFoods().size()
        );

        assertSame(
                food,
                world.getFoods().get(0)
        );
    }

    @Test
    void addsHerbivoreInsideWorldBounds() {
        World world = new World(100.0, 100.0);
        Herbivore herbivore = createHerbivore(50.0, 50.0);

        world.addHerbivore(herbivore);

        assertEquals(1, world.getHerbivores().size());
        assertSame(herbivore, world.getHerbivores().get(0));
    }

    @Test
    void allowsHerbivoreExactlyOnPhysicalWorldBoundary() {
        World world = new World(100.0, 100.0);

        Herbivore topLeft = createHerbivore(1.0, 1.0);
        Herbivore bottomRight = new Herbivore(
                2,
                99.0,
                99.0,
                100.0,
                1,
                createGenome(),
                Direction.NORTH
        );

        world.addHerbivore(topLeft);
        world.addHerbivore(bottomRight);

        assertEquals(2, world.getHerbivores().size());
    }

    @Test
    void rejectsHerbivoreWhoseBodyExtendsOutsideWorldBounds() {
        World world = new World(100.0, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> world.addHerbivore(
                        createHerbivore(0.9, 50.0)
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.addHerbivore(
                        createHerbivore(99.1, 50.0)
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.addHerbivore(
                        createHerbivore(50.0, 0.9)
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.addHerbivore(
                        createHerbivore(50.0, 99.1)
                )
        );
    }

    @Test
    void rejectsAddingNullHerbivore() {
        World world = new World(100.0, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> world.addHerbivore(null)
        );
    }

    @Test
    void removesExistingHerbivore() {
        World world = new World(100.0, 100.0);
        Herbivore herbivore = createHerbivore(50.0, 50.0);

        world.addHerbivore(herbivore);
        world.removeHerbivore(herbivore);

        assertTrue(world.getHerbivores().isEmpty());
    }

    @Test
    void rejectsRemovingHerbivoreThatDoesNotExistInWorld() {
        World world = new World(100.0, 100.0);
        Herbivore herbivore = createHerbivore(50.0, 50.0);

        assertThrows(
                IllegalStateException.class,
                () -> world.removeHerbivore(herbivore)
        );
    }

    @Test
    void rejectsRemovingNullHerbivore() {
        World world = new World(100.0, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> world.removeHerbivore(null)
        );
    }

    @Test
    void herbivoreListCannotBeModifiedExternally() {
        World world = new World(100.0, 100.0);
        Herbivore herbivore = createHerbivore(50.0, 50.0);

        world.addHerbivore(herbivore);

        List<Herbivore> herbivores = world.getHerbivores();

        assertThrows(
                UnsupportedOperationException.class,
                herbivores::clear
        );

        assertEquals(1, world.getHerbivores().size());
        assertSame(herbivore, world.getHerbivores().get(0));
    }
    @Test
    void movesCreatureNormally() {
        World world = new World(100.0, 100.0);
        Herbivore herbivore = createHerbivore(50.0, 50.0);

        world.addHerbivore(herbivore);

        boolean hitWall = world.moveCreature(
                herbivore,
                new Vector2D(1.0, 0.0),
                5.0
        );

        assertFalse(hitWall);
        assertEquals(55.0, herbivore.getX(), 1e-9);
        assertEquals(50.0, herbivore.getY(), 1e-9);
    }

    @Test
    void normalizesDiagonalMovement() {
        World world = new World(100.0, 100.0);
        Herbivore herbivore = createHerbivore(50.0, 50.0);

        world.addHerbivore(herbivore);

        boolean hitWall = world.moveCreature(
                herbivore,
                new Vector2D(1.0, 1.0),
                10.0
        );

        double expectedOffset = 10.0 / Math.sqrt(2.0);

        assertFalse(hitWall);
        assertEquals(
                50.0 + expectedOffset,
                herbivore.getX(),
                1e-9
        );
        assertEquals(
                50.0 + expectedOffset,
                herbivore.getY(),
                1e-9
        );
    }

    @Test
    void clampsCreatureAtLeftWall() {
        World world = new World(100.0, 100.0);
        Herbivore herbivore = createHerbivore(2.0, 50.0);

        world.addHerbivore(herbivore);

        boolean hitWall = world.moveCreature(
                herbivore,
                new Vector2D(-1.0, 0.0),
                10.0
        );

        assertTrue(hitWall);
        assertEquals(1.0, herbivore.getX(), 1e-9);
        assertEquals(50.0, herbivore.getY(), 1e-9);
    }

    @Test
    void clampsCreatureAtRightWall() {
        World world = new World(100.0, 100.0);
        Herbivore herbivore = createHerbivore(98.0, 50.0);

        world.addHerbivore(herbivore);

        boolean hitWall = world.moveCreature(
                herbivore,
                new Vector2D(1.0, 0.0),
                10.0
        );

        assertTrue(hitWall);
        assertEquals(99.0, herbivore.getX(), 1e-9);
        assertEquals(50.0, herbivore.getY(), 1e-9);
    }

    @Test
    void clampsCreatureAtTopWall() {
        World world = new World(100.0, 100.0);
        Herbivore herbivore = createHerbivore(50.0, 2.0);

        world.addHerbivore(herbivore);

        boolean hitWall = world.moveCreature(
                herbivore,
                new Vector2D(0.0, -1.0),
                10.0
        );

        assertTrue(hitWall);
        assertEquals(50.0, herbivore.getX(), 1e-9);
        assertEquals(1.0, herbivore.getY(), 1e-9);
    }

    @Test
    void clampsCreatureAtBottomWall() {
        World world = new World(100.0, 100.0);
        Herbivore herbivore = createHerbivore(50.0, 98.0);

        world.addHerbivore(herbivore);

        boolean hitWall = world.moveCreature(
                herbivore,
                new Vector2D(0.0, 1.0),
                10.0
        );

        assertTrue(hitWall);
        assertEquals(50.0, herbivore.getX(), 1e-9);
        assertEquals(99.0, herbivore.getY(), 1e-9);
    }

    @Test
    void slidesAlongWallWhenOnlyOneAxisExceedsBounds() {
        World world = new World(100.0, 100.0);
        Herbivore herbivore = createHerbivore(50.0, 2.0);

        world.addHerbivore(herbivore);

        boolean hitWall = world.moveCreature(
                herbivore,
                new Vector2D(1.0, -1.0),
                10.0
        );

        double expectedX =
                50.0 + 10.0 / Math.sqrt(2.0);

        assertTrue(hitWall);
        assertEquals(expectedX, herbivore.getX(), 1e-9);
        assertEquals(1.0, herbivore.getY(), 1e-9);
    }

    @Test
    void zeroVectorDoesNotMoveCreature() {
        World world = new World(100.0, 100.0);
        Herbivore herbivore = createHerbivore(50.0, 50.0);

        world.addHerbivore(herbivore);

        boolean hitWall = world.moveCreature(
                herbivore,
                new Vector2D(0.0, 0.0),
                10.0
        );

        assertFalse(hitWall);
        assertEquals(50.0, herbivore.getX(), 1e-9);
        assertEquals(50.0, herbivore.getY(), 1e-9);
    }

    @Test
    void rejectsInvalidMovementDistance() {
        World world = new World(100.0, 100.0);
        Herbivore herbivore = createHerbivore(50.0, 50.0);

        world.addHerbivore(herbivore);

        assertThrows(
                IllegalArgumentException.class,
                () -> world.moveCreature(
                        herbivore,
                        Direction.EAST.getVector(),
                        -1.0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.moveCreature(
                        herbivore,
                        Direction.EAST.getVector(),
                        Double.NaN
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> world.moveCreature(
                        herbivore,
                        Direction.EAST.getVector(),
                        Double.POSITIVE_INFINITY
                )
        );
    }

    @Test
    void rejectsNullCreatureMovement() {
        World world = new World(100.0, 100.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> world.moveCreature(
                        null,
                        Direction.EAST.getVector(),
                        1.0
                )
        );
    }

    @Test
    void rejectsNullMovementDirection() {
        World world = new World(100.0, 100.0);
        Herbivore herbivore = createHerbivore(50.0, 50.0);

        world.addHerbivore(herbivore);

        assertThrows(
                IllegalArgumentException.class,
                () -> world.moveCreature(
                        herbivore,
                        null,
                        1.0
                )
        );
    }

    @Test
    void rejectsMovingCreatureThatDoesNotBelongToWorld() {
        World world = new World(100.0, 100.0);
        Herbivore herbivore = createHerbivore(50.0, 50.0);

        assertThrows(
                IllegalStateException.class,
                () -> world.moveCreature(
                        herbivore,
                        Direction.EAST.getVector(),
                        1.0
                )
        );
    }
}