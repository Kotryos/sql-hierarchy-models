package dev.kotryos.sqlhierarchy.adjacencylist;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import dev.kotryos.sqlhierarchy.HierarchyNodeRepository;
import dev.kotryos.sqlhierarchy.HierarchyRepositoryTestContract;
import dev.kotryos.sqlhierarchy.TestDatabases;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

class AdjacencyListNodeRepositoryTest {

    private static final String VEHICLES_DATASET = "datasets/adjacency-list/vehicles.yml";
    private static final String EMPTY_DATASET = "datasets/adjacency-list/empty.yml";
    private static final String SINGLE_NODE_DATASET = "datasets/adjacency-list/single-node.yml";
    private static final String EXPECTED_AFTER_INSERT = "datasets/adjacency-list/expected-after-insert.yml";
    private static final String EXPECTED_AFTER_DELETE = "datasets/adjacency-list/expected-after-delete.yml";

    @DataSet(value = VEHICLES_DATASET, cleanBefore = true, disableConstraints = true, skipCleaningFor = "flyway_schema_history")
    abstract static class TestContract extends HierarchyRepositoryTestContract {

        @Test
        void findLeafNodesWhenNodesExistReturnsLeafNodes() {
            assertFindLeafNodesWhenNodesExistReturnsLeafNodes();
        }

        @DataSet(value = EMPTY_DATASET, cleanBefore = true, disableConstraints = true, skipCleaningFor = "flyway_schema_history")
        @Test
        void findLeafNodesWhenNoNodesExistReturnsEmptyList() {
            assertFindLeafNodesWhenNoNodesExistReturnsEmptyList();
        }

        @DataSet(value = SINGLE_NODE_DATASET, cleanBefore = true, disableConstraints = true, skipCleaningFor = "flyway_schema_history")
        @Test
        void findLeafNodesWhenOnlyRootExistsReturnsRoot() {
            assertFindLeafNodesWhenOnlyRootExistsReturnsRoot();
        }

        @Test
        void findPathWhenLeafNodeIdPassedReturnsPath() {
            assertFindPathWhenLeafNodeIdPassedReturnsPath();
        }

        @Test
        void findPathWhenRootNodeIdPassedReturnsEmptyList() {
            assertFindPathWhenRootNodeIdPassedReturnsEmptyList();
        }

        @Test
        void findPathWhenNonExistingNodeIdPassedReturnsEmptyList() {
            assertFindPathWhenNonExistingNodeIdPassedReturnsEmptyList();
        }

        @Test
        void findSubtreeWhenLeafNodeIdPassedReturnsEmptyList() {
            assertFindSubtreeWhenLeafNodeIdPassedReturnsEmptyList();
        }

        @Test
        void findSubtreeWhenRootNodeIdPassedReturnsFlattenedSubtree() {
            assertFindSubtreeWhenRootNodeIdPassedReturnsFlattenedSubtree();
        }

        @Test
        void findSubtreeWhenNonExistingNodeIdPassedReturnsEmptyList() {
            assertFindSubtreeWhenNonExistingNodeIdPassedReturnsEmptyList();
        }

        @Test
        void findDepthWhenLeafNodeIdPassedReturnsDepth() {
            assertFindDepthWhenLeafNodeIdPassedReturnsDepth();
        }

        @Test
        void findDepthWhenRootNodeIdPassedReturnsZero() {
            assertFindDepthWhenRootNodeIdPassedReturnsZero();
        }

        @Test
        void findDepthWhenNonExistingNodeIdPassedReturnsZero() {
            assertFindDepthWhenNonExistingNodeIdPassedReturnsZero();
        }

        @ExpectedDataSet(value = EXPECTED_AFTER_INSERT, orderBy = "id")
        @Test
        void insertChildAddsLeafUnderParent() {
            insertChild();
        }

        @ExpectedDataSet(value = EXPECTED_AFTER_DELETE, orderBy = "id")
        @Test
        void deleteLeafRemovesNode() {
            deleteLeaf();
        }
    }

    @Nested
    class MySql extends TestContract {

        @Autowired
        private AdjacencyListNodeRepository nodeRepository;

        @DynamicPropertySource
        static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
            TestDatabases.register(TestDatabases.Database.MYSQL, registry);
        }

        @Override
        protected HierarchyNodeRepository<AdjacencyListNode> repository() {
            return nodeRepository;
        }
    }

    @Nested
    class Postgres extends TestContract {

        @Autowired
        private AdjacencyListNodeRepository nodeRepository;

        @DynamicPropertySource
        static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
            TestDatabases.register(TestDatabases.Database.POSTGRES, registry);
        }

        @Override
        protected HierarchyNodeRepository<AdjacencyListNode> repository() {
            return nodeRepository;
        }
    }
}
