# ParkCore Backend: Parking Simulation Engine

> **Spring Boot engine that simulates a shopping-center parking lot and streams it live over WebSocket.**

This is the **backend** of ParkCore. The UI that draws the lot is in [Parkcore-Frontend](https://github.com/XuanGiaHanNguyen/Parkcore-Frontend).

## ⚡ What it does

Every 50 ms the engine:

1. **Spawns arrivals** at the entrance, according to the traffic density.
2. **Allocates a stall**: the free stall closest to the store, with a little randomness among the best few. Handicapped stalls are used only by permit holders.
3. **Moves every vehicle** along its route: entrance lane → aisle → turn into the stall. Vehicles yield to traffic directly ahead. When two vehicles block each other, the one that arrived first goes, and a short timeout breaks any remaining deadlock.
4. **Parks and releases**: parked cars stay for a while, wait for a gap in the aisle, back out, U-turn and exit. The stall is freed as soon as the car is back in the aisle.
5. **Broadcasts** the full state to every connected client.

Police cars patrol an aisle without parking. The lot starts about 40% full so there's something to watch right away.

## 🛠️ Tech stack

Java 21 · Spring Boot 4 · Spring WebSocket (raw, no STOMP) · Jackson · Maven

## 🚀 Running locally

Requires **JDK 21 or newer**.

```bash
./mvnw spring-boot:run
```

The server listens on **port 8085** by default (set `PORT` to change it). The WebSocket endpoint is:

```
ws://localhost:8085/ws/simulation
```

Then start the [frontend](https://github.com/XuanGiaHanNguyen/Parkcore-Frontend).

### Build a jar / run tests

```bash
./mvnw package          # builds target/backend-0.0.1-SNAPSHOT.jar and runs tests
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Docker

```bash
docker build -t parkcore-backend .
docker run -p 8080:8080 parkcore-backend      # inside the container PORT=8080
```

## 📁 Code structure

```
src/main/java/com/parkcore/backend/
├── BackendApplication.java
├── config/SimulationConfig.java          Starts the engine on boot
├── model/
│   ├── ParkingLot.java                   Lot geometry (mirrors the frontend canvas) and routes
│   ├── ParkingSpot.java                  One stall: position, aisle, HC flag, reservation
│   ├── Vehicle.java                      Vehicle state and route
│   └── Waypoint.java                     Point on a route (drive / reverse / park)
├── simulation/
│   ├── SimulationEngine.java             Main loop: spawn, allocate, move, park, exit; controls
│   ├── VehicleSpawner.java               Vehicle types, sizes, speeds
│   └── logic/CollisionDetector.java      "Is anything in my path?" and right-of-way rules
├── broadcast/SimulationBroadcaster.java  Wraps state as STATE_UPDATE and sends it
├── dto/                                  JSON shapes sent to clients
└── websocket/                            Raw WebSocket endpoint + control message handling
```

`ParkingLot.java` uses the same constants as the frontend's `parkinglot.tsx` (canvas 1000 × 710). If you change the drawing, change both.

## 📡 WebSocket protocol

Server → client, every tick:

```json
{ "type": "STATE_UPDATE",
  "payload": { "vehicles": [ { "id": "vehicle-12", "type": "TAXI", "x": 512.4, "y": 161.5, "angle": 180,
                               "speed": 90, "state": "MOVING", "spotId": "R1-4" } ],
               "vehicleCount": 61, "parkedCount": 44, "totalSpots": 144, "spotsLeft": 89,
               "servedCount": 120, "running": true, "speed": "normal", "density": "normal",
               "avgSpeed": 71.2, "timestamp": 1761789012345 } }
```

Vehicle `state` is one of `MOVING`, `STOPPED`, `PARKING`, `PARKED`, `EXITING`.

Client → server (all fields optional):

```json
{ "type": "CONTROL", "action": "play | pause | reset", "speed": "slow | normal | fast", "density": "sparse | normal | heavy" }
```

## ☁️ Deploying

The engine keeps a background thread and open WebSocket connections, so it needs an always-on host (Render, Railway, Fly.io, a VPS…), not serverless functions. Deploy with the included `Dockerfile`. The platform's `PORT` variable is picked up automatically. Point the frontend's `NEXT_PUBLIC_WS_URL` at `wss://<your-host>/ws/simulation`.

All clients share one simulation, so anyone pressing pause or reset affects everyone watching.
