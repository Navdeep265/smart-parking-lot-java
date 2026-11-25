# OOP, SOLID and design principles

## Four OOP pillars
| Principle | Implementation |
|---|---|
| Encapsulation | Vehicle validates and normalizes its private final plate; immutable records represent spot and ticket snapshots; repository owns its connection. |
| Abstraction | AllocationStrategy, FeeStrategy, ParkingQueries and ParkingRepository expose contracts. |
| Inheritance | Motorcycle, Car and Bus extend Vehicle and supply vehicle-specific size and type. |
| Polymorphism | BestFitAllocation uses Vehicle.requiredSize() without type checks; strategies can be substituted through their interfaces. |

## SOLID
| Principle | Where it is applied | Trade-off |
|---|---|---|
| Single Responsibility | ParkingService orchestrates use cases; JDBC repository persists; FeeStrategy prices; AllocationStrategy selects; API adapter handles HTTP. | Repository combines transaction and persistence responsibilities for this small demo. |
| Open/Closed | Add a FeeStrategy or AllocationStrategy implementation and inject it without changing ParkingService. | Adding vehicle types still requires updating the enum and factory. |
| Liskov Substitution | All three Vehicle subclasses honor the plate and required-size contract. StrategyTest exercises them through Vehicle. | Substitutes must obey the strategy contract (e.g. return a compatible free spot). |
| Interface Segregation | ParkingQueries offers only reads, while ParkingRepository extends it with transactional writes; fee and allocation contracts are separate. | Additional write interfaces could be introduced if more clients need narrower contracts. |
| Dependency Inversion | ParkingService depends on interfaces and Clock via its constructor, not a JDBC connection or concrete strategy. | SQLException remains in the contract; this is a pragmatic educational compromise. |

The tests demonstrate examples of these principles; no automated test proves that an entire system satisfies SOLID in every future extension.

## Design principles
- Composition over inheritance for pricing and allocation.
- DRY: shared pricing and allocation policies across both API adapters.
- KISS: one parking lot, a small relational model, and synchronous transactions.
- Separation of concerns: the API maps requests and responses; the service coordinates business operations; repository handles storage.
