package com.parkcore.backend.broadcast;

import com.parkcore.backend.dto.SimulationStateDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SimulationBroadcaster {

    private final SimpMessagingTemplate messaging;

    public SimulationBroadcaster(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    /**
     * Broadcast full simulation state to all WebSocket clients
     */
    public void broadcast(SimulationStateDTO state) {

        messaging.convertAndSend(
            "/topic/simulation",
            (Object) state
        );
    }
}