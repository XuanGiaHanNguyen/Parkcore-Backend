package com.parkcore.backend.model;

// Data structure for a vehicle
public class Vehicle {
    private String id; 
    private String type; // type of cars 
    private double x; // canvas x pixel coordinate
    private double y; // canvas y pixel coordinate
    private double angle; // degree: 0= right 90=down ...
    private double speed; // pixel per second
    private String state; // "moving", "stopped", "parked"
    private String parkingSpotId; // if parked, the id of the parking spot

    public Vehicle(String id, String type, double x, double y, double angle, double speed, String state, String parkingSpotId) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.speed = speed;
        this.state = state;
        this.parkingSpotId = parkingSpotId;
    }

    public Vehicle() {
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getAngle() {
        return angle;
    }

    public double getSpeed() {
        return speed;
    }

    public String getState() {
        return state;
    }

    public String getParkingSpotId() {
        return parkingSpotId;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setParkingSpotId(String parkingSpotId) {
        this.parkingSpotId = parkingSpotId;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", angle=" + angle +
                ", speed=" + speed +
                ", state='" + state + '\'' +
                ", parkingSpotId='" + parkingSpotId + '\'' +
                '}';
    }
}
