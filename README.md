# Smart Parking Lot Management System | Java OOP & SOLID

A low-level design and runnable Java backend for a multi-floor smart parking lot. It allocates spots by vehicle size, records entry and exit, calculates fees and exposes current availability through HTTP APIs.

## Technology
- Java 21 (verified), JDK HTTP Server, JDBC, embedded HSQLDB.
- Optional Spring Boot 3.5.8 / Maven adapter with Swagger UI, modern HSQLDB, and JUnit 5 wrappers. This optional build was not executed in the authoring environment because Maven and network access were unavailable.
- No Maven, external database server or internet download required. The bundled HSQLDB 1.8 library is a legacy dependency for this local educational demo; its license is in `lib/`. For production use, migrate to a maintained database and framework.

## Run locally
Install a JDK (not just a JRE), extract the project and open a terminal in the project root.

Windows: `run.bat` (PowerShell: `.\run.bat`)

macOS/Linux: `sh run.sh`

Open http://localhost:8080 to use the dashboard. Stop the server with Ctrl+C. The database is created in `data/` and persists across restarts. Use `run.bat 8081` or `sh run.sh 8081` to change port.

## Optional Spring Boot mode (requires Maven and internet)
Use Java 21 and Maven 3.9+. In the project root:

```sh
mvn test
mvn spring-boot:run
```

Open http://localhost:8080 for the dashboard or http://localhost:8080/swagger-ui/index.html for Swagger UI. The Spring mode uses a separate `data/spring-parking` database and downloads HSQLDB 2.7.4. The standalone mode remains the verified no-download path. JDBC is retained rather than JPA/H2 to share one repository implementation across both modes. The Spring/Maven dependencies and endpoints are supplied but have not been built or tested here.

## Features
- 3 floors, each with 4 small, 5 medium and 2 large spots (33 total).
- Best-fit allocation: smallest compatible spot, then lowest floor and spot ID.
- Unique active parking per vehicle and spot.
- Atomic check-in and check-out, repeat checkout returns original result.
- Availability updates immediately on commit; browser dashboard polls every 3 seconds.
- UTC timestamps and integer INR amounts; hourly rate recorded at entry.

## API
POST bodies use `application/x-www-form-urlencoded`; responses are JSON.

| Method | Path | Parameters | Result |
|---|---|---|---|
| POST | `/api/check-in` | `plate`, `type` | 201, ticket |
| POST | `/api/check-out` | `ticket` | 200, completed ticket and fee |
| GET | `/api/tickets/{id}` | ticket ID in path | 200, ticket |
| GET | `/api/availability` | none | 200, counts by floor and size |

```sh
curl -d 'plate=HR07AB1234&type=CAR' http://localhost:8080/api/check-in
curl http://localhost:8080/api/availability
curl -d 'ticket=REPLACE_WITH_TICKET_ID' http://localhost:8080/api/check-out
```

Errors: 400 invalid input, 404 unknown ticket or route, 409 duplicate entry or no compatible spot, 500 unexpected error.

## Fee policy (assumed)
Motorcycle INR 20/hour; car INR 50/hour; bus INR 100/hour. Minimum one hour; each started hour is billed. A car parked for 2h 30m costs INR 150. Smaller vehicles may use larger spots when necessary.

## Low-level design
See [LLD](docs/LLD.md) for UML class, sequence and ER diagrams, [SOLID principles](docs/SOLID.md) for concrete examples and limitations, and [design patterns](docs/DESIGN_PATTERNS.md) for pattern rationale.

### Source map
| Component | Responsibility |
|---|---|
| Vehicle / Motorcycle / Car / Bus | Domain model and vehicle size behavior |
| VehicleFactory | Construct vehicle objects |
| ParkingSpot / Ticket / Availability | Immutable domain snapshots |
| AllocationStrategy / BestFitAllocation | Select compatible spots |
| FeeStrategy / HourlyFeeStrategy | Price each parking session |
| ParkingQueries / ParkingRepository | Read and transactional persistence contracts |
| JdbcParkingRepository | JDBC persistence and transaction synchronization |
| ParkingService | Orchestrate entry and exit use cases |
| Main | Standalone HTTP adapter and composition root |
| spring/ParkingController | Optional Spring REST adapter |
| spring/SpringParkingApplication | Optional Spring configuration and dependency injection |


## Test
Windows: `test.bat` (PowerShell: `.\test.bat`); macOS/Linux: `sh test.sh`.
The standalone test runners passed eight test groups on Java 21: fee boundaries, best-fit allocation and duplicate rejection, checkout idempotency, last-spot concurrency, polymorphic vehicle compatibility, strategy substitution, transaction rollback, and persistence after closing/reopening a temporary file database.

## GitHub submission
Create a public repository at https://github.com/new, then upload the contents of this directory, or run:

```sh
git init
git add .
git commit -m "Implement Java smart parking lot backend"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/smart-parking-lot-java.git
git push -u origin main
```

Replace YOUR_USERNAME. Do not commit generated `data/`, `out/` or `target/` folders.

## Scope and limitations
This is a single-process, local educational backend. The JDBC repository serializes database operations on one connection, with database uniqueness constraints as a second safeguard. It is not designed for multiple application instances. No authentication, payment processing, sensor integration or WebSocket push. The local dashboard is a demonstration interface, not a production gate controller.
