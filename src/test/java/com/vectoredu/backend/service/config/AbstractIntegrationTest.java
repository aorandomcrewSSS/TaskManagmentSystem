package com.vectoredu.backend.service.config;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;

@Testcontainers
@TestPropertySource(properties = "spring.liquibase.enabled=true")
@SpringBootTest
public abstract class AbstractIntegrationTest {

    @BeforeAll
    static void applyMigrations() {
        try (Connection conn = DriverManager.getConnection(
                postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword())) {
            Liquibase liquibase = new Liquibase(
                    "db/changelog/changelog-master.yaml",
                    new ClassLoaderResourceAccessor(),
                    new JdbcConnection(conn)
            );
            liquibase.update(new Contexts(), new LabelExpression());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка Liquibase", e);
        }
    }

    // Define a static PostgreSQLContainer to ensure it's shared across all tests
    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    static {
        // Start the container if it's not already running
        if (!postgresContainer.isRunning()) {
            postgresContainer.start();
        }
    }

    // Dynamically set Spring properties to use the TestContainer's JDBC URL, username, and password
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }
}
