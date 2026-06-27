CREATE TABLE closure_table_node (
    id BIGINT NOT NULL PRIMARY KEY,
    label VARCHAR(255) NOT NULL UNIQUE,
    sort_order INTEGER NOT NULL UNIQUE
);

CREATE TABLE closure_table_path (
    ancestor_id BIGINT NOT NULL,
    descendant_id BIGINT NOT NULL,
    depth INTEGER NOT NULL,
    PRIMARY KEY (ancestor_id, descendant_id),
    CONSTRAINT fk_closure_table_path_ancestor
        FOREIGN KEY (ancestor_id) REFERENCES closure_table_node (id),
    CONSTRAINT fk_closure_table_path_descendant
        FOREIGN KEY (descendant_id) REFERENCES closure_table_node (id),
    CONSTRAINT chk_closure_table_path_depth CHECK (depth >= 0)
);

CREATE INDEX idx_closure_table_path_ancestor_depth
    ON closure_table_path (ancestor_id, depth);

CREATE INDEX idx_closure_table_path_descendant_depth
    ON closure_table_path (descendant_id, depth);
