# Smart Home Appliances

This project implements a room-based smart home appliance controller for lights, fans, and air conditioners. The design follows a layered Spring Boot architecture with separate responsibilities for persistence, domain logic, scheduling, and HTTP endpoints.

## Design notes

- `Room` owns the relationship to appliances for a clear ownership model.
- `Appliance` is an abstract base type, and each concrete device subtype defines its own shutdown behavior.
- `ApplianceService` centralizes validation and state changes, while the yearly maintenance scheduler only triggers a shutdown event.
- The code uses explicit locking per device to reduce the risk of concurrent overlapping state changes.

## Annual maintenance behavior

The application schedules a yearly maintenance action on January 1 at 01:00 in the configured timezone (`America/New_York` by default). At that time, every appliance is turned off. No extra actions are performed.

## Known limitations / trade-offs

- The demo uses H2 in-memory storage, which is ideal for local development and testing but is not a production-scale persistence solution.
- The scheduler is timezone-specific and assumes the JVM runs with a compatible timezone or the configured app timezone is valid.
- This implementation focuses on device state transitions and clear separation of concerns rather than a full distributed smart-home platform.
