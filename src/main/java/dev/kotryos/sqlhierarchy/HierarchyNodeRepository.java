package dev.kotryos.sqlhierarchy;

import java.util.List;

public interface HierarchyNodeRepository<N extends HierarchyNode> {

    List<N> findLeafNodes();

    List<N> findPath(long nodeId);

    List<N> findSubtree(long rootId);

    int findDepth(long nodeId);

    /**
     * Adds a new leaf node with the given id and label as the last child of {@code parentId}.
     * Each model keeps its own structure up to date (parent link, nested-set bounds, or closure
     * paths) so that the same read operations stay correct. This is where the write cost of each
     * model becomes visible.
     */
    void insertChild(long parentId, long newId, String label);

    /**
     * Removes a leaf node (one with no children) by id. Each model cleans up its own structure:
     * the parent link simply disappears, nested-set bounds close the gap left behind, and the
     * closure paths for the node are deleted.
     */
    void deleteLeaf(long nodeId);
}
