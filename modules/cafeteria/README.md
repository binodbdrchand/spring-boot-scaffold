# Cafeteria Module

The Cafeteria module is an independently buildable and deployable Atlas Platform module.

This document describes the complete development workflow for:

* generating REST API contracts
* generating event contracts
* adding JPA entities
* generating SQL from entities
* creating Flyway migrations
* validating the database schema
* running the application
* testing migrations
* inspecting and managing Flyway migrations
* resetting the development database

---

## 1. Module Location

```text
modules/cafeteria/
├── build.gradle.kts
├── README.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/atlas/cafeteria/
    │   └── resources/
    │       ├── application.yml
    │       └── db/
    │           └── migration/
    └── test/
```

The Cafeteria module is a Gradle project named:

```text
:cafeteria
```

All commands below are executed from the repository root:

```text
atlas-platform/
```

---

# 2. Contract Source of Truth

Contract files are **not generated inside the module source tree**.

The source-of-truth contracts are stored under:

```text
contracts/cafeteria/
├── src/main/resources/
│   ├── api/
│   │   └── openapi.yaml
│   └── events/
│       └── schemas/
│           └── OrderPlaced.json
```

Generated Java classes are written into the module's `build/` directory.

They are generated artifacts and must not be edited manually.

---

# 3. Generate REST API Contracts

REST APIs are defined in:

```text
contracts/cafeteria/src/main/resources/api/openapi.yaml
```

Generate the Java API interfaces and models:

```powershell
./gradlew :cafeteria:generateRestApi
```

Generated files are placed under:

```text
modules/cafeteria/build/generated/openapi/
```

The generated packages are:

```text
com.atlas.cafeteria.api.generated
com.atlas.cafeteria.api.generated.model
```

The generated API interfaces are intended to be implemented by the module's controllers.

---

# 4. Generate Event Contracts

Event schemas are stored under:

```text
contracts/cafeteria/src/main/resources/events/schemas/
```

Generate Java classes from all event schemas:

```powershell
./gradlew :cafeteria:generateJsonSchema2DataClass
```

Generated files are placed under:

```text
modules/cafeteria/build/generated/jsonschema/
```

The generated event classes use:

```text
com.atlas.cafeteria.events
```

---

# 5. Generate All Contracts

To generate both REST API and event contracts:

```powershell
./gradlew :cafeteria:generateContracts
```

This runs:

```text
generateRestApi
generateJsonSchema2DataClass
```

This is the preferred command after changing any contract.

---

# 6. Build the Module

Compile the module:

```powershell
./gradlew :cafeteria:build
```

This also generates the contracts automatically because Java compilation depends on contract generation.

A normal build therefore performs:

```text
Contract source
      ↓
Generated Java
      ↓
Java compilation
      ↓
Tests
      ↓
JAR
```

---

# 7. Adding a JPA Entity

Entities belong under:

```text
modules/cafeteria/src/main/java/com/atlas/cafeteria/entity/
```

Example:

```text
entity/
└── CafeteriaTest.java
```

A new entity should:

1. be annotated with `@Entity`
2. define its table explicitly with `@Table`
3. define database column names where appropriate
4. use appropriate Java types
5. follow the module's existing entity conventions

Example:

```java
@Entity
@Table(name = "cafeteria_test")
public class CafeteriaTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "created_at")
    private Instant createdAt;
}
```

Do **not** let Hibernate create or update production database schemas.

The application uses:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

Hibernate therefore validates the database schema but does not modify it.

---

# 8. Generate SQL From JPA Entities

Hibernate is used to generate a proposed database schema from the current JPA entities.

Run:

```powershell
./gradlew :cafeteria:generateSchema
```

The generated SQL is written to:

```text
modules/cafeteria/build/generated/schema.sql
```

This is a **review artifact**.

It is not automatically applied to the database.

The workflow is:

```text
JPA Entity
    ↓
Hibernate schema generation
    ↓
build/generated/schema.sql
    ↓
Developer reviews SQL
    ↓
Developer creates Flyway migration
```

Never copy the generated schema blindly into an existing production database.

---

# 9. Create a Flyway Migration

Flyway migrations are stored under:

```text
modules/cafeteria/src/main/resources/db/migration/
```

Migration names follow Flyway's naming convention:

```text
V<version>__<description>.sql
```

Example:

```text
V1__create_cafeteria_item.sql
```

Another migration:

```text
V2__add_employee_reference_to_cafeteria_test.sql
```

Another:

```text
V3__create_cafeteria_order.sql
```

The version must increase for every new migration.

---

# 10. Creating a Migration From an Entity Change

Suppose a new entity produces this SQL:

```sql
create table cafeteria_order (
    id bigint generated by default as identity,
    employee_id bigint not null,
    created_at timestamp with time zone,
    primary key (id)
);
```

Create:

```text
src/main/resources/db/migration/V2__create_cafeteria_order.sql
```

Put the reviewed SQL into that migration.

The migration becomes the **authoritative database change**.

After the migration has been created, the generated Hibernate SQL is no longer the database source of truth.

---

# 11. Important Migration Rule

