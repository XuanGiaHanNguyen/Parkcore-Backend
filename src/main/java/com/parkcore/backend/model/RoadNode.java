package com.parkcore.backend.model;

/**
 * Represents a waypoint in the parking lot road network.
 * Vehicles navigate by following connected nodes.
 */
public class RoadNode {
    private String id;
    private double x;
    private double y;
    private String type; // "entrance", "aisle", "exit", "intersection", "parking"

    public RoadNode(String id, double x, double y, String type) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.type = type;
    }

    // Getters
    public String getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public String getType() { return type; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setType(String type) { this.type = type; }

    @Override
    public String toString() {
        return "RoadNode{" +
                "id='" + id + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoadNode roadNode = (RoadNode) o;
        return id != null && id.equals(roadNode.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
