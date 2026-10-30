package com.parkcore.backend.simulation;

import com.fasterxml.jackson.databind.JsonNode;
import com.parkcore.backend.broadcast.SimulationBroadcaster;
import com.parkcore.backend.dto.SimulationStateDTO;
import com.parkcore.backend.dto.VehicleDTO;
import com.parkcore.backend.model.ParkingLot;
import com.parkcore.backend.model.ParkingSpot;
import com.parkcore.backend.model.Vehicle;
import com.parkcore.backend.model.Waypoint;
import com.parkcore.backend.simulation.logic.CollisionDetector;
import com.parkcore.backend.websocket.RawWebSocketHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Owns the simulation loop. Every tick it spawns arrivals, moves each vehicle
 * along its route (yielding to traffic ahead), parks and un-parks vehicles,
 * and broadcasts the resulting state.
 *
 * Lifecycle of a vehicle:
 *   MOVING/STOPPED → PARKING (turning into its stall) → PARKED → EXITING → removed off-screen
 * Police cars patrol: they drive through an aisle without parking.
 */
@Service
public class SimulationEngine {
    private static final long TICK_MS = 50;
    private static final double TURN_RATE = 320;           // degrees per second
    private static final double ACCEL = 160;               // px per second²
    private static final double MANEUVER_SPEED_FACTOR = 0.45;
    private static final double INITIAL_OCCUPANCY = 0.4;
    private static final int MAX_ARRIVING = 16;            // vehicles looking for a stall at once
    private static final double MIN_DWELL = 30, MAX_DWELL = 90;
    private static final double DEADLOCK_WAIT = 4.0;       // seconds blocked before pushing through
    private static final double EXIT_WAIT_LIMIT = 6.0;     // seconds a parked car waits for a gap

    private final SimulationBroadcaster broadcaster;
    private final Random random = new Random();
    private final ParkingLot lot = new ParkingLot();
    private final List<Vehicle> vehicles = new ArrayList<>();
    private VehicleSpawner spawner;

    private boolean started = false;
    private boolean running = true;
    private String speed = "normal";
    private String density = "normal";
    private double spawnTimer = 0;
    private int servedCount = 0;

    public SimulationEngine(SimulationBroadcaster broadcaster, RawWebSocketHandler wsHandler) {
        this.broadcaster = broadcaster;
        wsHandler.setControlListener(this::handleControl);
        reset();
    }

