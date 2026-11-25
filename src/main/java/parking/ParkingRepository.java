package parking;
import java.sql.SQLException;
import java.util.List;
public interface ParkingRepository extends ParkingQueries, AutoCloseable {
    @FunctionalInterface interface Work<T>{ T run() throws SQLException; }
    <T> T transaction(Work<T> work) throws SQLException;
    boolean isParked(String plate) throws SQLException;
    List<ParkingSpot> freeSpots() throws SQLException;
    void register(Vehicle v) throws SQLException;
    void save(Ticket ticket) throws SQLException;
    void complete(Ticket ticket) throws SQLException;
    void close() throws SQLException;
}
