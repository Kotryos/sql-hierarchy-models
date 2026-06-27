package dev.kotryos.sqlhierarchy.nestedset;

import dev.kotryos.sqlhierarchy.HierarchyNode;

public record NestedSetNode(Long id, String label, int left, int right) implements HierarchyNode {
}
