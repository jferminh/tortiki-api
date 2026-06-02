// src/test/java/com/tortiki/api/TortikiApiApplicationTests.java
package com.tortiki.api;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de démarrage du contexte Spring Boot complet.
 *
 * <p>Nécessite une instance PostgreSQL active (profil "test").
 * Exécuté automatiquement en CI via le service Docker du pipeline.
 * En local : démarrer Docker Compose avant de lancer ce test,
 * ou utiliser le tag Maven : {@code ./mvnw test -P integration}.</p>
 */
@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
class TortikiApiApplicationTests {

    /**
     * Vérifie que le contexte Spring Boot démarre sans exception.
     */
    @Test
    void contextLoads() {
        // Contexte complet : PostgreSQL + Flyway + Security + tous les beans.
    }
}