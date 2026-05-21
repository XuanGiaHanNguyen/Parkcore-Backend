package com.parkcore.backend.simulation;

import com.parkcore.backend.model.Vehicle;
import com.parkcore.backend.broadcast.SimulationBroadcaster;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class SimulationEngine {
    private MockVehicleGenerator generator;
    private SimulationBroadcaster broadcaster;
    private boolean running = false;

    public SimulationEngine(SimulationBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
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
                    
                    broadcaster.broadcast(message);
                    
                    // Sleep 50ms before next tick
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
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