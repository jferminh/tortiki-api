package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.infrastructure.adapter.out.storage.MinioStorageAdapter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Classe de base pour les tests d'intégration Testcontainers.
 *
 * <p>Démarre un conteneur PostgreSQL 16 une seule fois via un bloc
 * statique — partagé entre toutes les sous-classes pendant toute
 * la durée de la JVM Maven Surefire.</p>
 *
 * <p>Le cycle de vie est géré manuellement (pas de {@code @Testcontainers})
 * afin d'éviter que Ryuk détruise le conteneur entre deux suites de tests.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

  @MockitoBean
  MinioStorageAdapter minioStorageAdapter;

  /**
   * Constructeur protégé — classe abstraite non instantiable directement.
   */
  protected AbstractIntegrationTest() {}

  /**
   * Conteneur PostgreSQL 16 partagé — démarré une seule fois pour la JVM.
   *
   * <p>Le bloc statique garantit le démarrage avant le premier test,
   * indépendamment de l'ordre d'exécution des classes.</p>
   */
  @SuppressWarnings("resource")
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("tortikitest")
          .withUsername("tortiki")
          .withPassword("tortiki");

  static {
    POSTGRES.start();
  }

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