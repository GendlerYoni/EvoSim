
package io.github.gendleryoni.evosim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimulationSessionTest {

    private static final long TICK_NANOS = 50_000_000L;
    private static final long MILLISECOND = 1_000_000L;
    private static final long SECOND = 1_000_000_000L;

    private SimulationConfig createConfig(
            int herbivores,
            int predators,
            int food,
            long seed
    ) {
        return new SimulationConfig(
                new Genome(1.0, 1.0, 1.0, 1.0, 1.0, 180.0),
                herbivores,
                predators,
                food,
                seed,
                0
        );
    }

    private SimulationSession createRunningSession() {
        return new SimulationSession(
                createConfig(1, 0, 0, 12345L)
        );
    }

    private SimulationSession createEmptySession() {
        return new SimulationSession(
                createConfig(0, 0, 0, 12345L)
        );
    }

    // ---------------------------------------------------------
    // Construction and initial state
    // ---------------------------------------------------------

    @Test
    void rejectsNullConfig() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationSession(null)
        );
    }

    @Test
    void startsRunningAtDefaultSpeed() {
        SimulationSession session = createRunningSession();

        assertAll(
                () -> assertEquals(
                        SimulationSession.State.RUNNING,
                        session.getState()
                ),
                () -> assertEquals(
                        1, session.getSpeedMultiplier()
                ),
                () -> assertEquals(
                        0L, session.getTickCount()
                )
        );
    }

    @Test
    void retainsInitialConfig() {
        SimulationConfig config = createConfig(
                2, 1, 50, 12345L
        );

        SimulationSession session =
                new SimulationSession(config);

        assertSame(config, session.getConfig());
    }

    // ---------------------------------------------------------
    // Tick scheduling and accumulator
    // ---------------------------------------------------------

    @Test
    void firstUpdateOnlyEstablishesTimeReference() {
        SimulationSession session = createRunningSession();

        assertFalse(session.update(100 * MILLISECOND));
        assertEquals(0L, session.getTickCount());
    }

    @Test
    void executesOneTickAfterOneTickInterval() {
        SimulationSession session = createRunningSession();

        session.update(0L);

        assertTrue(session.update(TICK_NANOS));
        assertEquals(1L, session.getTickCount());
    }

    @Test
    void doesNotTickBeforeEnoughTimeAccumulates() {
        SimulationSession session = createRunningSession();

        session.update(0L);

        assertFalse(session.update(20 * MILLISECOND));
        assertFalse(session.update(40 * MILLISECOND));

        assertEquals(0L, session.getTickCount());

        assertTrue(session.update(50 * MILLISECOND));
        assertEquals(1L, session.getTickCount());
    }

    @Test
    void preservesUnusedAccumulatedTime() {
        SimulationSession session = createRunningSession();

        session.update(0L);

        assertTrue(session.update(60 * MILLISECOND));
        assertEquals(1L, session.getTickCount());

        // 10ms remained from the previous update.
        assertFalse(session.update(90 * MILLISECOND));

        // 10ms + 30ms + 20ms = 60ms -> one more tick.
        assertTrue(session.update(110 * MILLISECOND));

        assertEquals(2L, session.getTickCount());
    }

    @Test
    void executesMultipleTicksInOneUpdate() {
        SimulationSession session = createRunningSession();

        session.update(0L);

        assertTrue(session.update(260 * MILLISECOND));
        assertEquals(5L, session.getTickCount());

        // 10ms remained; another 40ms completes one tick.
        assertTrue(session.update(300 * MILLISECOND));
        assertEquals(6L, session.getTickCount());
    }

    @Test
    void updateAtSameTimestampDoesNotAdvanceSimulation() {
        SimulationSession session = createRunningSession();

        session.update(100 * MILLISECOND);

        assertFalse(session.update(100 * MILLISECOND));
        assertEquals(0L, session.getTickCount());
    }

    @Test
    void rejectsTimeMovingBackwardsWithoutCorruptingClock() {
        SimulationSession session = createRunningSession();

        session.update(100 * MILLISECOND);

        assertThrows(
                IllegalArgumentException.class,
                () -> session.update(90 * MILLISECOND)
        );

        // Failed update must not modify the previous timestamp.
        assertTrue(session.update(150 * MILLISECOND));
        assertEquals(1L, session.getTickCount());
    }

    // ---------------------------------------------------------
    // Pause and resume
    // ---------------------------------------------------------

    @Test
    void pauseChangesState() {
        SimulationSession session = createRunningSession();

        session.pause(0L);

        assertEquals(
                SimulationSession.State.PAUSED,
                session.getState()
        );
    }

    @Test
    void pauseProcessesElapsedTimeBeforeStopping() {
        SimulationSession session = createRunningSession();

        session.update(0L);
        session.pause(80 * MILLISECOND);

        assertEquals(1L, session.getTickCount());
        assertEquals(
                SimulationSession.State.PAUSED,
                session.getState()
        );
    }

    @Test
    void pausedSessionDoesNotAdvance() {
        SimulationSession session = createRunningSession();

        session.update(0L);
        session.pause(0L);

        assertFalse(session.update(10 * SECOND));
        assertEquals(0L, session.getTickCount());
    }

    @Test
    void resumeChangesStateBackToRunning() {
        SimulationSession session = createRunningSession();

        session.pause(0L);
        session.resume();

        assertEquals(
                SimulationSession.State.RUNNING,
                session.getState()
        );
    }

    @Test
    void resumeDoesNotCountPausedTime() {
        SimulationSession session = createRunningSession();

        session.update(0L);
        session.pause(0L);

        session.resume();

        // Ten seconds passed while the simulation was paused.
        assertFalse(session.update(10 * SECOND));
        assertEquals(0L, session.getTickCount());

        assertTrue(
                session.update(10 * SECOND + TICK_NANOS)
        );

        assertEquals(1L, session.getTickCount());
    }

    @Test
    void resumePreservesUnusedAccumulator() {
        SimulationSession session = createRunningSession();

        session.update(0L);

        // Accumulate 20ms without completing a tick.
        session.pause(20 * MILLISECOND);

        session.resume();
        session.update(10 * SECOND);

        // 20ms remained before pause; another 30ms completes a tick.
        assertTrue(
                session.update(10 * SECOND + 30 * MILLISECOND)
        );

        assertEquals(1L, session.getTickCount());
    }

    @Test
    void repeatedPauseHasNoAdditionalEffect() {
        SimulationSession session = createRunningSession();

        session.update(0L);
        session.pause(20 * MILLISECOND);

        session.pause(200 * MILLISECOND);

        assertEquals(
                SimulationSession.State.PAUSED,
                session.getState()
        );

        session.resume();
        session.update(300 * MILLISECOND);

        assertTrue(session.update(330 * MILLISECOND));
        assertEquals(1L, session.getTickCount());
    }

    @Test
    void resumeWhileAlreadyRunningDoesNothing() {
        SimulationSession session = createRunningSession();

        session.update(0L);
        session.resume();

        assertTrue(session.update(TICK_NANOS));
        assertEquals(1L, session.getTickCount());
    }

    // ---------------------------------------------------------
    // Speed multiplier
    // ---------------------------------------------------------

    @Test
    void changesSpeedByDelta() {
        SimulationSession session = createRunningSession();

        session.changeSpeed(4, 0L);
        assertEquals(5, session.getSpeedMultiplier());

        session.changeSpeed(10, 0L);
        assertEquals(15, session.getSpeedMultiplier());

        session.changeSpeed(-2, 0L);
        assertEquals(13, session.getSpeedMultiplier());
    }

    @Test
    void clampsSpeedAtMaximum() {
        SimulationSession session = createRunningSession();

        session.changeSpeed(94, 0L);
        assertEquals(95, session.getSpeedMultiplier());

        session.changeSpeed(10, 0L);
        assertEquals(100, session.getSpeedMultiplier());
    }

    @Test
    void clampsSpeedAtMinimum() {
        SimulationSession session = createRunningSession();

        session.changeSpeed(9, 0L);
        session.changeSpeed(-20, 0L);

        assertEquals(1, session.getSpeedMultiplier());
    }

    @Test
    void extremeSpeedDeltasCannotOverflow() {
        SimulationSession session = createRunningSession();

        session.changeSpeed(Integer.MAX_VALUE, 0L);
        assertEquals(100, session.getSpeedMultiplier());

        session.changeSpeed(Integer.MIN_VALUE, 0L);
        assertEquals(1, session.getSpeedMultiplier());
    }

    @Test
    void speedMultiplierIncreasesTickCount() {
        SimulationSession session = createRunningSession();

        // Change from 1x to 5x.
        session.changeSpeed(4, 0L);

        // 100ms * 5 = 500ms = 10 ticks.
        assertTrue(session.update(100 * MILLISECOND));

        assertEquals(10L, session.getTickCount());
    }

    @Test
    void speedChangeProcessesEarlierTimeAtOldSpeed() {
        SimulationSession session = createRunningSession();

        session.update(0L);

        // Process the first 30ms at 1x, then switch to 5x.
        session.changeSpeed(4, 30 * MILLISECOND);

        assertEquals(0L, session.getTickCount());

        // Another 4ms at 5x contributes 20ms.
        // 30ms + 20ms = one full tick.
        assertTrue(session.update(34 * MILLISECOND));

        assertEquals(1L, session.getTickCount());
    }

    @Test
    void changingSpeedWhilePausedDoesNotAccumulateTime() {
        SimulationSession session = createRunningSession();

        session.update(0L);
        session.pause(0L);

        session.changeSpeed(9, 10 * SECOND);

        assertEquals(10, session.getSpeedMultiplier());
        assertEquals(0L, session.getTickCount());

        session.resume();

        // Reestablish the reference time.
        assertFalse(session.update(20 * SECOND));

        // At 10x, 5ms represent one 50ms tick.
        assertTrue(
                session.update(20 * SECOND + 5 * MILLISECOND)
        );

        assertEquals(1L, session.getTickCount());
    }

    @Test
    void rejectsScaledTimeOverflowWithoutAdvancing() {
        SimulationSession session = createRunningSession();

        session.update(0L);
        session.changeSpeed(99, 0L);

        assertThrows(
                ArithmeticException.class,
                () -> session.update(Long.MAX_VALUE)
        );

        assertEquals(0L, session.getTickCount());

        // Failed calculation did not corrupt the time reference.
        assertTrue(session.update(500_000L));
        assertEquals(1L, session.getTickCount());
    }

    // ---------------------------------------------------------
    // Finished state
    // ---------------------------------------------------------

    @Test
    void emptySimulationFinishesBeforeFirstTick() {
        SimulationSession session = createEmptySession();

        assertEquals(
                SimulationSession.State.RUNNING,
                session.getState()
        );

        session.update(0L);

        // The update detects extinction before running the tick.
        assertTrue(session.update(TICK_NANOS));

        assertAll(
                () -> assertEquals(
                        SimulationSession.State.FINISHED,
                        session.getState()
                ),
                () -> assertEquals(
                        0L, session.getTickCount()
                )
        );
    }

    @Test
    void foodAloneDoesNotPreventExtinction() {
        SimulationSession session = new SimulationSession(
                createConfig(0, 0, 100, 12345L)
        );

        session.update(0L);
        session.update(TICK_NANOS);

        assertEquals(
                SimulationSession.State.FINISHED,
                session.getState()
        );
        assertEquals(0L, session.getTickCount());
    }

    @Test
    void finishedSessionIgnoresFurtherUpdatesAndControls() {
        SimulationSession session = createEmptySession();

        session.update(0L);
        session.update(TICK_NANOS);

        assertFalse(session.update(10 * SECOND));

        session.resume();
        session.pause(10 * SECOND);

        assertEquals(
                SimulationSession.State.FINISHED,
                session.getState()
        );
        assertEquals(0L, session.getTickCount());
    }

    @Test
    void predatorWithoutFoodEventuallyCausesExtinction() {
        SimulationSession session = new SimulationSession(
                createConfig(0, 1, 0, 12345L)
        );

        session.update(0L);

        // No prey or food: the predator eventually runs out of energy.
        session.update(200 * TICK_NANOS);

        assertEquals(
                SimulationSession.State.FINISHED,
                session.getState()
        );

        // The session stops instead of processing the full 200 ticks.
        assertTrue(session.getTickCount() < 200L);
    }

    // ---------------------------------------------------------
    // Reset
    // ---------------------------------------------------------

    @Test
    void resetRestartsRunningSimulation() {
        SimulationSession session = createRunningSession();

        session.update(0L);
        session.update(5 * TICK_NANOS);

        assertEquals(5L, session.getTickCount());

        session.reset();

        assertAll(
                () -> assertEquals(
                        SimulationSession.State.RUNNING,
                        session.getState()
                ),
                () -> assertEquals(
                        0L, session.getTickCount()
                )
        );
    }

    @Test
    void resetWorksWhilePaused() {
        SimulationSession session = createRunningSession();

        session.pause(0L);
        session.reset();

        assertEquals(
                SimulationSession.State.RUNNING,
                session.getState()
        );
        assertEquals(0L, session.getTickCount());
    }

    @Test
    void resetWorksAfterFinished() {
        SimulationSession session = createEmptySession();

        session.update(0L);
        session.update(TICK_NANOS);

        assertEquals(
                SimulationSession.State.FINISHED,
                session.getState()
        );

        session.reset();

        assertEquals(
                SimulationSession.State.RUNNING,
                session.getState()
        );
        assertEquals(0L, session.getTickCount());
    }

    @Test
    void resetGeneratesDifferentSeed() {
        SimulationSession session = createRunningSession();

        long originalSeed = session.getConfig().getSeed();

        session.reset();

        assertNotEquals(
                originalSeed,
                session.getConfig().getSeed()
        );
    }

    @Test
    void resetPreservesConfigurationExceptSeed() {
        SimulationConfig original = createConfig(
                10, 5, 100, 12345L
        );

        SimulationSession session =
                new SimulationSession(original);

        session.reset();

        SimulationConfig afterReset = session.getConfig();

        assertAll(
                () -> assertNotSame(original, afterReset),
                () -> assertSame(
                        original.getInitialGenome(),
                        afterReset.getInitialGenome()
                ),
                () -> assertEquals(
                        original.getInitialHerbivores(),
                        afterReset.getInitialHerbivores()
                ),
                () -> assertEquals(
                        original.getInitialPredators(),
                        afterReset.getInitialPredators()
                ),
                () -> assertEquals(
                        original.getInitialFood(),
                        afterReset.getInitialFood()
                ),
                () -> assertEquals(
                        original.getFoodSpawnMaxPerTick(),
                        afterReset.getFoodSpawnMaxPerTick()
                ),
                () -> assertNotEquals(
                        original.getSeed(),
                        afterReset.getSeed()
                )
        );
    }

    @Test
    void consecutiveResetsUseDifferentSeeds() {
        SimulationSession session = createRunningSession();

        long firstSeed = session.getConfig().getSeed();

        session.reset();
        long secondSeed = session.getConfig().getSeed();

        session.reset();
        long thirdSeed = session.getConfig().getSeed();

        assertNotEquals(firstSeed, secondSeed);
        assertNotEquals(secondSeed, thirdSeed);
    }

    @Test
    void resetPreservesCurrentSpeed() {
        SimulationSession session = createRunningSession();

        session.changeSpeed(24, 0L);

        assertEquals(25, session.getSpeedMultiplier());

        session.reset();

        assertEquals(25, session.getSpeedMultiplier());
        assertEquals(
                SimulationSession.State.RUNNING,
                session.getState()
        );
    }

    @Test
    void resetClearsAccumulatorAndPreviousTime() {
        SimulationSession session = createRunningSession();

        session.update(0L);

        // Accumulate 40ms without completing a tick.
        session.update(40 * MILLISECOND);

        assertEquals(0L, session.getTickCount());

        session.reset();

        // First update after reset only establishes the new clock.
        assertFalse(session.update(SECOND));

        // If the old 40ms remained, this would already cause a tick.
        assertFalse(
                session.update(SECOND + 20 * MILLISECOND)
        );

        assertEquals(0L, session.getTickCount());

        assertTrue(
                session.update(SECOND + 50 * MILLISECOND)
        );

        assertEquals(1L, session.getTickCount());
    }
}
