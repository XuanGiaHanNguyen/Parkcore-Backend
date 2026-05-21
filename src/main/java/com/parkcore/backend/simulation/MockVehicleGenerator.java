package com.parkcore.backend.simulation;
import com.parkcore.backend.model.*;
import com.parkcore.backend.simulation.logic.PathFinder;
import com.parkcore.backend.simulation.logic.CollisionDetector;
import java.util.*;

/**
 * Generates test vehicles that navigate the parking lot using pathfinding.
 * Includes collision avoidance logic.
 */
public class MockVehicleGenerator {
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<VehicleWithPath> vehiclesWithPaths = new ArrayList<>();
    private static final String[] TYPES = {
        "BLUE_CAR", "RED_CAR", "GREEN_CAR", "WHITE_CAR", 
        "YELLOW_CAR", "TAXI", "VAN", "SEMI_TRUCK", "POLICE"
    };
    
    private ParkingLotLayout layout;
    private PathFinder pathFinder;

    public MockVehicleGenerator() {
        this.layout = new ParkingLotLayout();
        this.pathFinder = new PathFinder(layout);
        createMockVehicles();
    }

    private void createMockVehicles() {
        // Spawn 10 vehicles at entrance
        for (int i = 0; i < 10; i++) {
            RoadNode startNode = i < 5 ? layout.getEntranceTop() : layout.getEntranceBottom();
            RoadNode targetNode = i % 2 == 0 ? layout.getExitTop() : layout.getExitBottom();
            
            Vehicle v = new Vehicle(
                "vehicle-" + i,
                TYPES[i % TYPES.length],
                startNode.getX(),
                startNode.getY(),
                0.0,
                25.0,
                "MOVING",
                null
            );
            
            vehicles.add(v);
            
            // Create path for this vehicle
            List<RoadNode> path = pathFinder.findPath(startNode, targetNode);
            VehicleWithPath vwp = new VehicleWithPath(v, path, 0);
            vehiclesWithPaths.add(vwp);
        }
    }

    public List<Vehicle> update() {
        List<Vehicle> updatedVehicles = new ArrayList<>();
        
        // Move each vehicle along its path with collision avoidance
        for (int i = 0; i < vehiclesWithPaths.size(); i++) {
            VehicleWithPath vwp = vehiclesWithPaths.get(i);
            Vehicle v = vwp.vehicle;
            
            if (vwp.pathIndex >= vwp.path.size()) {
                // Path complete, reset or exit
                vwp.pathIndex = 0;
                updatedVehicles.add(v);
                continue;
            }
            
            // Get current target waypoint
            RoadNode targetNode = vwp.path.get(vwp.pathIndex);
            double targetX = targetNode.getX();
            double targetY = targetNode.getY();
            
            // Calculate direction to target
            double dx = targetX - v.getX();
            double dy = targetY - v.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            // If reached waypoint, move to next
            if (distance < 5.0) {
                vwp.pathIndex++;
                if (vwp.pathIndex >= vwp.path.size()) {
                    updatedVehicles.add(v);
                    continue;
                }
                targetNode = vwp.path.get(vwp.pathIndex);
                targetX = targetNode.getX();
                targetY = targetNode.getY();
                dx = targetX - v.getX();
                dy = targetY - v.getY();
                distance = Math.sqrt(dx * dx + dy * dy);
            }
            
            Vehicle newV = v;
            
            if (distance > 0.1) {
                // Check for collisions
                boolean collision = CollisionDetector.hasCollisionWithAny(v, vehicles);
                
                double moveDistance = collision ? 0.0 : 2.0; // Stop if collision imminent
                double moveX = (dx / distance) * moveDistance;
                double moveY = (dy / distance) * moveDistance;
                
                newV = new Vehicle(
                    v.getId(),
                    v.getType(),
                    v.getX() + moveX,
                    v.getY() + moveY,
                    Math.atan2(dy, dx) * 180.0 / Math.PI,
                    v.getSpeed(),
                    collision ? "STOPPED" : "MOVING",
                    v.getParkingSpotId()
                );
            }
            
            updatedVehicles.add(newV);
            vwp.vehicle = newV;
        }
        
        vehicles = updatedVehicles;
        return vehicles;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    /**
     * Helper class to track vehicle path progress
     */
    private static class VehicleWithPath {
        Vehicle vehicle;
        List<RoadNode> path;
        int pathIndex;

        VehicleWithPath(Vehicle vehicle, List<RoadNode> path, int pathIndex) {
            this.vehicle = vehicle;
            this.path = path;
            this.pathIndex = pathIndex;
        }
    }
}