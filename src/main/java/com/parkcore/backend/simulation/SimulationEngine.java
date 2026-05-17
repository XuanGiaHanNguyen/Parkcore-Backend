package com.parkcore.backend.simulation;

import com.parkcore.backend.dto.SimulationStateDTO;
import com.parkcore.backend.model.Vehicle;
import com.parkcore.backend.broadcast.SimulationBroadcaster;
import org.springframework.stereotype.Service;
import java.util.List;

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
                    double avgSpeed = vehicles.stream()
                        .mapToDouble(Vehicle::getSpeed)
                        .average()
                        .orElse(0.0);
                    
                    // 3. Create state DTO
                    SimulationStateDTO state = new SimulationStateDTO(
                        vehicles,
                        System.currentTimeMillis(),
                        vehicles.size(),
                        avgSpeed
                    );
                    
                    // 4. Broadcast to all connected clients
                    broadcaster.broadcast(state);
                    
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

    public SimulationStateDTO getState() {
        List<Vehicle> vehicles = generator.getVehicles();
        double avgSpeed = vehicles.stream()
            .mapToDouble(Vehicle::getSpeed)
            .average()
            .orElse(0.0);
        return new SimulationStateDTO(
            vehicles,
            System.currentTimeMillis(),
            vehicles.size(),
            avgSpeed
        );
    }
}