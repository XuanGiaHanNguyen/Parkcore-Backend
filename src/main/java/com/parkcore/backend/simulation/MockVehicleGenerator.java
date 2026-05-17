package com.parkcore.backend.simulation;
import com.parkcore.backend.model.Vehicle;
import java.util.*;

// generate code that mock data for vehicles, to be used in testing and development before the real simulation logic is implemented
public class MockVehicleGenerator {
    private List<Vehicle> vehicles = new ArrayList<>();
    private static final String[] TYPES = {
        "BLUE_CAR", "RED_CAR", "GREEN_CAR", "WHITE_CAR", 
        "YELLOW_CAR", "TAXI", "VAN", "SEMI_TRUCK", "POLICE"
    };

    public MockVehicleGenerator() {
        createMockVehicles();
    }

    private void createMockVehicles() {
        // Spawn 10 vehicles at entrance road
        for (int i = 0; i < 10; i++) {
            Vehicle v = new Vehicle(
                "vehicle-" + i,
                TYPES[i % TYPES.length],
                250 + i * 20,    // x: spread across entrance
                100,              // y: entrance road height
                0,                // angle: facing right
                25.0,             // speed: 25 units/sec
                "MOVING",
                null
            );
            vehicles.add(v);
        }
    }

    public List<Vehicle> update() {
        // Move each vehicle right by 2 pixels per update
        for (Vehicle v : vehicles) {
            v.setX(v.getX() + 2);
            // Wraparound: if off screen, reset to start
            if (v.getX() > 1000) {
                v.setX(250);
            }
        }
        return vehicles;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }
}