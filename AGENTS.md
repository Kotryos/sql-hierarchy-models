# AGENTS.md

## Build

Run full verification with:

```shell
mvn -B verify
```

Docker must be available; integration tests use Testcontainers with PostgreSQL and MySQL.

## jOOQ

jOOQ sources are generated from Flyway migrations during `generate-sources`.
Generated files live in `target/generated-sources/jooq` and must not be committed.

## Tests

Tests use one shared hierarchy contract, one test class per model, and JUnit `@Nested` classes for PostgreSQL/MySQL.
Database Rider YAML fixtures live under `src/test/resources/datasets`.
Testcontainers are started through the singleton `TestDatabases` helper, not `@Testcontainers`.

## Style

Keep the project educational and readable.
Prefer simple jOOQ DSL, small YAML fixtures, and short SQL comments above repository methods.
Avoid custom random seeders, benchmarks, or extra databases unless there is a clear goal.
