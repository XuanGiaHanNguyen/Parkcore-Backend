package com.parkcore.backend.simulation;

import com.parkcore.backend.model.Vehicle;
import com.parkcore.backend.websocket.RawWebSocketHandler;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class SimulationEngine {
    private MockVehicleGenerator generator;
    private RawWebSocketHandler wsHandler;
    private boolean running = false;

    public SimulationEngine(RawWebSocketHandler wsHandler) {
        this.wsHandler = wsHandler;
        this.generator = new MockVehicleGenerator();
    }

    public void start() {
        if (running) return;
        running = true;

        // Run simulation loop every 50ms
        Thread simulationThread = new Thread(() -> {
            while (running) {
                try {
                    // 1. Update all vehicles
                    List<Vehicle> vehicles = generator.update();
                    
                    // 2. Calculate stats
                    double totalSpeed = 0;
                    for (Vehicle v : vehicles) {
                        totalSpeed += v.getSpeed();
                    }
                    double avgSpeed = vehicles.isEmpty() ? 0 : totalSpeed / vehicles.size();
                    
                    // 3. Broadcast to all connected clients
                    Map<String, Object> payload = Map.of(
                        "vehicles", vehicles,
                        "timestamp", System.currentTimeMillis(),
                        "vehicleCount", vehicles.size(),
                        "avgSpeed", avgSpeed
                    );

                    Map<String, Object> message = Map.of(
                        "type", "STATE_UPDATE",
                        "payload", payload
                    );
                    
                    wsHandler.broadcastToAll(message);
                    
                    // Sleep 50ms before next tick
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in simulation loop: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        simulationThread.setDaemon(true);
        simulationThread.start();
    }

    public void stop() {
        running = false;
    }
}