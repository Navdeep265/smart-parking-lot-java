package parking;
import java.sql.SQLException;
import java.util.List;
public interface ParkingQueries { Ticket getTicket(String id) throws SQLException; List<Availability> availability() throws SQLException; }
