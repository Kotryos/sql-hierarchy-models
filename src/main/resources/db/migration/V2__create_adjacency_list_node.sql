CREATE TABLE adjacency_list_node (
    id BIGINT NOT NULL PRIMARY KEY,
    label VARCHAR(255) NOT NULL UNIQUE,
    parent_id BIGINT,
    CONSTRAINT fk_adjacency_list_node_parent
        FOREIGN KEY (parent_id) REFERENCES adjacency_list_node (id)
);

CREATE INDEX idx_adjacency_list_node_parent
    ON adjacency_list_node (parent_id);
