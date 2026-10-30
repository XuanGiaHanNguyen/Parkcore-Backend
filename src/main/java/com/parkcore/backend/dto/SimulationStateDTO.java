package com.parkcore.backend.dto;

import java.util.List;

// Data structure for the simulation state sent to frontend
public class SimulationStateDTO {
    public List<VehicleDTO> vehicles;
    public long timestamp;
    public int vehicleCount;
    public double avgSpeed;
    public int parkedCount;
    public int totalSpots;
    public int spotsLeft;    // stalls not occupied or reserved
    public int servedCount;  // vehicles that parked and have left
    public boolean running;
    public String speed;     // "slow" | "normal" | "fast"
    public String density;   // "sparse" | "normal" | "heavy"
}
