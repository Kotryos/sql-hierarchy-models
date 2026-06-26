# SQL Hierarchy Models
[![CI](https://github.com/Kotryos/sql-hierarchy-models/actions/workflows/ci.yml/badge.svg)](https://github.com/Kotryos/sql-hierarchy-models/actions/workflows/ci.yml)

This project compares relational models for hierarchical data in SQL databases.
The active codebase is being modernized around Java 21, Spring Boot, jOOQ, Flyway, JUnit 5, AssertJ, and Testcontainers.

The original 2016 Spring Data JPA nested-set demo is preserved in the [`legacy-2016`](https://github.com/Kotryos/sql-hierarchy-models/tree/legacy-2016) tag.

## Current Model

The first modernized model is the Nested Set Model.
It stores each node with `lft` and `rgt` interval values and currently implements:

- retrieval of all leaf nodes
- path retrieval
- flattened subtree retrieval
- node depth retrieval

The current integration tests use this tree:

```text
Vehicles [1, 18]
|-- Motor Vehicles [2, 11]
|   |-- Cars [3, 8]
|   |   |-- Convertibles [4, 5]
|   |   `-- Minivans [6, 7]
|   `-- Buses [9, 10]
`-- Rail Vehicles [12, 17]
    |-- Trains [13, 14]
    `-- Trams [15, 16]
```

## How To Run

Run the full verification build from the project root:

```shell
mvn verify
```

The current baseline uses Flyway migrations and Testcontainers-backed integration tests for PostgreSQL and MySQL.
Docker must be available to run `mvn verify`.

## jOOQ Code Generation

jOOQ table metadata is generated from the Flyway SQL migrations during the Maven `generate-sources` phase.
Generated files are written to `target/generated-sources/jooq` and are not committed.

Run code generation explicitly with:

```shell
mvn generate-sources
```
