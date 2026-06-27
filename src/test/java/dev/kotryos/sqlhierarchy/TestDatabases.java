package dev.kotryos.sqlhierarchy;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@SuppressWarnings("resource")
public final class TestDatabases {

    private static final String MYSQL_IMAGE = "mysql:8.0.36";
    private static final String POSTGRES_IMAGE = "postgres:16.4-alpine";

    private TestDatabases() {
    }

    public static void register(Database database, DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> database.container().getJdbcUrl());
        registry.add("spring.datasource.username", () -> database.container().getUsername());
        registry.add("spring.datasource.password", () -> database.container().getPassword());
        registry.add("spring.datasource.driver-class-name", () -> database.container().getDriverClassName());
    }

    public enum Database {
        MYSQL(new MySQLContainer<>(MYSQL_IMAGE)),
        POSTGRES(new PostgreSQLContainer<>(POSTGRES_IMAGE));

        private final JdbcDatabaseContainer<?> container;

        Database(JdbcDatabaseContainer<?> container) {
            this.container = container;
        }

        private synchronized JdbcDatabaseContainer<?> container() {
            if (!container.isRunning()) {
                container.start();
            }
            return container;
        }
    }
}
