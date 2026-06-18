package com.tortiki.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.tortiki.api.infrastructure.adapter.out.persistence.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Test de démarrage du contexte Spring Boot complet.
 *
 * <p>Hérite de {@link AbstractIntegrationTest} pour réutiliser
 * le conteneur PostgreSQL 16 partagés — évite de démarrer
 * un second conteneur dédié à ce seul test de smoke.</p>
 */
class TortikiApiApplicationTests extends AbstractIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Vérifie que le contexte Spring Boot démarre sans exception
     * et contient les beans critiques de l'application.
     */
    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.containsBean("securityConfig")).isTrue();
        assertThat(applicationContext.containsBean("userService")).isTrue();
        assertThat(applicationContext.containsBean("listingService")).isTrue();
    }
}