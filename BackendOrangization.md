src/main/java/com/yourname/parkingsim/
│
├── ParkingSimApplication.java              ← main entry, @SpringBootApplication + @EnableScheduling
│
├── config/
│   └── WebSocketConfig.java               ← WebSocket broker setup
│
├── model/                                 ← pure data, no logic
│   ├── Vehicle.java                       ← id, x, y, angle, speed, state, type, spotId
│   ├── ParkingSpot.java                   ← id, x, y, occupied, vehicleId
│   └── ParkingLot.java                    ← holds list of spots + aisles
│
├── dto/                                   ← what gets sent to frontend via WebSocket
│   ├── VehicleDTO.java                    ← mirrors Vehicle but serialization-safe
│   └── SimulationStateDTO.java            ← { vehicles, timestamp, vehicleCount, avgSpeed }
│
├── simulation/                            ← ALL the brain lives here
│   ├── SimulationEngine.java              ← @Service, owns the main loop + thread pool
│   ├── VehicleSpawner.java                ← creates new vehicles at entrance points
│   │
│   ├── vehicle/                           ← per-vehicle behavior
│   │   ├── VehicleBehavior.java           ← interface: update(vehicle, lot, allVehicles)
│   │   ├── NormalCarBehavior.java         ← implements VehicleBehavior
│   │   ├── TruckBehavior.java             ← implements VehicleBehavior (wider turns, slower)
│   │   └── PoliceBehavior.java            ← implements VehicleBehavior (priority, etc.)
│   │
│   └── logic/                             ← shared algorithms used by behaviors
│       ├── CollisionDetector.java         ← checks safe distance between vehicles
│       ├── PathFinder.java                ← finds route from entrance → spot → exit
│       └── StopSignHandler.java           ← determines if vehicle must stop/yield
│
└── broadcast/
    └── SimulationBroadcaster.java         ← @Scheduled, converts state → DTO → WebSocket


The flow end to end
SimulationEngine (every 100ms)
  └── for each Vehicle
        ├── StopSignHandler.mustStop(vehicle)?
        ├── CollisionDetector.isClearAhead(vehicle, allVehicles)?
        ├── VehicleBehavior.update(vehicle, lot)   ← moves it
        └── PathFinder.getNextWaypoint(vehicle)    ← guides it

SimulationBroadcaster (every 100ms)
  └── reads current state from SimulationEngine
  └── converts to SimulationStateDTO
  └── sends to /topic/simulation via WebSocket

Frontend receives → updateVehicles() → drawAllVehicles()