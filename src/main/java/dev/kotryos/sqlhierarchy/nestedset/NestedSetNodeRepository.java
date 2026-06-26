package dev.kotryos.sqlhierarchy.nestedset;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.util.List;

import static dev.kotryos.sqlhierarchy.jooq.Tables.NESTED_SET_NODE;

@Repository
public class NestedSetNodeRepository {

    private final DSLContext dsl;

    public NestedSetNodeRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<NestedSetNode> findLeafNodes() {
        var node = NESTED_SET_NODE.as("node");

        return dsl.select(node.ID, node.LABEL, node.LFT, node.RGT)
                .from(node)
                .where(node.RGT.eq(node.LFT.plus(1)))
                .fetch(record -> mapNode(record, node));
    }

    public List<NestedSetNode> findPath(long nodeId) {
        var current = NESTED_SET_NODE.as("node");
        var parent = NESTED_SET_NODE.as("parent");

        return dsl.select(parent.ID, parent.LABEL, parent.LFT, parent.RGT)
                .from(current, parent)
                .where(current.LFT.between(parent.LFT, parent.RGT))
                .and(parent.ID.ne(nodeId))
                .and(current.ID.eq(nodeId))
                .orderBy(parent.LFT)
                .fetch(record -> mapNode(record, parent));
    }

    public List<NestedSetNode> findSubtree(long rootId) {
        var current = NESTED_SET_NODE.as("node");
        var child = NESTED_SET_NODE.as("child");

        return dsl.select(child.ID, child.LABEL, child.LFT, child.RGT)
                .from(current, child)
                .where(child.LFT.between(current.LFT, current.RGT))
                .and(child.RGT.between(current.LFT, current.RGT))
                .and(child.ID.ne(rootId))
                .and(current.ID.eq(rootId))
                .orderBy(child.LFT)
                .fetch(record -> mapNode(record, child));
    }

    public int findDepth(long nodeId) {
        var current = NESTED_SET_NODE.as("node");
        var parent = NESTED_SET_NODE.as("parent");

        return dsl.fetchCount(
                dsl.selectOne()
                .from(current, parent)
                .where(current.LFT.between(parent.LFT, parent.RGT))
                .and(parent.ID.ne(nodeId))
                .and(current.ID.eq(nodeId))
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