After a migration has been applied to a shared or persistent database:

**Do not edit the existing migration.**

For example, if this has already been applied:

```text
V2__create_cafeteria_order.sql
```

do not modify `V2`.

Create:

```text
V3__alter_cafeteria_order.sql
```

instead.

This preserves the database migration history.

---

# 12. Run the Application

Start the Cafeteria application:

```powershell
./gradlew :cafeteria:bootRun
```

This starts the HTTP server and therefore **does not exit automatically**.

The application uses an in-memory H2 database during the current development setup.

The startup sequence is:

```text
Spring Boot
    ↓
Hikari DataSource
    ↓
Flyway
    ↓
Apply pending migrations
    ↓
Hibernate
    ↓
Validate schema
    ↓
Start HTTP server
```

Stop the application with:

```text
Ctrl+C
```

---

# 13. Run Tests Without Starting the Server

For automated verification:

```powershell
./gradlew :cafeteria:test
```

Tests should be used for database/migration verification rather than using `bootRun` when the desired result is simply:

```text
migrate → validate → exit
```

The test process terminates automatically.

---

# 14. Current H2 Database

The current development configuration uses:

```text
jdbc:h2:mem:cafeteria
```

This is an **in-memory database**.

Consequently:

* the database exists only while the JVM is running
* stopping the application destroys the database
* starting the application creates a fresh database
* Flyway migrations run again against the fresh database
* there is currently no persistent local database to manually clean

Therefore, to reset the current development database completely:

```text
Stop the application
Start it again
```

or simply rerun:

```powershell
./gradlew :cafeteria:bootRun
```

The H2 database starts empty and Flyway applies all migrations again.

---

# 15. Flyway Migration Lifecycle

On application startup Flyway performs approximately:

```text
Connect to database
        ↓
Create flyway_schema_history if necessary
        ↓
Validate existing migrations
        ↓
Find pending migrations
        ↓
Apply pending migrations
        ↓
Record successful migrations
        ↓
Hibernate validates schema
```

Flyway records migration history in:

```text
flyway_schema_history
```

For the current H2 database this table is in:

```text
PUBLIC.flyway_schema_history
```

---

# 16. Flyway Commands

The application currently uses Flyway through Spring Boot.

The normal application-level operation is:

```powershell
./gradlew :cafeteria:bootRun
```

which automatically performs:

```text
validate
migrate
```

before Hibernate initializes.

There is currently no Flyway Gradle plugin configured in this project.

Therefore commands such as:

```powershell
./gradlew flywayMigrate
./gradlew flywayInfo
./gradlew flywayClean
```

are **not currently available**.

Do not add the Flyway Gradle plugin merely to execute these commands unless the project explicitly decides to use it.

---

# 17. Validate Migrations

Flyway validation checks whether the migration files match the migrations recorded in the database.

During application startup this happens automatically.

The log should contain something similar to:

```text
Successfully validated N migrations
```

If validation fails, investigate the migration history before modifying anything.

---

# 18. Migration Information

Flyway's migration history is stored in:

```text
flyway_schema_history
```

When using the current in-memory H2 database, the history disappears when the JVM exits.

For a persistent database, Flyway's `info` operation can be used through the Flyway CLI if the CLI is installed and configured for that database.

Example Flyway CLI operation:

```text
flyway info
```

---

# 19. Apply Pending Migrations

With the current Spring Boot setup, pending migrations are automatically applied when the application starts:

```powershell
./gradlew :cafeteria:bootRun
```

For a persistent deployment, application startup therefore performs:

```text
validate
→ migrate
→ Hibernate validation
```

---

# 20. Revert to an Empty Database

## Current H2 Development Database

Because the database is in-memory:

```text
jdbc:h2:mem:cafeteria
```

the database is reset simply by stopping the JVM.

Start it again:

```powershell
./gradlew :cafeteria:bootRun
```

This gives a completely empty database followed by:

```text
V1
V2
V3
...
```

being applied in order.

No manual `DROP TABLE` is required.

---

# 21. Revert the Last Migration

Normal Flyway migrations are **forward-only**.

There is no general Community-edition command equivalent to:

```text
flyway rollback-last
```

Do not manually delete the migration record from:

```text
flyway_schema_history
```

and do not manually modify migration history.

If a migration has already been applied and needs to be changed, create a new migration.

Example:

```text
V1__create_order.sql
V2__alter_order.sql
V3__remove_old_column.sql
```

The database history remains:

```text
V1
V2
V3
```

rather than pretending that `V2` never happened.

---

# 22. Undo Migrations

Flyway has an `undo` migration concept in editions/features where it is supported.

An undo migration is separate from the normal versioned migration.

For example:

```text
V2__create_order.sql
U2__drop_order.sql
```

However, the Atlas Platform does **not currently use undo migrations**.

The preferred project workflow is:

```text
Wrong/applied migration
        ↓
Create a new corrective migration
        ↓
Apply corrective migration
```

This preserves an auditable database history.

---

# 23. Cleaning a Persistent Development Database

If this project later uses a persistent local database, Flyway's `clean` operation can remove objects managed by Flyway.

Conceptually:

