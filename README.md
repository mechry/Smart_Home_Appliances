# Smart Home Appliances

This project implements a room-based smart home appliance controller for lights, fans, and air conditioners. The design follows a layered Spring Boot architecture with separate responsibilities for persistence, domain logic, scheduling, and HTTP endpoints.

## Architecture Overview

The application follows a classic layered architecture:

- **Controller Layer**: REST endpoints for HTTP API (`RoomController`, `LightController`, `FanController`, `AirConditionerController`)
- **Service Layer**: Business logic, validation, and state management (`RoomService`, `LightService`, `FanService`, `AirConditionerService`)
- **Repository Layer**: Data access using Spring Data JPA
- **Domain Layer**: Entity models with business behavior (`Room`, `Appliance` and its subclasses)
- **Validation Layer**: Custom validators for cross-field and business rule validation
- **Scheduler Layer**: Annual maintenance shutdown scheduler

## Design Considerations

### Domain Model

- **Room owns appliances**: The `Room` entity maintains a one-to-many relationship with appliances. This provides a clear ownership model and prevents appliances from being assigned to multiple rooms simultaneously.
- **Abstract Appliance base**: `Appliance` is an abstract base class with shared properties (id, name, powerState, room). Concrete subclasses (`Light`, `Fan`, `AirConditioner`) define device-specific behavior and properties.
- **Polymorphic behavior**: Each appliance type implements `turnOn()` and `turnOff()` differently based on its characteristics (e.g., Light has switch position, Fan has speed, AirConditioner has thermostat mode).
- **Single ownership constraint**: The domain enforces that an appliance can only belong to one room at a time via the `addAppliance()` method.

### Concurrency Control

- **Per-device locking**: Each service maintains a `ConcurrentHashMap<Long, ReentrantLock>` to manage locks per device ID. This ensures thread-safe state changes without blocking all operations.
- **Lock acquisition pattern**: Locks are acquired using `computeIfAbsent()` and released in `finally` blocks to prevent deadlocks.
- **Assumption**: This locking strategy works for single-JVM deployments. For distributed systems, a distributed lock mechanism (e.g., Redis, ZooKeeper) would be required.

### Validation Strategy

- **Request-level validation**: DTOs use Jakarta validation annotations (`@NotBlank`, `@NotNull`, `@Min`, `@Max`) for basic field validation.
- **Business-level validation**: Custom validators (`RoomValidator`, `FanValidator`, `AirConditionerValidator`) handle business rules like room existence, device existence, and device-room association.
- **Internationalization**: Error messages use `MessageSource` for i18n support, allowing locale-specific error messages.

### API Design

- **RESTful conventions**: Controllers follow REST principles with appropriate HTTP methods (POST for creation, GET for retrieval, PUT for updates).
- **Room-scoped operations**: Controllers provide both device-specific and room-scoped endpoints (e.g., `PUT /api/lights/{id}/on` vs `PUT /api/lights/rooms/{roomId}/{lightId}/on`).
- **No pagination**: List endpoints return all records. This is acceptable for small datasets but would need pagination for production use.

## API Endpoints

### Rooms

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/rooms` | Create a new room | `{"name": "Living Room"}` | `{"id": 1, "name": "Living Room", "appliances": []}` |
| GET | `/api/rooms` | Get all rooms | - | `[{"id": 1, "name": "Living Room", "appliances": []}]` |
| GET | `/api/rooms/{roomId}` | Get room by ID | - | `{"id": 1, "name": "Living Room", "appliances": []}` |

**Example: Create a room**
```bash
curl -X POST http://localhost:8080/api/rooms \
  -H "Content-Type: application/json" \
  -d '{"name": "Living Room"}'
