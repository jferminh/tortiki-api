package com.tortiki.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test de démarrage du contexte Spring Boot complet.
 *
 * <p>Nécessite une instance PostgreSQL active (profil "test").
 * Exécuté automatiquement en CI via le service Docker du pipeline.
 * En local : démarrer Docker Compose avant de lancer ce test,
 * ou utiliser le tag Maven : {@code ./mvnw test -P integration}.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class TortikiApiApplicationTests {

    @Container
    @SuppressWarnings("resource") // géré par @Testcontainers, pas par try-with-resources
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("tortiki_test")
            .withUsername("tortiki")
            .withPassword("tortiki_secret");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    /**
     * Vérifie que le contexte Spring Boot démarre sans exception.
     */
    @Test
    void contextLoads() {
        // Contexte complet : PostgreSQL + Flyway + Security + tous les beans.
    }
}