    public synchronized void start() {
        if (started) return;
        started = true;

        Thread simulationThread = new Thread(() -> {
            while (true) {
                long tickStart = System.currentTimeMillis();
                try {
                    SimulationStateDTO snapshot;
                    synchronized (this) {
                        if (running) step(TICK_MS / 1000.0 * speedMultiplier());
                        snapshot = snapshot();
                    }
                    broadcaster.broadcast(snapshot);
                } catch (Exception e) {
                    System.err.println("Error in simulation loop: " + e.getMessage());
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(Math.max(1, TICK_MS - (System.currentTimeMillis() - tickStart)));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }, "simulation-loop");
        simulationThread.setDaemon(true);
        simulationThread.start();
    }

    // ─── Controls ────────────────────────────────────────────────────────────

    /** Handles {"type":"CONTROL","action":"play|pause|reset","speed":"...","density":"..."}. */
    public synchronized void handleControl(JsonNode msg) {
        switch (msg.path("action").asText("")) {
            case "play" -> running = true;
            case "pause" -> running = false;
            case "reset" -> reset();
            default -> { }
        }
        String s = msg.path("speed").asText("");
        if (s.equals("slow") || s.equals("normal") || s.equals("fast")) speed = s;
        String d = msg.path("density").asText("");
        if (d.equals("sparse") || d.equals("normal") || d.equals("heavy")) density = d;
    }

    private double speedMultiplier() {
        return switch (speed) {
            case "slow" -> 0.5;
            case "fast" -> 2.0;
            default -> 1.0;
        };
    }

    /** Average seconds (simulation time) between arrivals. */
    private double spawnInterval() {
        return switch (density) {
            case "sparse" -> 4.0;
            case "heavy" -> 0.9;
            default -> 2.0;
        };
    }

    /** Clears the lot and pre-fills some stalls so there is something to watch immediately. */
    private void reset() {
        vehicles.clear();
        for (ParkingSpot spot : lot.getSpots()) spot.release();
        spawner = new VehicleSpawner(random);
        servedCount = 0;
        spawnTimer = 0;

        for (ParkingSpot spot : lot.getSpots()) {
            if (spot.isHandicapped() || random.nextDouble() >= INITIAL_OCCUPANCY) continue;
            Vehicle v = spawner.spawnAt(spot.getX(), spot.getY(), spot.getParkedAngle());
            v.setSpot(spot);
            spot.reserve(v.getId());
            v.setState("PARKED");
            v.setDwellRemaining(2 + random.nextDouble() * MAX_DWELL);
            vehicles.add(v);
        }
    }

    // ─── Simulation step ─────────────────────────────────────────────────────

    private void step(double dt) {
        spawnTimer -= dt;
        if (spawnTimer <= 0) {
            spawnTimer = trySpawn()
                    ? spawnInterval() * (0.6 + 0.8 * random.nextDouble())
                    : 0.25; // entrance busy, try again shortly
        }

        for (Vehicle v : new ArrayList<>(vehicles)) {
            update(v, dt);
        }
        vehicles.removeIf(v -> !v.isParked() && !v.hasRoute());
    }

    private boolean trySpawn() {
        long arriving = vehicles.stream()
                .filter(v -> !v.isParked() && !v.getState().equals("EXITING")).count();
        if (arriving >= MAX_ARRIVING) return false;
        if (!CollisionDetector.isAreaClear(ParkingLot.SPAWN_X, ParkingLot.SPAWN_Y, 60, null, vehicles)) {
            return false;
        }

        Vehicle v = spawner.spawnAtEntrance();
        if (v.getType().equals("POLICE")) {
            v.setRoute(lot.routeThrough(random.nextInt(lot.getAisleCount())));
        } else {
            ParkingSpot spot = allocateSpot(v);
            if (spot == null) return true; // lot full: nobody else comes in
            spot.reserve(v.getId());
            v.setSpot(spot);
            v.setRoute(lot.routeToSpot(spot));
        }
        vehicles.add(v);
        return true;
    }

    /**
     * Allocation strategy: closest free stall to the store (smallest x), with a
     * little randomness among the best few so cars spread over the aisles.
     * HC stalls are only used by permit holders, who prefer them.
     */
    private ParkingSpot allocateSpot(Vehicle v) {
        List<ParkingSpot> candidates = lot.getSpots().stream()
                .filter(ParkingSpot::isFree)
                .filter(s -> !s.isHandicapped() || v.hasHandicapPermit())
                .sorted(Comparator.comparingDouble(s -> s.getX() - (s.isHandicapped() ? 1000 : 0)))
                .limit(4)
                .toList();
        if (candidates.isEmpty()) return null;
        return candidates.get(random.nextInt(candidates.size()));
    }

    private void update(Vehicle v, double dt) {
        if (v.isParked()) {
            updateParked(v, dt);
            return;
        }
        if (v.getGhostTime() > 0) v.setGhostTime(v.getGhostTime() - dt);

        Waypoint wp = v.currentWaypoint();
        if (wp == null) return;

        if (CollisionDetector.mustYield(v, vehicles)) {
            v.setSpeed(0);
            v.setBlockedTime(v.getBlockedTime() + dt);
            if (v.getState().equals("MOVING")) v.setState("STOPPED");
            if (v.getBlockedTime() > DEADLOCK_WAIT) {
                v.setGhostTime(1.2);
                v.setBlockedTime(0);
            }
            return;
        }
        v.setBlockedTime(0);
        if (v.getState().equals("STOPPED")) v.setState("MOVING");

        boolean maneuvering = wp.reverse() || v.getState().equals("PARKING");
        double targetSpeed = v.getMaxSpeed() * (maneuvering ? MANEUVER_SPEED_FACTOR : 1.0);
        v.setSpeed(Math.min(targetSpeed, v.getSpeed() + ACCEL * dt));

        double move = v.getSpeed() * dt;
        while (move > 0 && wp != null) {
            double dx = wp.x() - v.getX();
            double dy = wp.y() - v.getY();
            double dist = Math.hypot(dx, dy);
            if (dist <= move) {
                v.setPosition(wp.x(), wp.y());
                move -= dist;
                reachWaypoint(v, wp);
                if (v.isParked()) return;
                wp = v.currentWaypoint();
            } else {
                v.setPosition(v.getX() + dx / dist * move, v.getY() + dy / dist * move);
                move = 0;
            }
        }
        if (wp != null) steerTowards(v, wp, dt);
    }

    private void updateParked(Vehicle v, double dt) {
        v.setDwellRemaining(v.getDwellRemaining() - dt);
        if (v.getDwellRemaining() > 0) return;

        // Wait for a gap in aisle traffic before backing out
        ParkingSpot spot = v.getSpot();
        boolean clear =
                CollisionDetector.isAreaClear(spot.getX() + 14, (spot.getEdgeY() + spot.getLaneY()) / 2, 50, v, vehicles)
                && CollisionDetector.isAreaClear(spot.getX() + 70, spot.getLaneY(), 40, v, vehicles);
        if (!clear && v.getBlockedTime() < EXIT_WAIT_LIMIT) {
            v.setBlockedTime(v.getBlockedTime() + dt);
            return;
        }
        v.setBlockedTime(0);
        v.setRoute(lot.routeOutOfSpot(spot));
        v.setState("EXITING");
    }

    private void reachWaypoint(Vehicle v, Waypoint wp) {
        v.advanceWaypoint();
        ParkingSpot spot = v.getSpot();

        if (wp.parks()) {
            v.setState("PARKED");
            v.setSpeed(0);
            v.setAngle(spot.getParkedAngle());
            v.setDwellRemaining(MIN_DWELL + random.nextDouble() * (MAX_DWELL - MIN_DWELL));
            return;
        }

        Waypoint next = v.currentWaypoint();
        if (v.getState().equals("EXITING")) {
            // Fully backed out into the aisle: the stall is free again
            if (spot != null && next != null && !next.reverse()) {
                spot.release();
                v.setSpot(null);
                servedCount++;
            }
        } else if (spot != null && v.routeSize() <= 3) {
            // Last three waypoints are the turn into the stall
            v.setState("PARKING");
        }
    }

    /** Rotates the vehicle's nose towards its travel direction at a limited turn rate. */
    private void steerTowards(Vehicle v, Waypoint wp, double dt) {
        double dx = wp.x() - v.getX();
        double dy = wp.y() - v.getY();
        if (Math.hypot(dx, dy) < 0.5) return;
        double target = Math.toDegrees(Math.atan2(dy, dx)) + (wp.reverse() ? 180 : 0);
        double diff = ((target - v.getAngle()) % 360 + 540) % 360 - 180;
        double maxTurn = TURN_RATE * dt;
        double turn = Math.max(-maxTurn, Math.min(maxTurn, diff));
        v.setAngle(((v.getAngle() + turn) % 360 + 360) % 360);
    }

    // ─── Snapshot ────────────────────────────────────────────────────────────

    private SimulationStateDTO snapshot() {
        SimulationStateDTO dto = new SimulationStateDTO();
        dto.vehicles = vehicles.stream().map(VehicleDTO::from).toList();
        dto.timestamp = System.currentTimeMillis();
        dto.vehicleCount = vehicles.size();
        dto.avgSpeed = vehicles.stream().filter(v -> !v.isParked())
                .mapToDouble(Vehicle::getSpeed).average().orElse(0);
        dto.parkedCount = (int) vehicles.stream().filter(Vehicle::isParked).count();
        dto.totalSpots = lot.getSpots().size();
        dto.spotsLeft = (int) lot.getSpots().stream().filter(ParkingSpot::isFree).count();
        dto.servedCount = servedCount;
        dto.running = running;
        dto.speed = speed;
        dto.density = density;
        return dto;
    }

    public synchronized void stop() {
        running = false;
    }
}
