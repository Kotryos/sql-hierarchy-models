package dev.kotryos.sqlhierarchy.adjacencylist;

import dev.kotryos.sqlhierarchy.HierarchyNodeRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.util.List;

import static dev.kotryos.sqlhierarchy.jooq.Tables.ADJACENCY_LIST_NODE;
import static org.jooq.impl.DSL.notExists;
import static org.jooq.impl.DSL.selectOne;

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
         * SELECT node.id, node.label, node.parent_id, node.sort_order
         * FROM adjacency_list_node node
         * WHERE NOT EXISTS (
         *     SELECT 1
         *     FROM adjacency_list_node child
         *     WHERE child.parent_id = node.id
         * )
         */
        return dsl.select(node.ID, node.LABEL, node.PARENT_ID, node.SORT_ORDER)
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
         * Walk from the selected node upward through parent_id until the root.
         * PostgreSQL and MySQL 8 use WITH RECURSIVE for this recursive CTE.
         */
        return dsl.resultQuery(
                        """
                        WITH RECURSIVE path_nodes (id, label, parent_id, sort_order, depth) AS (
                            SELECT id, label, parent_id, sort_order, 0
                            FROM adjacency_list_node
                            WHERE id = ?
                            UNION ALL
                            SELECT parent.id, parent.label, parent.parent_id, parent.sort_order, path_nodes.depth + 1
                            FROM adjacency_list_node parent
                            JOIN path_nodes ON path_nodes.parent_id = parent.id
                        )
                        SELECT id, label, parent_id, sort_order
                        FROM path_nodes
                        WHERE depth > 0
                        ORDER BY depth DESC
                        """,
                        nodeId
                )
                .fetch(this::mapNode);
    }

    @Override
    public List<AdjacencyListNode> findSubtree(long rootId) {
        /*
         * Walk from the selected root downward by repeatedly joining children.
         * sort_order is a stored preorder value used only for stable output.
         */
        return dsl.resultQuery(
                        """
                        WITH RECURSIVE subtree_nodes (id, label, parent_id, sort_order, depth) AS (
                            SELECT id, label, parent_id, sort_order, 0
                            FROM adjacency_list_node
                            WHERE id = ?
                            UNION ALL
                            SELECT child.id, child.label, child.parent_id, child.sort_order, subtree_nodes.depth + 1
                            FROM adjacency_list_node child
                            JOIN subtree_nodes ON child.parent_id = subtree_nodes.id
                        )
                        SELECT id, label, parent_id, sort_order
                        FROM subtree_nodes
                        WHERE depth > 0
                        ORDER BY sort_order
                        """,
                        rootId
                )
                .fetch(this::mapNode);
    }

    @Override
    public int findDepth(long nodeId) {
        /*
         * Same upward recursive CTE as findPath, but returns the maximum depth.
         * Missing nodes produce no rows, so the repository maps NULL to 0.
         */
        Integer depth = dsl.resultQuery(
                        """
                        WITH RECURSIVE path_nodes (id, parent_id, depth) AS (
                            SELECT id, parent_id, 0
                            FROM adjacency_list_node
                            WHERE id = ?
                            UNION ALL
                            SELECT parent.id, parent.parent_id, path_nodes.depth + 1
                            FROM adjacency_list_node parent
                            JOIN path_nodes ON path_nodes.parent_id = parent.id
                        )
                        SELECT MAX(depth) AS depth
                        FROM path_nodes
                        """,
                        nodeId
                )
                .fetchOne("depth", Integer.class);

        return depth == null ? 0 : depth;
    }

    private AdjacencyListNode mapNode(Record record, dev.kotryos.sqlhierarchy.jooq.tables.AdjacencyListNode node) {
        return new AdjacencyListNode(
                record.get(node.ID),
                record.get(node.LABEL),
                record.get(node.PARENT_ID),
                record.get(node.SORT_ORDER)
        );
    }

    private AdjacencyListNode mapNode(Record record) {
        return new AdjacencyListNode(
                record.get("id", Long.class),
                record.get("label", String.class),
                record.get("parent_id", Long.class),
                record.get("sort_order", Integer.class)
        );
    }
}