```

### Lights

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/lights` | Create a new light | `{"name": "Lamp", "roomId": 1}` | `{"id": 1, "name": "Lamp", "powerState": "OFF", "switchPosition": "OFF", "room": {...}}` |
| GET | `/api/lights` | Get all lights | - | `[{"id": 1, "name": "Lamp", ...}]` |
| GET | `/api/lights/room/{roomId}` | Get lights by room | - | `[{"id": 1, "name": "Lamp", ...}]` |
| PUT | `/api/lights/{lightId}/on` | Turn on a light | - | `{"id": 1, "powerState": "ON", "switchPosition": "ON", ...}` |
| PUT | `/api/lights/{lightId}/off` | Turn off a light | - | `{"id": 1, "powerState": "OFF", "switchPosition": "OFF", ...}` |
| PUT | `/api/lights/rooms/{roomId}/{lightId}/on` | Turn on light by room | - | `{"id": 1, "powerState": "ON", ...}` |
| PUT | `/api/lights/rooms/{roomId}/{lightId}/off` | Turn off light by room | - | `{"id": 1, "powerState": "OFF", ...}` |
| PUT | `/api/lights/rooms/{roomId}/all/on` | Turn on all lights in room | - | `[{"id": 1, "powerState": "ON", ...}]` |
| PUT | `/api/lights/rooms/{roomId}/all/off` | Turn off all lights in room | - | `[{"id": 1, "powerState": "OFF", ...}]` |
| DELETE | `/api/lights/{lightId}` | Delete a light | - | 204 No Content |

**Example: Create a light**
```bash
curl -X POST http://localhost:8080/api/lights \
  -H "Content-Type: application/json" \
  -d '{"name": "Lamp", "roomId": 1}'
```

**Example: Turn on a light**
```bash
curl -X PUT http://localhost:8080/api/lights/1/on
```

### Fans

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/fans` | Create a new fan | `{"name": "Ceiling Fan", "roomId": 1}` | `{"id": 1, "name": "Ceiling Fan", "powerState": "OFF", "speed": 0, "room": {...}}` |
| GET | `/api/fans` | Get all fans | - | `[{"id": 1, "name": "Ceiling Fan", ...}]` |
| GET | `/api/fans/room/{roomId}` | Get fans by room | - | `[{"id": 1, "name": "Ceiling Fan", ...}]` |
| PUT | `/api/fans/{fanId}/on` | Turn on a fan | - | `{"id": 1, "powerState": "ON", "speed": 1, ...}` |
| PUT | `/api/fans/{fanId}/off` | Turn off a fan | - | `{"id": 1, "powerState": "OFF", "speed": 0, ...}` |
| PUT | `/api/fans/{fanId}/speed` | Update fan speed | `{"speed": 2}` | `{"id": 1, "powerState": "ON", "speed": 2, ...}` |
| PUT | `/api/fans/rooms/{roomId}/{fanId}/on` | Turn on fan by room | - | `{"id": 1, "powerState": "ON", ...}` |
| PUT | `/api/fans/rooms/{roomId}/{fanId}/off` | Turn off fan by room | - | `{"id": 1, "powerState": "OFF", ...}` |
| PUT | `/api/fans/rooms/{roomId}/{fanId}/speed` | Update fan speed by room | `{"speed": 2}` | `{"id": 1, "powerState": "ON", "speed": 2, ...}` |
| PUT | `/api/fans/rooms/{roomId}/all/on` | Turn on all fans in room | - | `[{"id": 1, "powerState": "ON", ...}]` |
| PUT | `/api/fans/rooms/{roomId}/all/off` | Turn off all fans in room | - | `[{"id": 1, "powerState": "OFF", ...}]` |
| DELETE | `/api/fans/{fanId}` | Delete a fan | - | 204 No Content |

**Example: Create a fan**
```bash
curl -X POST http://localhost:8080/api/fans \
  -H "Content-Type: application/json" \
  -d '{"name": "Ceiling Fan", "roomId": 1}'
```

**Example: Update fan speed**
```bash
curl -X PUT http://localhost:8080/api/fans/1/speed \
  -H "Content-Type: application/json" \
  -d '{"speed": 2}'
