package dev.kotryos.sqlhierarchy.nestedset;

import com.github.database.rider.core.api.dataset.DataSet;
import dev.kotryos.sqlhierarchy.HierarchyNodeRepository;
import dev.kotryos.sqlhierarchy.HierarchyRepositoryTestContract;
import dev.kotryos.sqlhierarchy.TestDatabases;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

class NestedSetNodeRepositoryTest {

    private static final String VEHICLES_DATASET = "datasets/nested-set/vehicles.yml";
    private static final String EMPTY_DATASET = "datasets/nested-set/empty.yml";
    private static final String SINGLE_NODE_DATASET = "datasets/nested-set/single-node.yml";

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
    }

    @Nested
    class MySql extends TestContract {

        @Autowired
        private NestedSetNodeRepository nodeRepository;

        @DynamicPropertySource
        static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
            TestDatabases.register(TestDatabases.Database.MYSQL, registry);
        }

        @Override
        protected HierarchyNodeRepository<NestedSetNode> repository() {
            return nodeRepository;
        }
    }

    @Nested
    class Postgres extends TestContract {

        @Autowired
        private NestedSetNodeRepository nodeRepository;

        @DynamicPropertySource
        static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
            TestDatabases.register(TestDatabases.Database.POSTGRES, registry);
        }

        @Override
        protected HierarchyNodeRepository<NestedSetNode> repository() {
            return nodeRepository;
        }
    }
}
