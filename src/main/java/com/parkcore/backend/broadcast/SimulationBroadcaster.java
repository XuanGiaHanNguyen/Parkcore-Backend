package com.parkcore.backend.broadcast;
import com.parkcore.backend.dto.VehicleDTO;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.EnableScheduling;


import java.util.List;
import java.util.Map;

@Service
@EnableScheduling
public class SimulationBroadcaster {

    private final SimpMessagingTemplate messaging;
    private double x = 350;

    public SimulationBroadcaster(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    @Scheduled(fixedRate = 100)
    public void broadcast() {
        x += 1; // move vehicle slowly to the right
        if (x > 900) x = 350;

        VehicleDTO v = new VehicleDTO();
        v.id = "v1"; v.type = "BLUE_CAR";
        v.x = x; v.y = 300;
        v.angle = 0; v.speed = 10;
        v.state = "MOVING";

        Map<String, Object> payload = Map.of(
            "vehicles", List.of(v),
            "timestamp", System.currentTimeMillis(),
            "vehicleCount", 1,
            "avgSpeed", 10.0
        );

        Map<String, Object> message = Map.of(
            "type", "STATE_UPDATE",
            "payload", payload
        );

        messaging.convertAndSend("/topic/simulation", (Object) message);
    }
}