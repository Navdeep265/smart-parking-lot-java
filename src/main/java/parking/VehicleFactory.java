package parking;
public final class VehicleFactory { private VehicleFactory(){} public static Vehicle create(String plate,VehicleType type){return switch(type){case MOTORCYCLE -> new Motorcycle(plate);case CAR -> new Car(plate);case BUS -> new Bus(plate);};} }
