package parking;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
public class ParkingTest {
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    static int free(ParkingService s)throws Exception{return s.availability().stream().mapToInt(Availability::available).sum();}
    public static void main(String[] args)throws Exception {
        check(new HourlyFeeStrategy().calculate(0,0,50)==50,"minimum fee");check(new HourlyFeeStrategy().calculate(0,3600000,50)==50,"exact hour");check(new HourlyFeeStrategy().calculate(0,9000000,50)==150,"rounded fee");System.out.println("PASS fee calculation");
        try(var s=new ParkingService(new JdbcParkingRepository("jdbc:hsqldb:mem:parkingtest"),new BestFitAllocation(),new HourlyFeeStrategy(),Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"),ZoneOffset.UTC))){
            check(free(s)==33,"initial capacity");var car=s.checkIn("CAR01",VehicleType.CAR);check(car.spot()==105&&free(s)==32,"best fit");
            try{s.checkIn("CAR01",VehicleType.CAR);throw new AssertionError("duplicate allowed");}catch(Conflict expected){}System.out.println("PASS allocation and duplicate rejection");
            var out=s.checkOut(car.id());check(out.fee()==50&&free(s)==33&&out.equals(s.checkOut(car.id())),"checkout");System.out.println("PASS checkout and idempotency");
            for(int i=0;i<5;i++)s.checkIn("BUS"+i,VehicleType.BUS);
            ExecutorService executor=Executors.newFixedThreadPool(8);try{List<Callable<Boolean>> jobs=new ArrayList<>();for(int i=0;i<12;i++){final int n=i;jobs.add(()->{try{s.checkIn("RACE"+n,VehicleType.BUS);return true;}catch(Conflict expected){return false;}});}int wins=0;for(var f:executor.invokeAll(jobs))if(f.get())wins++;check(wins==1&&free(s)==27,"concurrent allocation");}finally{executor.shutdownNow();}System.out.println("PASS concurrent requests for last compatible spot");
        }
    }
}
