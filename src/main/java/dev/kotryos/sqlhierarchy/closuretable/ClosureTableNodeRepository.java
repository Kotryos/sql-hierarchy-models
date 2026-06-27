package dev.kotryos.sqlhierarchy.closuretable;

import dev.kotryos.sqlhierarchy.HierarchyNodeRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.util.List;

import static dev.kotryos.sqlhierarchy.jooq.Tables.CLOSURE_TABLE_NODE;
import static dev.kotryos.sqlhierarchy.jooq.Tables.CLOSURE_TABLE_PATH;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.notExists;
import static org.jooq.impl.DSL.selectOne;

@Repository
public class ClosureTableNodeRepository implements HierarchyNodeRepository<ClosureTableNode> {

    private final DSLContext dsl;

    public ClosureTableNodeRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<ClosureTableNode> findLeafNodes() {
        var node = CLOSURE_TABLE_NODE.as("node");
        var childPath = CLOSURE_TABLE_PATH.as("child_path");

        /*
         * SELECT node.id, node.label, node.sort_order
         * FROM closure_table_node node
         * WHERE NOT EXISTS (
         *     SELECT 1
         *     FROM closure_table_path child_path
         *     WHERE child_path.ancestor_id = node.id
         *       AND child_path.depth = 1
         * )
         */
        return dsl.select(node.ID, node.LABEL, node.SORT_ORDER)
                .from(node)
                .where(notExists(
                        selectOne()
                                .from(childPath)
                                .where(childPath.ANCESTOR_ID.eq(node.ID))
                                .and(childPath.DEPTH.eq(1))
                ))
                .fetch(record -> mapNode(record, node));
    }

    @Override
    public List<ClosureTableNode> findPath(long nodeId) {
        var node = CLOSURE_TABLE_NODE.as("node");
        var path = CLOSURE_TABLE_PATH.as("path");

        /*
         * SELECT node.id, node.label, node.sort_order
         * FROM closure_table_path path
         * JOIN closure_table_node node ON node.id = path.ancestor_id
         * WHERE path.descendant_id = :nodeId
         *   AND path.depth > 0
         * ORDER BY path.depth DESC
         */
        return dsl.select(node.ID, node.LABEL, node.SORT_ORDER)
                .from(path)
                .join(node).on(node.ID.eq(path.ANCESTOR_ID))
                .where(path.DESCENDANT_ID.eq(nodeId))
                .and(path.DEPTH.gt(0))
                .orderBy(path.DEPTH.desc())
                .fetch(record -> mapNode(record, node));
    }

    @Override
    public List<ClosureTableNode> findSubtree(long rootId) {
        var node = CLOSURE_TABLE_NODE.as("node");
        var path = CLOSURE_TABLE_PATH.as("path");

        /*
         * SELECT node.id, node.label, node.sort_order
         * FROM closure_table_path path
         * JOIN closure_table_node node ON node.id = path.descendant_id
         * WHERE path.ancestor_id = :rootId
         *   AND path.depth > 0
         * ORDER BY node.sort_order
         */
        return dsl.select(node.ID, node.LABEL, node.SORT_ORDER)
                .from(path)
                .join(node).on(node.ID.eq(path.DESCENDANT_ID))
                .where(path.ANCESTOR_ID.eq(rootId))
                .and(path.DEPTH.gt(0))
                .orderBy(node.SORT_ORDER)
                .fetch(record -> mapNode(record, node));
    }

    @Override
    public int findDepth(long nodeId) {
        var path = CLOSURE_TABLE_PATH.as("path");

        /*
         * SELECT MAX(path.depth)
         * FROM closure_table_path path
         * WHERE path.descendant_id = :nodeId
         */
        Integer depth = dsl.select(max(path.DEPTH))
                .from(path)
                .where(path.DESCENDANT_ID.eq(nodeId))
                .fetchOne(0, Integer.class);

        return depth == null ? 0 : depth;
    }

    private ClosureTableNode mapNode(Record record, dev.kotryos.sqlhierarchy.jooq.tables.ClosureTableNode node) {
        return new ClosureTableNode(
                record.get(node.ID),
                record.get(node.LABEL),
                record.get(node.SORT_ORDER)
        );
    }
}