```text
flyway clean
```

followed by:

```text
flyway migrate
```

returns the database to an empty schema and reapplies all migrations.

**Do not use `clean` against production databases.**

Flyway intentionally protects against accidental destructive operations, and clean should only be enabled/used deliberately in development environments.

The current H2 database does not need this because it is already in-memory.

---

# 24. Repairing Migration Metadata

Flyway provides a `repair` operation for fixing migration metadata after certain failures or administrative situations.

Conceptually:

```text
flyway repair
```

`repair` does **not** undo database changes.

It repairs Flyway's schema history metadata.

Do not use it as a substitute for correcting a bad migration.

---

# 25. Recommended Development Workflow

When adding a new database-backed feature:

```text
1. Create/update JPA entity
          ↓
2. Compile
          ↓
3. Generate Hibernate SQL
          ↓
4. Review build/generated/schema.sql
          ↓
5. Create new Flyway V<n>__description.sql
          ↓
6. Review migration
          ↓
7. Run tests
          ↓
8. Start application
          ↓
9. Hibernate validates the migrated schema
```

Commands:

```powershell
./gradlew :cafeteria:generateSchema
```

Then create the migration:

```text
src/main/resources/db/migration/V<n>__description.sql
```

Then verify:

```powershell
./gradlew :cafeteria:test
```

Then, when the server is actually needed:

```powershell
./gradlew :cafeteria:bootRun
```

---

# 26. Recommended Contract Workflow

When changing an API or event contract:

```text
1. Change contract source
          ↓
2. Generate contracts
          ↓
3. Compile
          ↓
4. Implement/use generated types
          ↓
5. Run tests
```

Command:

```powershell
./gradlew :cafeteria:generateContracts
```

Or simply build:

```powershell
./gradlew :cafeteria:build
```

because Java compilation depends on contract generation.

---

# 27. Clean Generated Build Artifacts

Gradle generated contract classes and schema files live under:

```text
modules/cafeteria/build/
```

To remove all generated build artifacts:

```powershell
./gradlew :cafeteria:clean
```

Then regenerate everything:

```powershell
./gradlew :cafeteria:generateContracts
./gradlew :cafeteria:generateSchema
```

Or simply:

```powershell
./gradlew :cafeteria:build
```

for contract generation and compilation.

---

# 28. Useful Commands — Quick Reference

### Generate REST API

```powershell
./gradlew :cafeteria:generateRestApi
```

### Generate event classes

```powershell
./gradlew :cafeteria:generateJsonSchema2DataClass
```

### Generate all contracts

```powershell
./gradlew :cafeteria:generateContracts
```

### Generate Hibernate SQL

```powershell
./gradlew :cafeteria:generateSchema
```

### Compile

```powershell
./gradlew :cafeteria:compileJava
```

### Run tests

```powershell
./gradlew :cafeteria:test
```

### Build

```powershell
./gradlew :cafeteria:build
```

### Run application

```powershell
./gradlew :cafeteria:bootRun
```

### Clean generated artifacts

```powershell
./gradlew :cafeteria:clean
```

### Clean and build

```powershell
./gradlew :cafeteria:clean :cafeteria:build
```

---

# 29. Files That Matter

### Contract source

```text
contracts/cafeteria/src/main/resources/api/openapi.yaml
contracts/cafeteria/src/main/resources/events/schemas/
```

### Java source

```text
modules/cafeteria/src/main/java/com/atlas/cafeteria/
```

### Application configuration

```text
modules/cafeteria/src/main/resources/application.yml
```

### Flyway migrations

```text
modules/cafeteria/src/main/resources/db/migration/
```

### Generated OpenAPI Java

```text
modules/cafeteria/build/generated/openapi/
```

### Generated event Java

```text
modules/cafeteria/build/generated/jsonschema/
```

### Generated Hibernate SQL

```text
modules/cafeteria/build/generated/schema.sql
```

---

# 30. Rules to Remember

1. **Contracts are source-of-truth.**
2. **Generated Java is never edited manually.**
3. **JPA entities describe the intended schema.**
4. **Generated Hibernate SQL is reviewed, not automatically applied.**
5. **Flyway migrations are the database source of truth.**
6. **Never edit an already-applied migration.**
7. **Create a new migration for database changes.**
8. **Hibernate uses `ddl-auto=validate`; it does not modify the database.**
9. **`bootRun` intentionally keeps the server running.**
10. **Use tests for migration verification that should terminate automatically.**
11. **The current H2 database is in-memory, so stopping the JVM resets it.**
12. **Do not use destructive database operations against production.**

---

# 31. Typical Day-to-Day Workflow

For an API-only change:

```powershell
./gradlew :cafeteria:generateContracts
./gradlew :cafeteria:test
```

For an entity/database change:

```powershell
./gradlew :cafeteria:generateSchema
```

Review:

```text
build/generated/schema.sql
```

Create the appropriate:

```text
V<n>__description.sql
```

Then:

```powershell
./gradlew :cafeteria:test
```

When you need the running application:

```powershell
./gradlew :cafeteria:bootRun
```

Stop it with:

```text
Ctrl+C
```
