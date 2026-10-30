package com.parkcore.backend.model;

/**
 * A point a vehicle drives towards. When {@code reverse} is set the vehicle
 * backs up to it (nose pointing away from the direction of travel).
 * {@code parks} marks the final point inside a stall.
 */
public record Waypoint(double x, double y, boolean reverse, boolean parks) {

    public static Waypoint drive(double x, double y) {
        return new Waypoint(x, y, false, false);
    }

    public static Waypoint back(double x, double y) {
        return new Waypoint(x, y, true, false);
    }

    public static Waypoint park(double x, double y) {
        return new Waypoint(x, y, false, true);
    }
}
