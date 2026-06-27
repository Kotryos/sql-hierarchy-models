package dev.kotryos.sqlhierarchy.nestedset;

import dev.kotryos.sqlhierarchy.HierarchyNodeRepository;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.util.List;

import static dev.kotryos.sqlhierarchy.jooq.Tables.NESTED_SET_NODE;

@Repository
public class NestedSetNodeRepository implements HierarchyNodeRepository<NestedSetNode> {

    private final DSLContext dsl;

    public NestedSetNodeRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<NestedSetNode> findLeafNodes() {
        var node = NESTED_SET_NODE.as("node");

        /*
         * SELECT id, label, lft, rgt
         * FROM nested_set_node
         * WHERE rgt = lft + 1
         */
        return dsl.select(node.ID, node.LABEL, node.LFT, node.RGT)
                .from(node)
                .where(node.RGT.eq(node.LFT.plus(1)))
                .fetch(record -> mapNode(record, node));
    }

    @Override
    public List<NestedSetNode> findPath(long nodeId) {
        var current = NESTED_SET_NODE.as("node");
        var parent = NESTED_SET_NODE.as("parent");

        /*
         * SELECT parent.id, parent.label, parent.lft, parent.rgt
         * FROM nested_set_node node
         * JOIN nested_set_node parent ON node.lft BETWEEN parent.lft AND parent.rgt
         * WHERE node.id = :nodeId
         *   AND parent.id <> :nodeId
         * ORDER BY parent.lft
         */
        return dsl.select(parent.ID, parent.LABEL, parent.LFT, parent.RGT)
                .from(current)
                .join(parent).on(current.LFT.between(parent.LFT, parent.RGT))
                .where(current.ID.eq(nodeId))
                .and(parent.ID.ne(nodeId))
                .orderBy(parent.LFT)
                .fetch(record -> mapNode(record, parent));
    }

    @Override
    public List<NestedSetNode> findSubtree(long rootId) {
        var current = NESTED_SET_NODE.as("node");
        var child = NESTED_SET_NODE.as("child");

        /*
         * SELECT child.id, child.label, child.lft, child.rgt
         * FROM nested_set_node node
         * JOIN nested_set_node child ON child.lft BETWEEN node.lft AND node.rgt
         *   AND child.rgt BETWEEN node.lft AND node.rgt
         * WHERE node.id = :rootId
         *   AND child.id <> :rootId
         * ORDER BY child.lft
         */
        return dsl.select(child.ID, child.LABEL, child.LFT, child.RGT)
                .from(current)
                .join(child).on(child.LFT.between(current.LFT, current.RGT)
                        .and(child.RGT.between(current.LFT, current.RGT)))
                .where(current.ID.eq(rootId))
                .and(child.ID.ne(rootId))
                .orderBy(child.LFT)
                .fetch(record -> mapNode(record, child));
    }

    @Override
    public int findDepth(long nodeId) {
        var current = NESTED_SET_NODE.as("node");
        var parent = NESTED_SET_NODE.as("parent");

        /*
         * SELECT COUNT(*)
         * FROM nested_set_node node
         * JOIN nested_set_node parent ON node.lft BETWEEN parent.lft AND parent.rgt
         * WHERE node.id = :nodeId
         *   AND parent.id <> :nodeId
         */
        return dsl.fetchCount(
                dsl.selectOne()
                .from(current)
                .join(parent).on(current.LFT.between(parent.LFT, parent.RGT))
                .where(current.ID.eq(nodeId))
                .and(parent.ID.ne(nodeId))
        );
    }

    private NestedSetNode mapNode(
            Record record,
            dev.kotryos.sqlhierarchy.jooq.tables.NestedSetNode node
    ) {
        return new NestedSetNode(
                record.get(node.ID),
                record.get(node.LABEL),
                record.get(node.LFT),
                record.get(node.RGT)
        );
    }
}
