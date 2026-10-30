package com.parkcore.backend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Parking lot geometry and the routes vehicles follow through it.
 *
 * Every constant here mirrors frontend/src/app/simulation/roadsystem/parkinglot.tsx
 * (canvas 1000 x 710), so backend coordinates land exactly on the drawn stalls.
 *
 *   X 0-100    store
 *   X 100-118  sidewalk
 *   X 118-218  pedestrian road (used for U-turns at the west end of each aisle)
 *   X 218-900  lot: 6 rows of stalls with 3 horizontal aisles between them
 *   X 900-1000 entrance road: IN lane (northbound) at x=925, OUT lane (southbound) at x=975
 *
 * Each aisle has two lanes: westbound (south half) for finding a stall,
 * eastbound (north half) for leaving.
 */
public class ParkingLot {
    public static final double CANVAS_W = 1000;
    public static final double CANVAS_H = 710;

    private static final double STORE_W = 100;
    private static final double SIDEWALK_W = 18;
    private static final double PEDROAD_W = 100;
    private static final double ROAD_W = 100;
    private static final double SPOT_W = 28;
    private static final double SPOT_H = 52;
    private static final double AISLE_W = 52;
    private static final double CENTER_GAP = 8;
    private static final double ENT_H = 65;
    private static final int HC_COUNT = 3;
    private static final int NUM_PAIRS = 2;

    public static final double LOT_X = STORE_W + SIDEWALK_W + PEDROAD_W;       // 218
    public static final double ROAD_X = CANVAS_W - ROAD_W;                      // 900
    public static final double IN_X = ROAD_X + ROAD_W * 0.25;                   // 925
    public static final double OUT_X = ROAD_X + ROAD_W * 0.75;                  // 975
    private static final double LANE_OFFSET = 13;

    public static final double SPAWN_X = IN_X;
    public static final double SPAWN_Y = CANVAS_H + 40;
    private static final double DESPAWN_Y = CANVAS_H + 50;

    private final List<ParkingSpot> spots = new ArrayList<>();
    private final List<Double> aisleCenters = new ArrayList<>();

    public ParkingLot() {
        build();
    }

    private void build() {
        double pairH = SPOT_H * 2 + CENTER_GAP;
        double totalRowsH = NUM_PAIRS * pairH + (NUM_PAIRS - 1) * AISLE_W;
        double rowTop = Math.floor((CANVAS_H - totalRowsH) / 2);
        double lotW = ROAD_X - LOT_X;
        double rowStartX = LOT_X + 14;
        double rowEndX = LOT_X + lotW - 14;
        int numSpots = (int) Math.floor((rowEndX - rowStartX) / SPOT_W) + 1;

        // Row tops, top to bottom (same math as drawExtraParkingRows / drawParkingRows)
        List<Double> rowTops = new ArrayList<>();
        rowTops.add(ENT_H + 4);
        for (int pair = 0; pair < NUM_PAIRS; pair++) {
            double topY = rowTop + pair * (pairH + AISLE_W + 26) - 15;
            rowTops.add(topY);
            rowTops.add(topY + pairH - SPOT_H);
        }
        rowTops.add(CANVAS_H - ENT_H - 4 - SPOT_H);

        // Aisles sit between rows 0-1, 2-3, 4-5
        for (int r = 0; r < rowTops.size(); r += 2) {
            double above = rowTops.get(r) + SPOT_H;
            double below = rowTops.get(r + 1);
            aisleCenters.add((above + below) / 2);
        }

        for (int r = 0; r < rowTops.size(); r++) {
            int aisle = r / 2;
            boolean opensDown = r % 2 == 0; // even rows sit above their aisle
            double top = rowTops.get(r);
            double edgeY = opensDown ? top + SPOT_H : top;
            boolean pairRow = r != 0 && r != rowTops.size() - 1; // HC stalls only in the middle rows
            for (int i = 0; i < numSpots; i++) {
                double cx = rowStartX - 5 + i * SPOT_W + SPOT_W / 2;
                spots.add(new ParkingSpot(
                        "R" + r + "-" + i,
                        cx,
                        top + SPOT_H / 2,
                        edgeY,
                        westLane(aisle),
                        aisle,
                        pairRow && i < HC_COUNT));
            }
        }
    }

    public List<ParkingSpot> getSpots() { return spots; }
    public int getAisleCount() { return aisleCenters.size(); }

    public double westLane(int aisle) { return aisleCenters.get(aisle) + LANE_OFFSET; }
    public double eastLane(int aisle) { return aisleCenters.get(aisle) - LANE_OFFSET; }

    // ─── Routes ──────────────────────────────────────────────────────────────

    /** From the spawn point up the IN lane and into the westbound lane of an aisle. */
    private void enterAisle(List<Waypoint> route, int aisle) {
        double ly = westLane(aisle);
        route.add(Waypoint.drive(IN_X, ly + 18));
        route.add(Waypoint.drive(IN_X - 18, ly));
    }

    /** From the westbound lane: U-turn on the pedestrian road, drive east, leave via the OUT lane. */
    private void leaveFromAisle(List<Waypoint> route, int aisle) {
        double wy = westLane(aisle);
        double ey = eastLane(aisle);
        double mid = (wy + ey) / 2;
        route.add(Waypoint.drive(LOT_X - 4, wy));
        route.add(Waypoint.drive(LOT_X - 30, wy - 6));
        route.add(Waypoint.drive(LOT_X - 38, mid));
        route.add(Waypoint.drive(LOT_X - 30, ey + 6));
        route.add(Waypoint.drive(LOT_X - 4, ey));
        route.add(Waypoint.drive(OUT_X - 18, ey));
        route.add(Waypoint.drive(OUT_X, ey + 18));
        route.add(Waypoint.drive(OUT_X, DESPAWN_Y));
    }

    /** Spawn → aisle → nose-in to the stall. */
    public List<Waypoint> routeToSpot(ParkingSpot spot) {
        List<Waypoint> route = new ArrayList<>();
        enterAisle(route, spot.getAisle());
        double ly = spot.getLaneY();
        route.add(Waypoint.drive(spot.getX() + 34, ly));
        route.add(Waypoint.drive(spot.getX() + 10, (ly + spot.getEdgeY()) / 2));
        route.add(Waypoint.drive(spot.getX(), spot.getEdgeY()));
        route.add(Waypoint.park(spot.getX(), spot.getY()));
        return route;
    }

    /** Back out of the stall into the westbound lane, then leave the lot. */
    public List<Waypoint> routeOutOfSpot(ParkingSpot spot) {
        List<Waypoint> route = new ArrayList<>();
        double ly = spot.getLaneY();
        route.add(Waypoint.back(spot.getX(), spot.getEdgeY()));
        route.add(Waypoint.back(spot.getX() + 24, ly));
        leaveFromAisle(route, spot.getAisle());
        return route;
    }

    /** Drive through an aisle without parking (patrols, or when the lot is full). */
    public List<Waypoint> routeThrough(int aisle) {
        List<Waypoint> route = new ArrayList<>();
        enterAisle(route, aisle);
        leaveFromAisle(route, aisle);
        return route;
    }

    public static boolean isOffscreen(double y) {
        return y >= DESPAWN_Y - 1;
    }
}
