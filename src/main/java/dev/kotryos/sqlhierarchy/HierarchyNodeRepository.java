package dev.kotryos.sqlhierarchy;

import java.util.List;

public interface HierarchyNodeRepository<N extends HierarchyNode> {

    List<N> findLeafNodes();

    List<N> findPath(long nodeId);

    List<N> findSubtree(long rootId);

    int findDepth(long nodeId);
}
