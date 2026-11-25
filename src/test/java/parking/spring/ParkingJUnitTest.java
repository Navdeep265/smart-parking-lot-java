package parking.spring;

import org.junit.jupiter.api.Test;
import parking.ParkingTest;
import parking.StrategyTest;

class ParkingJUnitTest {
    @Test void integrationTests() throws Exception { ParkingTest.main(new String[0]); }
    @Test void strategyTests() throws Exception { StrategyTest.main(new String[0]); }
}
