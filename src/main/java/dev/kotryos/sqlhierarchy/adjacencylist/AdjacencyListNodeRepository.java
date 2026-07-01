package dev.kotryos.sqlhierarchy.adjacencylist;

import dev.kotryos.sqlhierarchy.HierarchyNodeRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dev.kotryos.sqlhierarchy.jooq.Tables.ADJACENCY_LIST_NODE;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.notExists;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.selectOne;
import static org.jooq.impl.DSL.table;

@Repository
public class AdjacencyListNodeRepository implements HierarchyNodeRepository<AdjacencyListNode> {

    private final DSLContext dsl;

    public AdjacencyListNodeRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<AdjacencyListNode> findLeafNodes() {
        var node = ADJACENCY_LIST_NODE.as("node");
        var child = ADJACENCY_LIST_NODE.as("child");

        /*
         * SELECT node.id, node.label, node.parent_id
         * FROM adjacency_list_node node
         * WHERE NOT EXISTS (
         *     SELECT 1
         *     FROM adjacency_list_node child
         *     WHERE child.parent_id = node.id
         * )
         */
        return dsl.select(node.ID, node.LABEL, node.PARENT_ID)
                .from(node)
                .where(notExists(
                        selectOne()
                                .from(child)
                                .where(child.PARENT_ID.eq(node.ID))
                ))
                .fetch(record -> mapNode(record, node));
    }

    @Override
    public List<AdjacencyListNode> findPath(long nodeId) {
        /*
         * Walk upward through parent_id to the root with a recursive CTE.
         *
         * WITH RECURSIVE path_nodes (id, label, parent_id, depth) AS (
         *     SELECT id, label, parent_id, 0
         *     FROM adjacency_list_node
         *     WHERE id = :nodeId
         *     UNION ALL
         *     SELECT parent.id, parent.label, parent.parent_id, path_nodes.depth + 1
         *     FROM adjacency_list_node parent
         *     JOIN path_nodes ON path_nodes.parent_id = parent.id
         * )
         * SELECT id, label, parent_id
         * FROM path_nodes
         * WHERE depth > 0
         * ORDER BY depth DESC
         */
        var parent = ADJACENCY_LIST_NODE.as("parent");
        var id = field(name("path_nodes", "id"), Long.class);
        var label = field(name("path_nodes", "label"), String.class);
        var parentId = field(name("path_nodes", "parent_id"), Long.class);
        var depth = field(name("path_nodes", "depth"), Integer.class);

        var pathNodes = name("path_nodes").fields("id", "label", "parent_id", "depth").as(
                select(ADJACENCY_LIST_NODE.ID, ADJACENCY_LIST_NODE.LABEL, ADJACENCY_LIST_NODE.PARENT_ID, inline(0))
                        .from(ADJACENCY_LIST_NODE)
                        .where(ADJACENCY_LIST_NODE.ID.eq(nodeId))
                        .unionAll(
                                select(parent.ID, parent.LABEL, parent.PARENT_ID, depth.plus(1))
                                        .from(parent)
                                        .join(table(name("path_nodes"))).on(parentId.eq(parent.ID))));

        return dsl.withRecursive(pathNodes)
                .select(id, label, parentId)
                .from(pathNodes)
                .where(depth.gt(0))
                .orderBy(depth.desc())
                .fetch(this::mapNode);
    }

