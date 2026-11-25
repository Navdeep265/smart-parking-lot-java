package parking.spring;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import parking.*;

@SpringBootApplication
public class SpringParkingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringParkingApplication.class, args);
    }

    @Bean AllocationStrategy allocationStrategy() { return new BestFitAllocation(); }
    @Bean FeeStrategy feeStrategy() { return new HourlyFeeStrategy(); }
    @Bean Clock clock() { return Clock.systemUTC(); }

    @Bean(destroyMethod = "close")
    ParkingService parkingService(AllocationStrategy allocation, FeeStrategy fees, Clock clock) throws Exception {
        Files.createDirectories(Path.of("data"));
        return new ParkingService(new JdbcParkingRepository("jdbc:hsqldb:file:data/spring-parking"), allocation, fees, clock);
    }
}
