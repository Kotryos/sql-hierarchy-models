package dev.kotryos.sqlhierarchy.closuretable;

import dev.kotryos.sqlhierarchy.HierarchyNode;

public record ClosureTableNode(Long id, String label) implements HierarchyNode {
}
