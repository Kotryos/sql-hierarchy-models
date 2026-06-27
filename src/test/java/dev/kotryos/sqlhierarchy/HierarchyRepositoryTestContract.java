package dev.kotryos.sqlhierarchy;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.spring.api.DBRider;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DBRider
@DBUnit(cacheConnection = false, cacheTableNames = false, caseSensitiveTableNames = true)
@SpringBootTest
public abstract class HierarchyRepositoryTestContract {

    private static final long ROOT_ID = 1L;
    private static final long LEAF_ID = 8L;
    private static final long NON_EXISTING_ID = 10L;
    private static final List<Long> VEHICLE_LEAF_IDS = List.of(5L, 6L, 7L, 8L, 9L);
    private static final List<Long> VEHICLE_LEAF_PATH_IDS = List.of(1L, 2L, 4L);
    private static final List<Long> VEHICLE_ROOT_SUBTREE_IDS = List.of(2L, 4L, 8L, 9L, 5L, 3L, 6L, 7L);
    private static final int VEHICLE_LEAF_DEPTH = 3;

    protected abstract HierarchyNodeRepository<? extends HierarchyNode> repository();

    protected void assertFindLeafNodesWhenNodesExistReturnsLeafNodes() {
        assertThat(findLeafIds()).containsExactlyInAnyOrderElementsOf(VEHICLE_LEAF_IDS);
    }

    protected void assertFindLeafNodesWhenNoNodesExistReturnsEmptyList() {
        assertThat(findLeafIds()).isEmpty();
    }

    protected void assertFindLeafNodesWhenOnlyRootExistsReturnsRoot() {
        assertThat(findLeafIds()).containsExactly(ROOT_ID);
    }

    protected void assertFindPathWhenLeafNodeIdPassedReturnsPath() {
        assertThat(findPathIds(LEAF_ID)).containsExactlyElementsOf(VEHICLE_LEAF_PATH_IDS);
    }

    protected void assertFindPathWhenRootNodeIdPassedReturnsEmptyList() {
        assertThat(findPathIds(ROOT_ID)).isEmpty();
    }

    protected void assertFindPathWhenNonExistingNodeIdPassedReturnsEmptyList() {
        assertThat(findPathIds(NON_EXISTING_ID)).isEmpty();
    }

    protected void assertFindSubtreeWhenLeafNodeIdPassedReturnsEmptyList() {
        assertThat(findSubtreeIds(LEAF_ID)).isEmpty();
    }

    protected void assertFindSubtreeWhenRootNodeIdPassedReturnsFlattenedSubtree() {
        assertThat(findSubtreeIds(ROOT_ID)).containsExactlyElementsOf(VEHICLE_ROOT_SUBTREE_IDS);
    }

    protected void assertFindSubtreeWhenNonExistingNodeIdPassedReturnsEmptyList() {
        assertThat(findSubtreeIds(NON_EXISTING_ID)).isEmpty();
    }

    protected void assertFindDepthWhenLeafNodeIdPassedReturnsDepth() {
        assertThat(findDepth(LEAF_ID)).isEqualTo(VEHICLE_LEAF_DEPTH);
    }

    protected void assertFindDepthWhenRootNodeIdPassedReturnsZero() {
        assertThat(findDepth(ROOT_ID)).isZero();
    }

    protected void assertFindDepthWhenNonExistingNodeIdPassedReturnsZero() {
        assertThat(findDepth(NON_EXISTING_ID)).isZero();
    }

    private List<Long> findLeafIds() {
        return repository().findLeafNodes().stream()
                .map(HierarchyNode::id)
                .toList();
    }

    private List<Long> findPathIds(long nodeId) {
        return repository().findPath(nodeId).stream()
                .map(HierarchyNode::id)
                .toList();
    }

    private List<Long> findSubtreeIds(long rootId) {
        return repository().findSubtree(rootId).stream()
                .map(HierarchyNode::id)
                .toList();
    }

    private int findDepth(long nodeId) {
        return repository().findDepth(nodeId);
    }
}

