# Design patterns

| Pattern | Implementation | Why it helps |
|---|---|---|
| Strategy | AllocationStrategy / BestFitAllocation; FeeStrategy / HourlyFeeStrategy | Independently substitute allocation and pricing algorithms. |
| Simple Factory | VehicleFactory | Centralizes creation of the correct Vehicle subtype; this is a simple factory, not the GoF Factory Method pattern. |
| Repository | ParkingRepository / JdbcParkingRepository | Isolates JDBC and database access from business orchestration. |
| Dependency Injection | ParkingService constructor; Main composition root; SpringParkingApplication beans | Makes dependencies explicit and testable. |
| Transaction callback | ParkingRepository.transaction | Centralizes commit and rollback around a complete use case. |

Observer and Singleton are deliberately not implemented; polling and explicit instance ownership avoid unnecessary complexity. The optional Spring adapter is a framework integration, not a requirement for OOP.
