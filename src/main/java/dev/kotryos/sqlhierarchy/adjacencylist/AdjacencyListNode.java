package dev.kotryos.sqlhierarchy.adjacencylist;

import dev.kotryos.sqlhierarchy.HierarchyNode;

public record AdjacencyListNode(Long id, String label, Long parentId) implements HierarchyNode {
}