```

**Example: Turn on a fan**
```bash
curl -X PUT http://localhost:8080/api/fans/1/on
```

### Air Conditioners

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/air-conditioners` | Create a new AC | `{"name": "AC Unit", "roomId": 1}` | `{"id": 1, "name": "AC Unit", "powerState": "OFF", "thermostatMode": "OFF", "room": {...}}` |
| GET | `/api/air-conditioners` | Get all ACs | - | `[{"id": 1, "name": "AC Unit", ...}]` |
| GET | `/api/air-conditioners/room/{roomId}` | Get ACs by room | - | `[{"id": 1, "name": "AC Unit", ...}]` |
| PUT | `/api/air-conditioners/{acId}/on` | Turn on an AC | - | `{"id": 1, "powerState": "ON", "thermostatMode": "COOL", ...}` |
| PUT | `/api/air-conditioners/{acId}/off` | Turn off an AC | - | `{"id": 1, "powerState": "OFF", "thermostatMode": "OFF", ...}` |
| PUT | `/api/air-conditioners/rooms/{roomId}/{acId}/on` | Turn on AC by room | - | `{"id": 1, "powerState": "ON", ...}` |
| PUT | `/api/air-conditioners/rooms/{roomId}/{acId}/off` | Turn off AC by room | - | `{"id": 1, "powerState": "OFF", ...}` |
| PUT | `/api/air-conditioners/rooms/{roomId}/all/on` | Turn on all ACs in room | - | `[{"id": 1, "powerState": "ON", ...}]` |
| PUT | `/api/air-conditioners/rooms/{roomId}/all/off` | Turn off all ACs in room | - | `[{"id": 1, "powerState": "OFF", ...}]` |
| DELETE | `/api/air-conditioners/{acId}` | Delete an AC | - | 204 No Content |

**Example: Create an air conditioner**
```bash
curl -X POST http://localhost:8080/api/air-conditioners \
  -H "Content-Type: application/json" \
  -d '{"name": "AC Unit", "roomId": 1}'
```

**Example: Turn on an air conditioner**
```bash
curl -X PUT http://localhost:8080/api/air-conditioners/1/on
```

## Assumptions

### Technical Assumptions

1. **Single JVM deployment**: The locking mechanism assumes a single application instance. Multi-instance deployments would require distributed coordination.
2. **Timezone configuration**: The scheduler assumes the application timezone is correctly configured. Default is `America/New_York`.
3. **Small dataset**: The application is designed for a reasonable number of rooms and appliances (typical home use case).
4. **No authentication/authorization**: The API has no security layer. In a production environment, Spring Security or similar would be required.
5. **No audit logging**: State changes are not logged for audit purposes. This could be added via Spring AOP or event listeners.
6. **No soft delete**: Deleted records are permanently removed. Soft delete would be preferable for production systems.

### Functional Assumptions

1. **Room-appliance relationship**: An appliance can only belong to one room at a time. Moving an appliance between rooms requires deletion and recreation.
2. **Device state persistence**: Device states (on/off, speed, etc.) are persisted in the database and survive application restarts.
3. **Immediate state changes**: All device state changes are assumed to be immediate. There is no support for delayed or scheduled operations (except the annual maintenance).
4. **No device communication**: The system does not actually communicate with physical devices. It only maintains their state in the database.
5. **Fan speed range**: Fan speed is limited to 0-2 (off, low, high). Speed 0 automatically turns off the fan.
6. **AC thermostat modes**: Air conditioner supports only OFF, COOL, HEAT, and DRY modes. No temperature setpoint control.
7. **Light switch behavior**: Lights have a simple on/off switch position that mirrors their power state.
8. **Room uniqueness**: Room names must be unique across the entire system (case-insensitive).
9. **Maintenance behavior**: The annual maintenance only turns off all devices. It does not perform any diagnostics, cleaning, or other maintenance tasks.
10. **No device grouping**: Devices cannot be grouped beyond their room association. No support for scenes or automation rules.

## Trade-offs

### Technology Choices

| Decision | Rationale | Trade-off |
|----------|-----------|-----------|
| H2 in-memory database | Fast setup, no external dependencies, ideal for testing | Not suitable for production; data lost on restart |
| Lombok | Reduces boilerplate code (getters, setters, constructors) | Adds compile-time dependency; can obscure generated code |
| ReentrantLock per device | Fine-grained concurrency control without global locking | Memory overhead for lock map; not distributed |
| Abstract base class pattern | Shared behavior and polymorphism | Requires type checking for device-specific operations |
| Spring Data JPA | Simplifies data access with repository pattern | May generate inefficient queries if not careful |

### Design Trade-offs

1. **Simplicity vs. Scalability**: The design prioritizes simplicity and clarity over horizontal scalability. Adding distributed coordination would increase complexity significantly.

2. **Synchronous vs. Asynchronous operations**: All operations are synchronous. For a real smart home system, asynchronous event-driven architecture (e.g., message queues) might be more appropriate for device commands.

3. **Validation location**: Validation occurs in both controllers (via annotations) and services (via custom validators). This provides defense in depth.

4. **No API versioning**: The API has no versioning strategy. In production, versioning (e.g., `/api/v1/lights`) would be necessary for backward compatibility.

