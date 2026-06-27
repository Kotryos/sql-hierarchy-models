package dev.kotryos.sqlhierarchy.adjacencylist;

import com.github.database.rider.core.api.dataset.DataSet;
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

    abstract static class TestContract extends HierarchyRepositoryTestContract {

        @DataSet(value = VEHICLES_DATASET, cleanBefore = true, disableConstraints = true, skipCleaningFor = "flyway_schema_history")
        @Test
        void findLeafNodesWhenNodesExistReturnsLeafNodes() {
            assertFindLeafNodesWhenNodesExistReturnsLeafNodes();
        }

        @DataSet(value = EMPTY_DATASET, cleanBefore = true, disableConstraints = true, skipCleaningFor = "flyway_schema_history")
        @Test
        void findLeafNodesWhenNoNodesExistReturnsEmptyList() {
            assertFindLeafNodesWhenNoNodesExistReturnsEmptyList();
        }

        @DataSet(value = VEHICLES_DATASET, cleanBefore = true, disableConstraints = true, skipCleaningFor = "flyway_schema_history")
        @Test
        void findPathWhenLeafNodeIdPassedReturnsPath() {
            assertFindPathWhenLeafNodeIdPassedReturnsPath();
        }

        @DataSet(value = VEHICLES_DATASET, cleanBefore = true, disableConstraints = true, skipCleaningFor = "flyway_schema_history")
        @Test
        void findPathWhenRootNodeIdPassedReturnsEmptyList() {
            assertFindPathWhenRootNodeIdPassedReturnsEmptyList();
        }

        @DataSet(value = VEHICLES_DATASET, cleanBefore = true, disableConstraints = true, skipCleaningFor = "flyway_schema_history")
        @Test
        void findPathWhenNonExistingNodeIdPassedReturnsEmptyList() {
            assertFindPathWhenNonExistingNodeIdPassedReturnsEmptyList();
        }

        @DataSet(value = VEHICLES_DATASET, cleanBefore = true, disableConstraints = true, skipCleaningFor = "flyway_schema_history")
        @Test
        void findSubtreeWhenLeafNodeIdPassedReturnsEmptyList() {
            assertFindSubtreeWhenLeafNodeIdPassedReturnsEmptyList();
        }

        @DataSet(value = VEHICLES_DATASET, cleanBefore = true, disableConstraints = true, skipCleaningFor = "flyway_schema_history")
        @Test
        void findSubtreeWhenRootNodeIdPassedReturnsFlattenedSubtree() {
            assertFindSubtreeWhenRootNodeIdPassedReturnsFlattenedSubtree();
        }

        @DataSet(value = VEHICLES_DATASET, cleanBefore = true, disableConstraints = true, skipCleaningFor = "flyway_schema_history")
        @Test
        void findSubtreeWhenNonExistingNodeIdPassedReturnsEmptyList() {
            assertFindSubtreeWhenNonExistingNodeIdPassedReturnsEmptyList();
        }

        @DataSet(value = VEHICLES_DATASET, cleanBefore = true, disableConstraints = true, skipCleaningFor = "flyway_schema_history")
        @Test
        void findDepthWhenLeafNodeIdPassedReturnsDepth() {
            assertFindDepthWhenLeafNodeIdPassedReturnsDepth();
        }

        @DataSet(value = VEHICLES_DATASET, cleanBefore = true, disableConstraints = true, skipCleaningFor = "flyway_schema_history")
        @Test
        void findDepthWhenRootNodeIdPassedReturnsZero() {
            assertFindDepthWhenRootNodeIdPassedReturnsZero();
        }

        @DataSet(value = VEHICLES_DATASET, cleanBefore = true, disableConstraints = true, skipCleaningFor = "flyway_schema_history")
        @Test
        void findDepthWhenNonExistingNodeIdPassedReturnsZero() {
            assertFindDepthWhenNonExistingNodeIdPassedReturnsZero();
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
