package com.parkcore.backend.simulation.logic;

import com.parkcore.backend.model.RoadNode;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages stop sign behavior at intersections.
 * Multiple vehicles yield appropriately at intersections.
 */
public class StopSignHandler {
    private Set<String> waitingVehicles = new HashSet<>();
    private static final long STOP_DURATION_MS = 500; // how long to wait at stop
    private static final double INTERSECTION_RADIUS = 40.0; // detection radius

    /**
     * Check if vehicle is at an intersection (stop node).
     */
    public boolean isAtIntersection(double x, double y, RoadNode intersectionNode) {
        if (intersectionNode == null) return false;
        
        double dx = x - intersectionNode.getX();
        double dy = y - intersectionNode.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        return distance < INTERSECTION_RADIUS && "intersection".equals(intersectionNode.getType());
    }

    /**
     * Register vehicle as waiting at intersection.
     */
    public void markAsWaiting(String vehicleId) {
        waitingVehicles.add(vehicleId);
    }

    /**
     * Check if vehicle should wait at this intersection.
     */
    public boolean shouldWait(String vehicleId, int totalVehiclesAtIntersection) {
        // Simple rule: if more than 1 vehicle, others must wait
        return totalVehiclesAtIntersection > 1;
    }

    /**
     * Release vehicle from waiting.
     */
    public void releaseVehicle(String vehicleId) {
        waitingVehicles.remove(vehicleId);
    }

    /**
     * Check if vehicle is currently waiting.
     */
    public boolean isWaiting(String vehicleId) {
        return waitingVehicles.contains(vehicleId);
    }

    /**
     * Get count of vehicles waiting.
     */
    public int getWaitingCount() {
        return waitingVehicles.size();
    }
}

