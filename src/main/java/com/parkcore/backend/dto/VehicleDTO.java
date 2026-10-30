package com.parkcore.backend.dto;

import com.parkcore.backend.model.Vehicle;

public class VehicleDTO {
    public String id;
    public String type;   // must match: "BLUE_CAR", "POLICE", etc.
    public double x;
    public double y;
    public double angle;
    public double speed;
    public String state;  // "MOVING", "PARKED", "STOPPED", etc.
    public String spotId;

    public static VehicleDTO from(Vehicle v) {
        VehicleDTO dto = new VehicleDTO();
        dto.id = v.getId();
        dto.type = v.getType();
        dto.x = Math.round(v.getX() * 10) / 10.0;
        dto.y = Math.round(v.getY() * 10) / 10.0;
        dto.angle = Math.round(v.getAngle() * 10) / 10.0;
        dto.speed = Math.round(v.getSpeed() * 10) / 10.0;
        dto.state = v.getState();
        dto.spotId = v.getSpotId();
        return dto;
    }
}
