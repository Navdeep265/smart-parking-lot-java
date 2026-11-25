package parking.spring;

import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import parking.*;

@RestController
@RequestMapping("/api")
public class ParkingController {
    private final ParkingService service;
    public ParkingController(ParkingService service) { this.service = service; }

    public record TicketResponse(String ticketId, String plate, VehicleType vehicleType,
        int spot, int floor, Instant entryTime, Instant exitTime, Integer feeINR, String status) {
        static TicketResponse of(Ticket t) {
            return new TicketResponse(t.id(), t.plate(), t.type(), t.spot(), t.floor(),
                Instant.ofEpochMilli(t.entry()), t.exit() == null ? null : Instant.ofEpochMilli(t.exit()),
                t.fee(), t.exit() == null ? "ACTIVE" : "COMPLETED");
        }
    }
    public record AvailabilityResponse(int floor, int sizeRank, int total, int available) {}

    @PostMapping("/check-in")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse checkIn(@RequestParam("plate") String plate, @RequestParam("type") String type) throws SQLException {
        return TicketResponse.of(service.checkIn(plate, VehicleType.valueOf(type.toUpperCase(Locale.ROOT))));
    }
    @PostMapping("/check-out")
    public TicketResponse checkOut(@RequestParam("ticket") String ticket) throws SQLException {
        return TicketResponse.of(service.checkOut(ticket));
    }
    @GetMapping("/tickets/{id}")
    public TicketResponse ticket(@PathVariable("id") String id) throws SQLException { return TicketResponse.of(service.getTicket(id)); }
    @GetMapping("/availability")
    public List<AvailabilityResponse> availability() throws SQLException {
        return service.availability().stream().map(a -> new AvailabilityResponse(a.floor(), a.size(), a.total(), a.available())).toList();
    }
    @ExceptionHandler(Conflict.class)
    ResponseEntity<Map<String,String>> conflict(Conflict e) { return error(HttpStatus.CONFLICT,e.getMessage()); }
    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Map<String,String>> missing(NoSuchElementException e) { return error(HttpStatus.NOT_FOUND,e.getMessage()); }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String,String>> invalid(IllegalArgumentException e) { return error(HttpStatus.BAD_REQUEST,e.getMessage()); }
    @ExceptionHandler(SQLException.class)
    ResponseEntity<Map<String,String>> database(SQLException e) { return error(HttpStatus.INTERNAL_SERVER_ERROR,"Database operation failed"); }
    private ResponseEntity<Map<String,String>> error(HttpStatus status,String message) {
        return ResponseEntity.status(status).body(Map.of("error",message == null ? status.getReasonPhrase() : message));
    }
}
