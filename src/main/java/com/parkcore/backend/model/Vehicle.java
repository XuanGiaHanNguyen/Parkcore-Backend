package com.parkcore.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

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

}
