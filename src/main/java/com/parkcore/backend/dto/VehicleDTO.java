package com.parkcore.backend.dto;

public class VehicleDTO {
    public String id;
    public String type;   // must match: "BLUE_CAR", "POLICE", etc.
    public double x;
    public double y;
    public double angle;
    public double speed;
    public String state;  // "MOVING", "PARKED", "STOPPED", etc.
    public String spotId;
}