package io.github.gendleryoni.evosim.service;

import io.github.gendleryoni.evosim.Genome;
import io.github.gendleryoni.evosim.SimulationConfig;
import io.github.gendleryoni.evosim.SimulationSession;
import io.github.gendleryoni.evosim.dto.CreateSimulationRequest;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SimulationService {

    private static final int MAX_SESSIONS = 10;

    private final Map<UUID, SimulationSession> sessions =
            new ConcurrentHashMap<>();

    public synchronized UUID createSimulation(
            CreateSimulationRequest request
    ) {
        if (sessions.size() >= MAX_SESSIONS) {
            throw new ServerAtCapacityException();
        }

        long seed = ThreadLocalRandom.current().nextLong();

        Genome initialGenome = new Genome(
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                180.0
        );

        SimulationConfig config = new SimulationConfig(
                initialGenome,
                request.initialHerbivores(),
                request.initialPredators(),
                request.initialFood(),
                seed,
                request.foodSpawnMaxPerTick()
        );

        SimulationSession session = new SimulationSession(config);

        UUID sessionId = UUID.randomUUID();

        sessions.put(sessionId, session);

        return sessionId;
    }
}