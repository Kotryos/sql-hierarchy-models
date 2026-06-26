CREATE TABLE nested_set_node (
    id BIGINT NOT NULL PRIMARY KEY,
    label VARCHAR(255) NOT NULL UNIQUE,
    lft INTEGER NOT NULL UNIQUE,
    rgt INTEGER NOT NULL UNIQUE,
    CONSTRAINT chk_nested_set_node_bounds CHECK (lft < rgt)
);
