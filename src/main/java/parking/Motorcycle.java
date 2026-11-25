package parking;
public final class Motorcycle extends Vehicle { public Motorcycle(String plate){super(plate);} public VehicleType type(){return VehicleType.MOTORCYCLE;} public int requiredSize(){return 1;} }
