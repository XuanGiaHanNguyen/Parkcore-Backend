package com.parkcore.backend.broadcast;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class SimulationBroadcaster {

    private final SimpMessagingTemplate messaging;

    public SimulationBroadcaster(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    /**
     * Broadcast full simulation state message to all WebSocket clients.
     * Message format: { type: "STATE_UPDATE", payload: { vehicles, timestamp, vehicleCount, avgSpeed } }
     */
    public void broadcast(Map<String, Object> message) {
        messaging.convertAndSend("/topic/simulation", (Object) message);
    }
}