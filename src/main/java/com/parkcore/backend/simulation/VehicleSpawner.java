package com.parkcore.backend.simulation;

import com.parkcore.backend.model.ParkingLot;
import com.parkcore.backend.model.Vehicle;
import java.util.Random;

/**
 * Creates new vehicles. Types must match frontend VehicleType, and sizes must
 * match VEHICLE_SIZE in frontend/src/app/simulation/roadsystem/lib/vehicleAssets.ts.
 */
public class VehicleSpawner {

    private record Spec(String type, int weight, double length, double width, double speed) {}

    private static final Spec[] SPECS = {
        new Spec("BLUE_CAR",   12, 40, 20, 90),
        new Spec("RED_CAR",    12, 40, 20, 90),
        new Spec("GREEN_CAR",  12, 40, 20, 90),
        new Spec("WHITE_CAR",  12, 40, 20, 90),
        new Spec("YELLOW_CAR", 12, 40, 20, 90),
        new Spec("TAXI",       10, 40, 20, 95),
        new Spec("VAN",        12, 44, 22, 75),
        new Spec("SEMI_TRUCK",  5, 50, 24, 60),
        new Spec("POLICE",      4, 40, 20, 105),
    };
    private static final int TOTAL_WEIGHT;
    static {
        int sum = 0;
        for (Spec s : SPECS) sum += s.weight;
        TOTAL_WEIGHT = sum;
    }

    private static final double HANDICAP_PERMIT_CHANCE = 0.08;

    private final Random random;
    private long nextSeq = 0;

    public VehicleSpawner(Random random) {
        this.random = random;
    }

    /** A random vehicle at the entrance, facing up the IN lane. */
    public Vehicle spawnAtEntrance() {
        return create(pickSpec(), ParkingLot.SPAWN_X, ParkingLot.SPAWN_Y, -90);
    }

    /** A random non-police vehicle placed at an arbitrary point (used to pre-fill stalls). */
    public Vehicle spawnAt(double x, double y, double angle) {
        Spec spec;
        do {
            spec = pickSpec();
        } while (spec.type.equals("POLICE"));
        return create(spec, x, y, angle);
    }

    private Vehicle create(Spec spec, double x, double y, double angle) {
        long seq = nextSeq++;
        boolean permit = !spec.type.equals("POLICE") && random.nextDouble() < HANDICAP_PERMIT_CHANCE;
        return new Vehicle("vehicle-" + seq, spec.type, seq, spec.length, spec.width,
                spec.speed, permit, x, y, angle);
    }

    private Spec pickSpec() {
        int roll = random.nextInt(TOTAL_WEIGHT);
        for (Spec s : SPECS) {
            roll -= s.weight;
            if (roll < 0) return s;
        }
        return SPECS[0];
    }
}
