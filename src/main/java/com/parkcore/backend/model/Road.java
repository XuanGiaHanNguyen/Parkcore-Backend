package com.parkcore.backend.model;

/**
 * Represents a connection between two RoadNodes.
 * Vehicles move along roads to navigate the parking lot.
 */
public class Road {
    private String id;
    private RoadNode fromNode;
    private RoadNode toNode;
    private double distance;
    private double width;
    private boolean bidirectional;

    public Road(String id, RoadNode fromNode, RoadNode toNode, boolean bidirectional) {
        this.id = id;
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.bidirectional = bidirectional;
        this.distance = calculateDistance();
        this.width = 50.0; // default width
    }

    private double calculateDistance() {
        double dx = toNode.getX() - fromNode.getX();
        double dy = toNode.getY() - fromNode.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Getters
    public String getId() { return id; }
    public RoadNode getFromNode() { return fromNode; }
    public RoadNode getToNode() { return toNode; }
    public double getDistance() { return distance; }
    public double getWidth() { return width; }
    public boolean isBidirectional() { return bidirectional; }

    // Setters
    public void setWidth(double width) { this.width = width; }

    @Override
    public String toString() {
        return "Road{" +
                "id='" + id + '\'' +
                ", from=" + fromNode.getId() +
                ", to=" + toNode.getId() +
                ", distance=" + distance +
                ", bidirectional=" + bidirectional +
                '}';
    }
}
