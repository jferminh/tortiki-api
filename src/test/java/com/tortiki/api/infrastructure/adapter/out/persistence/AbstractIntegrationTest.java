package com.tortiki.api.infrastructure.adapter.out.persistence;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Classe de base pour les tests d'intégration Testcontainers.
 *
 * <p>Démarre un conteneur PostgreSQL 16, partagé entre tous les tests
 * qui étendent cette classe — évite de recréer le conteneur
 * à chaque classe de test (optimisation CI).</p>
 *
 * <p>Les propriétés datasource sont injectées dynamiquement via
 * {@link DynamicPropertySource} pour écraser la configuration
 * {@code application-test.yml}.</p>
 *
 * <p>Le cycle de vie du conteneur est géré par l'annotation
 * {@link Testcontainers} — {@code start()} et {@code close()}
 * sont appelés automatiquement.</p>
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

  /**
   * Constructeur protégé — classe abstraite non instantiable directement.
   */
  protected AbstractIntegrationTest() {}

  /**
   * Conteneur PostgreSQL 16, partagé entre tous les tests d'intégration.
   *
   * <p>Le cycle de vie (start/stop/close) est délégué à {@link Testcontainers}
   * via {@link Container} — aucune gestion manuelle requise.</p>
   */
  @SuppressWarnings("resource")
  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("tortiki_test")
          .withUsername("tortiki")
          .withPassword("tortiki");

  /**
   * Injecte les propriétés de connexion du conteneur dans le contexte Spring.
   *
   * @param registry registre des propriétés dynamiques Spring Test
   */
  @DynamicPropertySource
  static void overrideDataSourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }
}