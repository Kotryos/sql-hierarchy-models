# Introduction
The Nested Set Model is an approach to storing hierarchical data structures in relational databases.
It is an alternative to more common Adjacency List Model.
The Nested Set Model has a clear advantage over Adjacency List Model for operations like finding path for a node, retrieving the whole tree, finding depth of a node etc.
For example, in Adjacency List it is impossible to find a path for a node with pure SQL without knowing the depth of that node.
On the other hand, create/delete operations in Nested Set Model are much more complicated, since these operations cause the recalculation of "left" and "right" values for the whole tree.
As a consequence to the aforementioned, the write lock on the table is necessary for create/delete, which would require a stored procedure or SERIALIZABLE isolation level (may lead to heavy performance problems).

Summing up, the Nested Set Model is suitable for systems where different reads are frequent while writes are not needed or usually occur in batch at predefined time.

This project demonstrates Spring Data JPA powered implementation of different reads in the Nested Set Model.

# Implemented operations

 - retrieval of all leaf nodes
 - path retrieval
 - retrieval of child nodes for a node

# How to run
Run `mvn test` from the project root (requires Maven 3).