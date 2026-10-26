
package io.github.gendleryoni.evosim.service;

import io.github.gendleryoni.evosim.dto.CreateSimulationRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SimulationServiceTest {

    private static final int MAX_SESSIONS = 10;

    private CreateSimulationRequest createValidRequest() {
        return new CreateSimulationRequest(1, 1, 1, 1);
    }

    @Test
    void createSimulationReturnsSessionId() {
        SimulationService service = new SimulationService();

        UUID sessionId = service.createSimulation(
                createValidRequest()
        );

        assertNotNull(sessionId);
    }

    @Test
    void createSimulationReturnsUniqueSessionIds() {
        SimulationService service = new SimulationService();

        UUID firstId = service.createSimulation(
                createValidRequest()
        );

        UUID secondId = service.createSimulation(
                createValidRequest()
        );

        assertNotEquals(firstId, secondId);
    }

    @Test
    void zeroInitialEntitiesAreAllowed() {
        SimulationService service = new SimulationService();

        CreateSimulationRequest request =
                new CreateSimulationRequest(0, 0, 0, 0);

        UUID sessionId = service.createSimulation(request);

        assertNotNull(sessionId);
    }

    @Test
    void createSimulationRejectsWhenServerIsAtCapacity() {
        SimulationService service = new SimulationService();

        for (int i = 0; i < MAX_SESSIONS; i++) {
            assertNotNull(
                    service.createSimulation(createValidRequest())
            );
        }

        assertThrows(
                ServerAtCapacityException.class,
                () -> service.createSimulation(createValidRequest())
        );

        // The server must continue rejecting requests.
        assertThrows(
                ServerAtCapacityException.class,
                () -> service.createSimulation(createValidRequest())
        );
    }

    @Test
    void concurrentRequestsCannotExceedCapacity() throws Exception {
        SimulationService service = new SimulationService();

        int requestCount = 20;

        ExecutorService executor =
                Executors.newFixedThreadPool(requestCount);

        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            List<Future<UUID>> futures = new ArrayList<>();

            for (int i = 0; i < requestCount; i++) {
                futures.add(executor.submit(() -> {
                    startLatch.await();

                    try {
                        return service.createSimulation(
                                createValidRequest()
                        );
                    } catch (ServerAtCapacityException exception) {
                        return null;
                    }
                }));
            }

            // Let all submitted tasks start.
            startLatch.countDown();

            Set<UUID> createdIds = new HashSet<>();
            int rejectedRequests = 0;

            for (Future<UUID> future : futures) {
                UUID sessionId = future.get(
                        15, TimeUnit.SECONDS
                );

                if (sessionId == null) {
                    rejectedRequests++;
                } else {
                    assertTrue(createdIds.add(sessionId));
                }
            }

            assertEquals(MAX_SESSIONS, createdIds.size());
            assertEquals(
                    requestCount - MAX_SESSIONS,
                    rejectedRequests
            );

        } finally {
            startLatch.countDown();
            executor.shutdownNow();
        }
    }
}
