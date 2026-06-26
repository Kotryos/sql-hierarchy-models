package dev.kotryos.sqlhierarchy.nestedset;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static dev.kotryos.sqlhierarchy.jooq.Tables.NESTED_SET_NODE;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
abstract class NestedSetNodeRepositoryContract {

    private static final long ROOT_ID = 1L;
    private static final long LEAF_ID = 8L;
    private static final long NON_EXISTING_ID = 10L;

    @Autowired
    private DSLContext dsl;

    @Autowired
    private NestedSetNodeRepository nodeRepository;

    @BeforeEach
    void clearNodes() {
        dsl.deleteFrom(NESTED_SET_NODE).execute();
    }

    @Test
    void findLeafNodesWhenNodesExistReturnsLeafNodes() {
        insertInitialData();

        List<NestedSetNode> leafNodes = nodeRepository.findLeafNodes();

        assertThat(leafNodes).containsExactlyInAnyOrder(
                new NestedSetNode(8L, "Convertibles", 4, 5),
                new NestedSetNode(9L, "Minivans", 6, 7),
                new NestedSetNode(5L, "Buses", 9, 10),
                new NestedSetNode(6L, "Trains", 13, 14),
                new NestedSetNode(7L, "Trams", 15, 16)
        );
    }

    @Test
    void findLeafNodesWhenNoNodesExistReturnsEmptyList() {
        List<NestedSetNode> leafNodes = nodeRepository.findLeafNodes();

        assertThat(leafNodes).isEmpty();
    }

    @Test
    void findPathWhenLeafNodeIdPassedReturnsPath() {
        insertInitialData();

        List<NestedSetNode> path = nodeRepository.findPath(LEAF_ID);

        assertThat(path).containsExactly(
                new NestedSetNode(1L, "Vehicles", 1, 18),
                new NestedSetNode(2L, "Motor Vehicles", 2, 11),
                new NestedSetNode(4L, "Cars", 3, 8)
        );
    }

    @Test
    void findPathWhenRootNodeIdPassedReturnsEmptyList() {
        insertInitialData();

        List<NestedSetNode> path = nodeRepository.findPath(ROOT_ID);

        assertThat(path).isEmpty();
    }

    @Test
    void findPathWhenNonExistingNodeIdPassedReturnsEmptyList() {
        insertInitialData();

        List<NestedSetNode> path = nodeRepository.findPath(NON_EXISTING_ID);

        assertThat(path).isEmpty();
    }

    @Test
    void findSubtreeWhenLeafNodeIdPassedReturnsEmptyList() {
        insertInitialData();

        List<NestedSetNode> subtree = nodeRepository.findSubtree(LEAF_ID);

        assertThat(subtree).isEmpty();
    }

    @Test
    void findSubtreeWhenRootNodeIdPassedReturnsFlattenedSubtree() {
        insertInitialData();

        List<NestedSetNode> subtree = nodeRepository.findSubtree(ROOT_ID);

        assertThat(subtree).containsExactly(
                new NestedSetNode(2L, "Motor Vehicles", 2, 11),
                new NestedSetNode(4L, "Cars", 3, 8),
                new NestedSetNode(8L, "Convertibles", 4, 5),
                new NestedSetNode(9L, "Minivans", 6, 7),
                new NestedSetNode(5L, "Buses", 9, 10),
                new NestedSetNode(3L, "Rail Vehicles", 12, 17),
                new NestedSetNode(6L, "Trains", 13, 14),
                new NestedSetNode(7L, "Trams", 15, 16)
        );
    }

    @Test
    void findSubtreeWhenNonExistingNodeIdPassedReturnsEmptyList() {
        insertInitialData();

        List<NestedSetNode> subtree = nodeRepository.findSubtree(NON_EXISTING_ID);

        assertThat(subtree).isEmpty();
    }

    @Test
    void findDepthWhenLeafNodeIdPassedReturnsDepth() {
        insertInitialData();

        int depth = nodeRepository.findDepth(LEAF_ID);

        assertThat(depth).isEqualTo(3);
    }

    @Test
    void findDepthWhenRootNodeIdPassedReturnsZero() {
        insertInitialData();

        int depth = nodeRepository.findDepth(ROOT_ID);

        assertThat(depth).isZero();
    }

    @Test
    void findDepthWhenNonExistingNodeIdPassedReturnsZero() {
        insertInitialData();

        int depth = nodeRepository.findDepth(NON_EXISTING_ID);

        assertThat(depth).isZero();
    }

    private void insertInitialData() {
        insertNode(1, "Vehicles", 1, 18);
        insertNode(2, "Motor Vehicles", 2, 11);
        insertNode(3, "Rail Vehicles", 12, 17);
        insertNode(4, "Cars", 3, 8);
        insertNode(5, "Buses", 9, 10);
        insertNode(6, "Trains", 13, 14);
        insertNode(7, "Trams", 15, 16);
        insertNode(8, "Convertibles", 4, 5);
        insertNode(9, "Minivans", 6, 7);
    }

    private void insertNode(long id, String label, int left, int right) {
        dsl.insertInto(NESTED_SET_NODE)
                .columns(
                        NESTED_SET_NODE.ID,
                        NESTED_SET_NODE.LABEL,
                        NESTED_SET_NODE.LFT,
                        NESTED_SET_NODE.RGT
                )
                .values(id, label, left, right)
                .execute();
    }
}
