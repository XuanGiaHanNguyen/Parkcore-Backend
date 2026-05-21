package com.parkcore.backend.simulation.logic;

import com.parkcore.backend.model.Road;
import com.parkcore.backend.model.RoadNode;
import com.parkcore.backend.model.ParkingLotLayout;

import java.util.*;

/**
 * A* pathfinding algorithm for finding optimal routes through parking lot.
 */
public class PathFinder {
    private ParkingLotLayout layout;

    public PathFinder(ParkingLotLayout layout) {
        this.layout = layout;
    }

    /**
     * Find path from start node to end node using A* algorithm.
     * Returns list of nodes to visit in order.
     */
    public List<RoadNode> findPath(RoadNode start, RoadNode end) {
        if (start == null || end == null) {
            return new ArrayList<>();
        }

        Set<RoadNode> openSet = new HashSet<>();
        Set<RoadNode> closedSet = new HashSet<>();
        Map<RoadNode, RoadNode> cameFrom = new HashMap<>();
        Map<RoadNode, Double> gScore = new HashMap<>(); // cost from start
        Map<RoadNode, Double> fScore = new HashMap<>(); // g + heuristic

        openSet.add(start);
        gScore.put(start, 0.0);
        fScore.put(start, heuristic(start, end));

        while (!openSet.isEmpty()) {
            RoadNode current = getNodeWithLowestFScore(openSet, fScore);

            if (current.equals(end)) {
                return reconstructPath(cameFrom, current);
            }

            openSet.remove(current);
            closedSet.add(current);

            // Check all neighbors
            for (Road road : layout.getRoadsFrom(current)) {
                RoadNode neighbor = road.getToNode();

                if (closedSet.contains(neighbor)) continue;

                double tentativeGScore = gScore.getOrDefault(current, Double.MAX_VALUE) + road.getDistance();

                if (!openSet.contains(neighbor)) {
                    openSet.add(neighbor);
                } else if (tentativeGScore >= gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    continue;
                }

                cameFrom.put(neighbor, current);
                gScore.put(neighbor, tentativeGScore);
                fScore.put(neighbor, tentativeGScore + heuristic(neighbor, end));
            }
        }

        // No path found, return empty list
        return new ArrayList<>();
    }

    /**
     * Heuristic function: straight-line distance to goal
     */
    private double heuristic(RoadNode node, RoadNode goal) {
        double dx = node.getX() - goal.getX();
        double dy = node.getY() - goal.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private RoadNode getNodeWithLowestFScore(Set<RoadNode> openSet, Map<RoadNode, Double> fScore) {
        RoadNode lowest = null;
        double lowestScore = Double.MAX_VALUE;

        for (RoadNode node : openSet) {
            double score = fScore.getOrDefault(node, Double.MAX_VALUE);
            if (score < lowestScore) {
                lowestScore = score;
                lowest = node;
            }
        }

        return lowest;
    }

    private List<RoadNode> reconstructPath(Map<RoadNode, RoadNode> cameFrom, RoadNode current) {
        List<RoadNode> path = new ArrayList<>();
        path.add(current);

        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(0, current);
        }

        return path;
    }
}

