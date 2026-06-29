CREATE TABLE nested_set_node (
    id BIGINT NOT NULL PRIMARY KEY,
    label VARCHAR(255) NOT NULL UNIQUE,
    -- lft and rgt are intentionally NOT UNIQUE: inserts and deletes shift many of these
    -- values in a single UPDATE, which transiently duplicates them mid-statement. Both
    -- PostgreSQL and MySQL check UNIQUE per row, so a UNIQUE constraint here would reject
    -- those bulk shifts.
    lft INTEGER NOT NULL,
    rgt INTEGER NOT NULL,
    CONSTRAINT chk_nested_set_node_bounds CHECK (lft < rgt)
);
