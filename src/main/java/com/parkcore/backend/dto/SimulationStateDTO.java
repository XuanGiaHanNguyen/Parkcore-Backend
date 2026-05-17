package com.parkcore.backend.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.parkcore.backend.model.Vehicle;
import java.util.List;

// Data structure for the simulation state sent to frontend
@Data
@AllArgsConstructor
public class SimulationStateDTO {
    private List<Vehicle> vehicles;
    private long timestamp;
    private int vehicleCount;
    private double avgSpeed;
}
