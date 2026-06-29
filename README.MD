# SQL Hierarchy Models
[![CI](https://github.com/Kotryos/sql-hierarchy-models/actions/workflows/ci.yml/badge.svg)](https://github.com/Kotryos/sql-hierarchy-models/actions/workflows/ci.yml)

This exploration project compares three relational models for hierarchical data using Java 21, Spring Boot, jOOQ, Flyway, JUnit 5, Database Rider, and Testcontainers.

A *structural* comparison: each model implements the same operations with equivalent, correct queries shown side by side. It is not a benchmark, and it stores a single tree (a forest would add a tree-id dimension to every query and is out of scope).

## Models

All repositories implement the same operations.

Reads: find leaf nodes, find a node path, find a flattened subtree, find node depth.

Writes (leaf-scoped): insert a child node (a new leaf), delete a leaf node. A new node has no children, so an insert can only add a leaf, and deleting a leaf avoids deciding what happens to a node's children — keeping both leaf-scoped keeps each model's algorithm small while still showing its write cost.

Not implemented yet:

- **find direct children / parent** (depth-1 reads) — trivial for adjacency list and closure table, awkward for nested set.
- **delete a subtree** — needs descendant collection and foreign-key-safe deletes.
- **move / re-parent** — the hardest mutation, where the nested set model is most expensive.
- **update a label** — skipped; a plain `UPDATE` identical in every model.

| Model | Storage shape | Read strengths | Write tradeoff |
| --- | --- | --- | --- |
| Nested Set | One row per node with `lft` and `rgt` bounds. | Fast paths, subtrees, leaf checks, and depths with range queries. | Inserting a node shifts the bounds of many other rows. |
| Adjacency List | One row per node with `parent_id`. | Simple schema and simple writes. | Paths and subtrees need recursive queries. |
| Closure Table | One node table plus one ancestor/descendant path table. | Fast paths, subtrees, and depths with joins. | Writes must maintain extra path rows. |

## Writes and Transactions

Writes run in a transaction (`@Transactional`); Spring Boot supplies the transaction manager and jOOQ joins it through a transaction-aware data source. Reads are single statements without an explicit transaction.

The project does not set an isolation level, so writes use each database's default — `READ COMMITTED` on PostgreSQL, `REPEATABLE READ` on MySQL (InnoDB). This only matters under concurrent transactions, which the tests do not exercise.

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

Nested Set encodes this tree as containment bounds:

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
id  label
1   Vehicles
2   Motor Vehicles
4   Cars
8   Convertibles

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
