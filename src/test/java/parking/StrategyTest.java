package parking;

import java.nio.file.*;
import java.time.*;
import java.util.*;

public class StrategyTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
    private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        var strategy = new BestFitAllocation();
        List<ParkingSpot> spots = List.of(new ParkingSpot(3,1,3),new ParkingSpot(2,1,2),new ParkingSpot(1,1,1));
        for(VehicleType type : VehicleType.values()) {
            Vehicle v = VehicleFactory.create("TEST1", type);
            check(strategy.select(v,spots).orElseThrow().size()==v.requiredSize(),"vehicle substitutability");
        }
        check(strategy.select(new Car("CAR1"),List.of(spots.get(0))).orElseThrow().size()==3,"larger fallback");
        check(strategy.select(new Bus("BUS1"),List.of(spots.get(2))).isEmpty(),"incompatible spot");
        System.out.println("PASS vehicle polymorphism and compatibility");

        FeeStrategy customFees = new FeeStrategy() {
            public int hourlyRate(VehicleType t){return 7;}
            public int calculate(long a,long b,int rate){return rate*2;}
        };
        AllocationStrategy customAllocation = (v,candidates) -> candidates.stream().filter(s->s.size()>=v.requiredSize()).max(Comparator.comparingInt(ParkingSpot::id));
        try(var s=new ParkingService(new JdbcParkingRepository("jdbc:hsqldb:mem:custom"),customAllocation,customFees,CLOCK)) {
            var ticket=s.checkIn("ALT1",VehicleType.CAR);
            check(ticket.spot()==311 && s.checkOut(ticket.id()).fee()==14,"injected strategies");
        }
        System.out.println("PASS strategy substitution without modifying service");

        FeeStrategy failingFees=new FeeStrategy(){
            public int hourlyRate(VehicleType t){throw new IllegalStateException("simulated failure");}
            public int calculate(long a,long b,int r){throw new IllegalStateException();}
        };
        try(var repo=new JdbcParkingRepository("jdbc:hsqldb:mem:rollback")) {
            var broken=new ParkingService(repo,strategy,failingFees,CLOCK);
            try{broken.checkIn("ROLL1",VehicleType.CAR);throw new AssertionError("expected failure");}catch(IllegalStateException expected){}
            var working=new ParkingService(repo,strategy,new HourlyFeeStrategy(),CLOCK);
            check(working.availability().stream().mapToInt(Availability::available).sum()==33,"rollback capacity");
            check(working.checkIn("ROLL1",VehicleType.BUS).type()==VehicleType.BUS,"vehicle registration rollback");
        }
        System.out.println("PASS transaction rollback");

        Path temp=Files.createTempDirectory("parking-persistence-");
        try {
            String url="jdbc:hsqldb:file:"+temp.resolve("test").toAbsolutePath();
            String id;
            try(var s=new ParkingService(new JdbcParkingRepository(url),strategy,new HourlyFeeStrategy(),CLOCK)){id=s.checkIn("PERSIST1",VehicleType.CAR).id();}
            try(var s=new ParkingService(new JdbcParkingRepository(url),strategy,new HourlyFeeStrategy(),CLOCK)){check(s.getTicket(id).plate().equals("PERSIST1"),"persisted ticket");check(s.checkOut(id).fee()==50,"persisted rate");}
        } finally {try(var paths=Files.walk(temp)){for(Path path:paths.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(path);}}
        System.out.println("PASS persistence after database reopen");
    }
}
