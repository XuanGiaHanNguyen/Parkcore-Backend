package com.parkcore.backend.simulation.logic;

import com.parkcore.backend.model.Vehicle;
import java.util.List;

/**
 * Detects collisions between vehicles and prevents overlaps.
 */
public class CollisionDetector {
    private static final double COLLISION_DISTANCE = 25.0; // vehicle radius

    /**
     * Check if two vehicles would collide at their current positions.
     */
    public static boolean wouldCollide(Vehicle v1, Vehicle v2) {
        double dx = v2.getX() - v1.getX();
        double dy = v2.getY() - v1.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < COLLISION_DISTANCE;
    }

    /**
     * Find nearby vehicles that could cause a collision.
     */
    public static boolean hasCollisionWithAny(Vehicle vehicle, List<Vehicle> otherVehicles) {
        for (Vehicle other : otherVehicles) {
            if (!other.getId().equals(vehicle.getId()) && wouldCollide(vehicle, other)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculate minimum distance to nearest vehicle.
     */
    public static double getMinDistanceToAny(Vehicle vehicle, List<Vehicle> otherVehicles) {
        double minDistance = Double.MAX_VALUE;

        for (Vehicle other : otherVehicles) {
            if (other.getId().equals(vehicle.getId())) continue;

            double dx = other.getX() - vehicle.getX();
            double dy = other.getY() - vehicle.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            minDistance = Math.min(minDistance, distance);
        }

        return minDistance;
    }
}

