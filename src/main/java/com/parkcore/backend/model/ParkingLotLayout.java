package com.parkcore.backend.model;

import java.util.*;

/**
 * Defines the parking lot layout as a directed graph of roads and nodes.
 * Used for pathfinding and vehicle navigation.
 */
public class ParkingLotLayout {
    private Map<String, RoadNode> nodes = new HashMap<>();
    private List<Road> roads = new ArrayList<>();
    
    // Reference nodes for easier pathfinding
    private RoadNode entranceTop;
    private RoadNode entranceBottom;
    private RoadNode exitTop;
    private RoadNode exitBottom;

    public ParkingLotLayout() {
        buildLayout();
    }

    private void buildLayout() {
        // Create entrance nodes
        entranceTop = addNode("entrance_top", 350.0, 50.0, "entrance");
        entranceBottom = addNode("entrance_bottom", 350.0, 650.0, "entrance");

        // Create exit nodes
        exitTop = addNode("exit_top", 850.0, 50.0, "exit");
        exitBottom = addNode("exit_bottom", 850.0, 650.0, "exit");

        // Create main intersection nodes (where vehicles turn)
        RoadNode intersect1Top = addNode("intersect_1_top", 350.0, 150.0, "intersection");
        RoadNode intersect1Bot = addNode("intersect_1_bot", 350.0, 600.0, "intersection");
        
        // Create aisle nodes - each aisle has left and right ends
        RoadNode aisle1Left = addNode("aisle_1_left", 350.0, 200.0, "aisle");
        RoadNode aisle1Right = addNode("aisle_1_right", 850.0, 200.0, "aisle");
        
        RoadNode aisle2Left = addNode("aisle_2_left", 350.0, 350.0, "aisle");
        RoadNode aisle2Right = addNode("aisle_2_right", 850.0, 350.0, "aisle");
        
        RoadNode aisle3Left = addNode("aisle_3_left", 350.0, 500.0, "aisle");
        RoadNode aisle3Right = addNode("aisle_3_right", 850.0, 500.0, "aisle");

        // Connect entrance to aisles
        addRoad("entrance_top_to_aisle1", entranceTop, aisle1Left, false);
        addRoad("entrance_bot_to_aisle3", entranceBottom, aisle3Left, false);

        // Connect aisles (horizontal roads)
        addRoad("aisle1_lr", aisle1Left, aisle1Right, true);
        addRoad("aisle2_lr", aisle2Left, aisle2Right, true);
        addRoad("aisle3_lr", aisle3Left, aisle3Right, true);

        // Connect aisles vertically (allow routing up/down)
        addRoad("aisle_1_to_2", aisle1Left, aisle2Left, true);
        addRoad("aisle_2_to_3", aisle2Left, aisle3Left, true);
        addRoad("aisle_1_to_2_right", aisle1Right, aisle2Right, true);
        addRoad("aisle_2_to_3_right", aisle2Right, aisle3Right, true);

        // Connect aisles to exit
        addRoad("aisle3_to_exit_top", aisle1Right, exitTop, false);
        addRoad("aisle3_to_exit_bot", aisle3Right, exitBottom, false);
    }

    private RoadNode addNode(String id, double x, double y, String type) {
        RoadNode node = new RoadNode(id, x, y, type);
        nodes.put(id, node);
        return node;
    }

    private void addRoad(String id, RoadNode from, RoadNode to, boolean bidirectional) {
        Road road = new Road(id, from, to, bidirectional);
        roads.add(road);
        
        if (bidirectional) {
            Road reverseRoad = new Road(id + "_reverse", to, from, false);
            roads.add(reverseRoad);
        }
    }

    // Getters
    public RoadNode getNode(String id) {
        return nodes.get(id);
    }

    public Collection<RoadNode> getAllNodes() {
        return nodes.values();
    }

    public List<Road> getRoadsFrom(RoadNode node) {
        List<Road> result = new ArrayList<>();
        for (Road road : roads) {
            if (road.getFromNode().equals(node)) {
                result.add(road);
            }
        }
        return result;
    }

    public List<Road> getAllRoads() {
        return roads;
    }

    public RoadNode getEntranceTop() { return entranceTop; }
    public RoadNode getEntranceBottom() { return entranceBottom; }
    public RoadNode getExitTop() { return exitTop; }
    public RoadNode getExitBottom() { return exitBottom; }

    @Override
    public String toString() {
        return "ParkingLotLayout{" +
                "nodes=" + nodes.size() +
                ", roads=" + roads.size() +
                '}';
    }
}
