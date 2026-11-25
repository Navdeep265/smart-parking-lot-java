package parking;
import java.util.*;
public interface AllocationStrategy { Optional<ParkingSpot> select(Vehicle vehicle,List<ParkingSpot> available); }
