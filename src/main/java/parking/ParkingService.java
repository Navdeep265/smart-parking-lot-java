package parking;
import java.sql.SQLException;
import java.time.Clock;
import java.util.*;
public final class ParkingService implements AutoCloseable {
    private final ParkingRepository repository;
    private final AllocationStrategy allocation;
    private final FeeStrategy fees;
    private final Clock clock;
    public ParkingService(ParkingRepository repository,AllocationStrategy allocation,FeeStrategy fees,Clock clock){this.repository=Objects.requireNonNull(repository);this.allocation=Objects.requireNonNull(allocation);this.fees=Objects.requireNonNull(fees);this.clock=Objects.requireNonNull(clock);}
    public Ticket checkIn(String plate,VehicleType type)throws SQLException {
        Vehicle v=VehicleFactory.create(plate,type);
        return repository.transaction(()->{if(repository.isParked(v.plate()))throw new Conflict("Vehicle already parked");ParkingSpot spot=allocation.select(v,repository.freeSpots()).orElseThrow(()->new Conflict("No compatible spot available"));repository.register(v);Ticket t=new Ticket(UUID.randomUUID().toString(),v.plate(),v.type(),spot.id(),spot.floor(),clock.millis(),null,null,fees.hourlyRate(v.type()));repository.save(t);return t;});
    }
    public Ticket checkOut(String id)throws SQLException {return repository.transaction(()->{Ticket t=repository.getTicket(id);if(t.exit()!=null)return t;long now=clock.millis();Ticket completed=new Ticket(t.id(),t.plate(),t.type(),t.spot(),t.floor(),t.entry(),now,fees.calculate(t.entry(),now,t.rate()),t.rate());repository.complete(completed);return completed;});}
    public Ticket getTicket(String id)throws SQLException{return repository.getTicket(id);}
    public List<Availability> availability()throws SQLException{return repository.availability();}
    public void close()throws SQLException{repository.close();}
}
