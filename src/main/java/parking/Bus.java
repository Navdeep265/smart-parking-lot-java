package parking;
public final class Bus extends Vehicle { public Bus(String plate){super(plate);} public VehicleType type(){return VehicleType.BUS;} public int requiredSize(){return 3;} }
