package parking;
import java.util.Locale;
public abstract class Vehicle {
    private final String plate;
    protected Vehicle(String plate) { this.plate=plate.trim().toUpperCase(Locale.ROOT); if(!this.plate.matches("[A-Z0-9-]{2,20}")) throw new IllegalArgumentException("Invalid registration number"); }
    public final String plate(){return plate;}
    public abstract VehicleType type();
    public abstract int requiredSize();
}
