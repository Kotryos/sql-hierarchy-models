# SQL Hierarchy Models
[![CI](https://github.com/Kotryos/sql-hierarchy-models/actions/workflows/ci.yml/badge.svg)](https://github.com/Kotryos/sql-hierarchy-models/actions/workflows/ci.yml)

Compares three relational models for hierarchical data using Java 21, Spring Boot, jOOQ, Flyway, JUnit 5, Database Rider, and Testcontainers.

The original project evaluated only nested-set read operations.

This is a *structural* comparison: each model implements the same read operations with equivalent, correct queries shown side by side. It is not a benchmark, and write operations are currently out of scope.

## Models

All repositories implement the same read operations:

- find leaf nodes
- find a node path
- find a flattened subtree
- find node depth

| Model | Storage shape | Read strengths | Write tradeoff |
| --- | --- | --- | --- |
| Nested Set | One row per node with `lft` and `rgt` bounds. | Fast paths, subtrees, leaf checks, and depths with range queries. | Moving or inserting nodes requires recalculating bounds. |
| Adjacency List | One row per node with `parent_id`. | Simple schema and simple writes. | Paths and subtrees need recursive queries. |
| Closure Table | One node table plus one ancestor/descendant path table. | Fast paths, subtrees, and depths with joins. | Writes must maintain extra path rows. |

## Test Fixture

```text
Vehicles
|-- Motor Vehicles
|   |-- Cars
|   |   |-- Convertibles
|   |   `-- Minivans
|   `-- Buses
`-- Rail Vehicles
    |-- Trains
    `-- Trams
```

Nested Set stores containment bounds:

```text
Vehicles [1,18]
|-- Motor Vehicles [2,11]
|   |-- Cars [3,8]
|   |   |-- Convertibles [4,5]
|   |   `-- Minivans [6,7]
|   `-- Buses [9,10]
`-- Rail Vehicles [12,17]
    |-- Trains [13,14]
    `-- Trams [15,16]
```

Adjacency List stores direct parent links:

```text
id  label           parent_id
1   Vehicles        null
2   Motor Vehicles  1
4   Cars            2
8   Convertibles    4
9   Minivans        4
```

Closure Table splits nodes and paths into two tables:

```text
closure_table_node
id  label           sort_order
1   Vehicles        1
2   Motor Vehicles  2
4   Cars            3
8   Convertibles    4

closure_table_path
ancestor_id  descendant_id  depth
1            1              0
1            4              2
2            8              2
4            8              1
8            8              0
```

Test datasets live under `src/test/resources/datasets`, split by model. JUnit `@Nested` classes run the same contract against PostgreSQL and MySQL.

## How To Run

```shell
mvn verify
```

The build runs jOOQ code generation, Flyway migrations, and Testcontainers-backed integration tests. Docker must be available.

## jOOQ Code Generation

jOOQ metadata is generated from Flyway migrations during `generate-sources` and written to `target/generated-sources/jooq`.

```shell
mvn generate-sources
```
