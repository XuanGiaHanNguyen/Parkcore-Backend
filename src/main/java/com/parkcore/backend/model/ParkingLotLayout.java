package com.parkcore.backend.model;

import java.util.*;

/**
 * Defines the parking lot layout as a directed graph of roads and nodes.
 * Used for pathfinding and vehicle navigation.
 * 
 * Coordinates match the frontend canvas layout:
 * - Canvas default: 550x200
 * - Store: X 0-100
 * - Sidewalk: X 100-118
 * - Pedestrian Road: X 118-218
 * - Lot Area (for vehicles): X 218-518, Y 0-200
 * - Entrance Road: X 518-550
 */
public class ParkingLotLayout {
    private Map<String, RoadNode> nodes = new HashMap<>();
    private List<Road> roads = new ArrayList<>();
    
    // Canvas dimensions for coordinate calculation
    private static final double CANVAS_W = 550.0;
    private static final double CANVAS_H = 200.0;
    
    // Layout constants (match parkinglot.tsx)
    private static final double STORE_W = 100.0;
    private static final double SIDEWALK_W = 18.0;
    private static final double PEDROAD_W = 100.0;
    private static final double ROAD_W = 100.0;
    private static final double ENT_H = 65.0;
    
    // Calculated coordinates
    private static final double LOT_X = STORE_W + SIDEWALK_W + PEDROAD_W; // 218
    private static final double LOT_W = CANVAS_W - LOT_X - ROAD_W;        // 232
    private static final double LOT_Y = 0.0;
    private static final double LOT_H = CANVAS_H;                         // 200
    
    // Reference nodes for easier pathfinding
    private RoadNode entranceTop;
    private RoadNode entranceBottom;
    private RoadNode exitTop;
    private RoadNode exitBottom;

    public ParkingLotLayout() {
        buildLayout();
    }

    private void buildLayout() {
        // Entrance nodes at left side of lot, top and bottom
        entranceTop = addNode("entrance_top", LOT_X + 20.0, LOT_Y + 30.0, "entrance");
        entranceBottom = addNode("entrance_bottom", LOT_X + 20.0, LOT_Y + LOT_H - 30.0, "entrance");

        // Exit nodes at right side of lot, top and bottom
        exitTop = addNode("exit_top", LOT_X + LOT_W - 20.0, LOT_Y + 30.0, "exit");
        exitBottom = addNode("exit_bottom", LOT_X + LOT_W - 20.0, LOT_Y + LOT_H - 30.0, "exit");

        // Create aisle nodes - three rows spread across lot
        // Each aisle has a left (entrance side) and right (exit side) end
        double aisleYSpacing = LOT_H / 4;
        
        RoadNode aisle1Left = addNode("aisle_1_left", LOT_X + 30.0, LOT_Y + aisleYSpacing, "aisle");
        RoadNode aisle1Right = addNode("aisle_1_right", LOT_X + LOT_W - 30.0, LOT_Y + aisleYSpacing, "aisle");
        
        RoadNode aisle2Left = addNode("aisle_2_left", LOT_X + 30.0, LOT_Y + LOT_H / 2, "aisle");
        RoadNode aisle2Right = addNode("aisle_2_right", LOT_X + LOT_W - 30.0, LOT_Y + LOT_H / 2, "aisle");
        
        RoadNode aisle3Left = addNode("aisle_3_left", LOT_X + 30.0, LOT_Y + LOT_H - aisleYSpacing, "aisle");
        RoadNode aisle3Right = addNode("aisle_3_right", LOT_X + LOT_W - 30.0, LOT_Y + LOT_H - aisleYSpacing, "aisle");

        // Connect entrances to aisles
        addRoad("entrance_top_to_aisle1", entranceTop, aisle1Left, false);
        addRoad("entrance_bot_to_aisle3", entranceBottom, aisle3Left, false);

        // Connect aisles horizontally (vehicles travel left-right along aisles)
        addRoad("aisle1_lr", aisle1Left, aisle1Right, true);
        addRoad("aisle2_lr", aisle2Left, aisle2Right, true);
        addRoad("aisle3_lr", aisle3Left, aisle3Right, true);

        // Connect aisles vertically (vehicles can route between aisles)
        addRoad("aisle_1_to_2", aisle1Left, aisle2Left, true);
        addRoad("aisle_2_to_3", aisle2Left, aisle3Left, true);
        addRoad("aisle_1_to_2_right", aisle1Right, aisle2Right, true);
        addRoad("aisle_2_to_3_right", aisle2Right, aisle3Right, true);

        // Connect aisles to exits
        addRoad("aisle1_to_exit_top", aisle1Right, exitTop, false);
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
