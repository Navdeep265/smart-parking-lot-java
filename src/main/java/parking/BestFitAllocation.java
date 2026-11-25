package parking;
import java.util.*;
public final class BestFitAllocation implements AllocationStrategy {
    public Optional<ParkingSpot> select(Vehicle v,List<ParkingSpot> available){return available.stream().filter(s->s.size()>=v.requiredSize()).min(Comparator.comparingInt(ParkingSpot::size).thenComparingInt(ParkingSpot::floor).thenComparingInt(ParkingSpot::id));}
}
