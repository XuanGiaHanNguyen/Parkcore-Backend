package com.parkcore.backend.model;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

// Data structure for a vehicle and its progress through the lot
public class Vehicle {
    private final String id;
    private final String type;      // type of car, matches frontend VehicleType
    private final long seq;         // spawn order, older vehicles get right of way
    private final double length;    // canvas px, nose to tail
    private final double width;     // canvas px
    private final double maxSpeed;  // px per second
    private final boolean handicapPermit;

    private double x;               // canvas x pixel coordinate (center)
    private double y;               // canvas y pixel coordinate (center)
    private double angle;           // degrees the nose points: 0 = right, 90 = down
    private double speed;           // current px per second
    private String state = "MOVING"; // MOVING, STOPPED, PARKING, PARKED, EXITING

    private final Deque<Waypoint> route = new ArrayDeque<>();
    private ParkingSpot spot;       // reserved/occupied stall, null for drive-through
    private double dwellRemaining;  // seconds left parked
    private double blockedTime;     // seconds spent waiting on traffic
    private double ghostTime;       // seconds collision checks are skipped (deadlock breaker)

    public Vehicle(String id, String type, long seq, double length, double width,
                   double maxSpeed, boolean handicapPermit, double x, double y, double angle) {
        this.id = id;
        this.type = type;
        this.seq = seq;
        this.length = length;
        this.width = width;
        this.maxSpeed = maxSpeed;
        this.handicapPermit = handicapPermit;
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public long getSeq() { return seq; }
    public double getLength() { return length; }
    public double getWidth() { return width; }
    public double getMaxSpeed() { return maxSpeed; }
    public boolean hasHandicapPermit() { return handicapPermit; }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getAngle() { return angle; }
    public double getSpeed() { return speed; }
    public String getState() { return state; }
    public ParkingSpot getSpot() { return spot; }
    public String getSpotId() { return spot == null ? null : spot.getId(); }
    public double getDwellRemaining() { return dwellRemaining; }
    public double getBlockedTime() { return blockedTime; }
    public double getGhostTime() { return ghostTime; }

    public void setPosition(double x, double y) { this.x = x; this.y = y; }
    public void setAngle(double angle) { this.angle = angle; }
    public void setSpeed(double speed) { this.speed = speed; }
    public void setState(String state) { this.state = state; }
    public void setSpot(ParkingSpot spot) { this.spot = spot; }
    public void setDwellRemaining(double dwellRemaining) { this.dwellRemaining = dwellRemaining; }
    public void setBlockedTime(double blockedTime) { this.blockedTime = blockedTime; }
    public void setGhostTime(double ghostTime) { this.ghostTime = ghostTime; }

    public boolean isParked() { return "PARKED".equals(state); }

    // Route
    public Waypoint currentWaypoint() { return route.peekFirst(); }
    public void advanceWaypoint() { route.pollFirst(); }
    public boolean hasRoute() { return !route.isEmpty(); }
    public int routeSize() { return route.size(); }
    public void setRoute(Collection<Waypoint> waypoints) {
        route.clear();
        route.addAll(waypoints);
    }

    @Override
    public String toString() {
        return "Vehicle{id='" + id + "', type='" + type + "', x=" + x + ", y=" + y
                + ", angle=" + angle + ", state='" + state + "', spot=" + getSpotId() + '}';
    }
}