    @Override
    public List<AdjacencyListNode> findSubtree(long rootId) {
        /*
         * Walk downward by repeatedly joining children with a recursive CTE.
         * The flattened subtree is a set; ordering by id is only for stable output.
         *
         * WITH RECURSIVE subtree_nodes (id, label, parent_id, depth) AS (
         *     SELECT id, label, parent_id, 0
         *     FROM adjacency_list_node
         *     WHERE id = :rootId
         *     UNION ALL
         *     SELECT child.id, child.label, child.parent_id, subtree_nodes.depth + 1
         *     FROM adjacency_list_node child
         *     JOIN subtree_nodes ON child.parent_id = subtree_nodes.id
         * )
         * SELECT id, label, parent_id
         * FROM subtree_nodes
         * WHERE depth > 0
         * ORDER BY id
         */
        var child = ADJACENCY_LIST_NODE.as("child");
        var id = field(name("subtree_nodes", "id"), Long.class);
        var label = field(name("subtree_nodes", "label"), String.class);
        var parentId = field(name("subtree_nodes", "parent_id"), Long.class);
        var depth = field(name("subtree_nodes", "depth"), Integer.class);

        var subtreeNodes = name("subtree_nodes").fields("id", "label", "parent_id", "depth").as(
                select(ADJACENCY_LIST_NODE.ID, ADJACENCY_LIST_NODE.LABEL, ADJACENCY_LIST_NODE.PARENT_ID, inline(0))
                        .from(ADJACENCY_LIST_NODE)
                        .where(ADJACENCY_LIST_NODE.ID.eq(rootId))
                        .unionAll(
                                select(child.ID, child.LABEL, child.PARENT_ID, depth.plus(1))
                                        .from(child)
                                        .join(table(name("subtree_nodes"))).on(child.PARENT_ID.eq(id))));

        return dsl.withRecursive(subtreeNodes)
                .select(id, label, parentId)
                .from(subtreeNodes)
                .where(depth.gt(0))
                .orderBy(id)
                .fetch(this::mapNode);
    }

    @Override
    public int findDepth(long nodeId) {
        /*
         * Same upward recursive CTE as findPath, returning the maximum depth.
         * Missing nodes produce no rows, so the repository maps NULL to 0.
         *
         * WITH RECURSIVE path_nodes (id, parent_id, depth) AS (
         *     SELECT id, parent_id, 0
         *     FROM adjacency_list_node
         *     WHERE id = :nodeId
         *     UNION ALL
         *     SELECT parent.id, parent.parent_id, path_nodes.depth + 1
         *     FROM adjacency_list_node parent
         *     JOIN path_nodes ON path_nodes.parent_id = parent.id
         * )
         * SELECT MAX(depth)
         * FROM path_nodes
         */
        var parent = ADJACENCY_LIST_NODE.as("parent");
        var parentId = field(name("path_nodes", "parent_id"), Long.class);
        var depth = field(name("path_nodes", "depth"), Integer.class);

        var pathNodes = name("path_nodes").fields("id", "parent_id", "depth").as(
                select(ADJACENCY_LIST_NODE.ID, ADJACENCY_LIST_NODE.PARENT_ID, inline(0))
                        .from(ADJACENCY_LIST_NODE)
                        .where(ADJACENCY_LIST_NODE.ID.eq(nodeId))
                        .unionAll(
                                select(parent.ID, parent.PARENT_ID, depth.plus(1))
                                        .from(parent)
                                        .join(table(name("path_nodes"))).on(parentId.eq(parent.ID))));

        Integer maxDepth = dsl.withRecursive(pathNodes)
                .select(max(depth))
                .from(pathNodes)
                .fetchOne(0, Integer.class);

        return maxDepth == null ? 0 : maxDepth;
    }

    @Override
    @Transactional
    public void insertChild(long parentId, long newId, String label) {
        // Adjacency list writes are the cheapest: a single row that points at its parent.

        /*
         * INSERT INTO adjacency_list_node (id, label, parent_id)
         * VALUES (:newId, :label, :parentId)
         */
        dsl.insertInto(ADJACENCY_LIST_NODE)
                .columns(ADJACENCY_LIST_NODE.ID, ADJACENCY_LIST_NODE.LABEL, ADJACENCY_LIST_NODE.PARENT_ID)
                .values(newId, label, parentId)
                .execute();
    }

    @Override
    @Transactional
    public void deleteLeaf(long nodeId) {
        // Nothing points at a leaf, so a single delete is all it takes.

        /*
         * DELETE FROM adjacency_list_node
         * WHERE id = :nodeId
         */
        dsl.deleteFrom(ADJACENCY_LIST_NODE)
                .where(ADJACENCY_LIST_NODE.ID.eq(nodeId))
                .execute();
    }

    private AdjacencyListNode mapNode(Record record, dev.kotryos.sqlhierarchy.jooq.tables.AdjacencyListNode node) {
        return new AdjacencyListNode(
                record.get(node.ID),
                record.get(node.LABEL),
                record.get(node.PARENT_ID)
        );
    }

    private AdjacencyListNode mapNode(Record record) {
        return new AdjacencyListNode(
                record.get("id", Long.class),
                record.get("label", String.class),
                record.get("parent_id", Long.class)
        );
    }
}
