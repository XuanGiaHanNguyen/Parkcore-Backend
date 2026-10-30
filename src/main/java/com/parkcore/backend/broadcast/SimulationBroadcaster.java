package com.parkcore.backend.broadcast;

import com.parkcore.backend.dto.SimulationStateDTO;
import com.parkcore.backend.websocket.RawWebSocketHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Wraps simulation state in the envelope the frontend expects
 * ({ type: "STATE_UPDATE", payload }) and sends it to every client.
 */
@Component
public class SimulationBroadcaster {
    private final RawWebSocketHandler wsHandler;

    public SimulationBroadcaster(RawWebSocketHandler wsHandler) {
        this.wsHandler = wsHandler;
    }

    public void broadcast(SimulationStateDTO state) {
        try {
            wsHandler.broadcastToAll(Map.of("type", "STATE_UPDATE", "payload", state));
        } catch (Exception e) {
            System.err.println("✗ Broadcast failed: " + e.getMessage());
        }
    }
}
