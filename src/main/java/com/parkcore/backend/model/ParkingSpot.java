package com.parkcore.backend.model;

/**
 * A single parking stall. Coordinates are canvas pixels (center of the stall).
 */
public class ParkingSpot {
    private final String id;
    private final double x;          // stall center x
    private final double y;          // stall center y
    private final double edgeY;      // y of the stall's open edge (where cars enter from the aisle)
    private final double laneY;      // y of the westbound lane cars park from
    private final int aisle;         // index of the aisle this stall opens onto
    private final boolean handicapped;

    private String vehicleId;        // reserved/occupied by this vehicle, or null

    public ParkingSpot(String id, double x, double y, double edgeY, double laneY, int aisle, boolean handicapped) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.edgeY = edgeY;
        this.laneY = laneY;
        this.aisle = aisle;
        this.handicapped = handicapped;
    }

    public String getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getEdgeY() { return edgeY; }
    public double getLaneY() { return laneY; }
    public int getAisle() { return aisle; }
    public boolean isHandicapped() { return handicapped; }

    public boolean isFree() { return vehicleId == null; }
    public String getVehicleId() { return vehicleId; }
    public void reserve(String vehicleId) { this.vehicleId = vehicleId; }
    public void release() { this.vehicleId = null; }

    /** Heading (degrees) of a car parked nose-in: -90 = up, 90 = down. */
    public double getParkedAngle() {
        return y < edgeY ? -90.0 : 90.0;
    }
}
