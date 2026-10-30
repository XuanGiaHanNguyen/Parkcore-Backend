package com.parkcore.backend.simulation.logic;

import com.parkcore.backend.model.Vehicle;
import com.parkcore.backend.model.Waypoint;
import java.util.List;

/**
 * Keeps vehicles from driving into each other.
 *
 * A vehicle only yields to traffic that is in front of it along its direction
 * of travel (not beside or behind it), so two cars passing in opposite lanes
 * never stop each other. When two vehicles block each other (crossing paths),
 * the one that spawned first gets right of way.
 */
public class CollisionDetector {
    private static final double GAP = 8.0;          // bumper-to-bumper spacing in px
    private static final double LATERAL_SLACK = 3.0;

    /** Unit vector of the direction a vehicle is currently travelling, or null if it has nowhere to go. */
    public static double[] travelDirection(Vehicle v) {
        Waypoint wp = v.currentWaypoint();
        if (wp == null) return null;
        double dx = wp.x() - v.getX();
        double dy = wp.y() - v.getY();
        double d = Math.hypot(dx, dy);
        if (d < 1e-6) return null;
        return new double[] { dx / d, dy / d };
    }

    /** True when {@code other} sits in the path directly ahead of {@code v}. */
    public static boolean isInPath(Vehicle v, Vehicle other) {
        double[] dir = travelDirection(v);
        if (dir == null) return false;
        double dx = other.getX() - v.getX();
        double dy = other.getY() - v.getY();
        double along = dx * dir[0] + dy * dir[1];
        if (along <= 0) return false;
        double lateral = Math.abs(dx * dir[1] - dy * dir[0]);
        double reach = (v.getLength() + other.getLength()) / 2 + GAP;
        double halfWidths = (v.getWidth() + other.getWidth()) / 2 + LATERAL_SLACK;
        return along < reach && lateral < halfWidths;
    }

    /** True if {@code v} must wait for some other vehicle this tick. */
    public static boolean mustYield(Vehicle v, List<Vehicle> all) {
        if (v.getGhostTime() > 0) return false;
        for (Vehicle other : all) {
            if (other == v || other.isParked()) continue;
            if (!isInPath(v, other)) continue;
            // Mutual block (e.g. crossing paths): older vehicle goes first
            if (isInPath(other, v) && v.getSeq() < other.getSeq()) continue;
            return true;
        }
        return false;
    }

    /** True if no moving vehicle is within {@code radius} px of the point. */
    public static boolean isAreaClear(double x, double y, double radius, Vehicle self, List<Vehicle> all) {
        for (Vehicle other : all) {
            if (other == self || other.isParked()) continue;
            if (Math.hypot(other.getX() - x, other.getY() - y) < radius) return false;
        }
        return true;
    }
}