5. **No pagination**: List endpoints return all matching records. This simplifies the API but could cause performance issues with large datasets.

### Functional Trade-offs

1. **Room-based organization vs. Flexible grouping**: Organizing devices by rooms provides a simple, intuitive model but limits flexibility. Users cannot create custom groups (e.g., "all bedroom lights") or scenes without room boundaries.

2. **Simple state model vs. Rich device features**: Each device type has a simplified state model (on/off, speed, mode). Real smart home devices often have many more features (color temperature, schedules, timers) which are not supported.

3. **Immediate execution vs. Scheduled operations**: All commands execute immediately. This simplifies the system but prevents users from scheduling device operations (e.g., "turn on lights at 7 PM").

4. **State persistence vs. Device synchronization**: The system maintains state in the database but doesn't synchronize with actual physical devices. This creates a potential mismatch between the system state and reality.

5. **Fixed device types vs. Extensible plugin system**: The system supports only three fixed device types (Light, Fan, AirConditioner). Adding new device types requires code changes rather than plugin-based extensions.

6. **No automation vs. Manual control**: The system requires manual control via API calls. There is no support for automation rules, triggers, or if-then logic.

7. **Single ownership vs. Device sharing**: An appliance can only belong to one room. This prevents complex scenarios where a device might logically belong to multiple areas (e.g., a hallway light serving two rooms).

8. **Global shutdown vs. Granular maintenance**: The annual maintenance turns off all devices globally. There's no support for room-specific or device-type-specific maintenance schedules.

## Annual Maintenance Behavior

The application schedules a yearly maintenance action on January 1 at 01:00 in the configured timezone (`America/New_York` by default). At that time, every appliance is turned off. No extra actions are performed.

**Implementation details**:
- Uses Spring's `@Scheduled` annotation with cron expression
- Calls `shutdownAll()` methods on each service
- Each service iterates through all devices and turns them off with proper locking

## Testing Strategy

- **Unit tests**: Service layer tests using Mockito for mocking dependencies
- **Integration tests**: Controller layer tests using `@SpringBootTest` and `MockMvc` with H2 database
- **Test coverage**: Tests cover CRUD operations, state transitions, validation, and error scenarios

## AI-Assisted Development

This project was developed with assistance from AI (Cascade) for the following aspects:

### Analysis and Planning
- **Requirements analysis**: Interpreted project requirements for a room-based smart home appliance controller
- **Architecture design**: Designed the layered architecture (Controller, Service, Repository, Domain, Validation, Scheduler)

### Development
- **Domain model creation**: Created entity classes (Room, Appliance, Light, Fan, AirConditioner) with proper relationships
- **Repository layer**: Configured Spring Data JPA repositories for data access

### Test Generation
- **Unit tests**: Generated comprehensive unit tests for all service classes using Mockito
- **Integration tests**: Created integration tests for all controllers using MockMvc and H2 database
- **Test coverage**: Ensured tests cover CRUD operations, state transitions, validation, and error scenarios

### Code Review
- **Code quality**: Reviewed and removed unused imports and code
- **Best practices**: Ensured adherence to Spring Boot and Java best practices
- **Consistency**: Maintained consistent coding style across all classes
- **Documentation**: Added comprehensive documentation including API endpoints, assumptions, and trade-offs

### Documentation
- **README enhancement**: enhanced comprehensive project documentation with architecture overview, design considerations, API documentation, assumptions, and trade-offs
- **API documentation**: Documented all REST endpoints with examples using curl commands
- **Design decisions**: Documented rationale behind technology choices and design trade-offs

## Future Enhancements

Potential improvements for production use:

1. **Database**: Replace H2 with PostgreSQL or MySQL for persistent storage
2. **Security**: Add Spring Security with JWT or OAuth2 authentication
3. **Distributed locking**: Implement Redis-based distributed locks for multi-instance deployments
4. **Event sourcing**: Add event bus (e.g., Kafka, RabbitMQ) for asynchronous device state changes
5. **Audit logging**: Implement audit trail for all state changes
6. **API documentation**: Add OpenAPI/Swagger documentation
7. **Pagination**: Implement pagination for list endpoints
8. **Rate limiting**: Add rate limiting to prevent API abuse
9. **Monitoring**: Add metrics via Micrometer and Actuator
10. **Circuit breakers**: Add resilience patterns for external dependencies
