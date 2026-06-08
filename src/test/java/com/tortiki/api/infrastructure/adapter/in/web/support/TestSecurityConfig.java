package com.tortiki.api.infrastructure.adapter.in.web.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration Spring Security permissive dédiée aux tests {@code @WebMvcTest}.
 *
 * <p>Désactive la protection CSRF et autorise toutes les requêtes sans
 * authentification, afin que les tests de contrôleur se concentrent
 * sur la logique métier plutôt que sur la sécurité.</p>
 *
 * <p>Usage : importer explicitement dans chaque classe {@code @WebMvcTest} :</p>
 * <pre>{@code
 * @WebMvcTest(AuthController.class)
 * @Import(TestSecurityConfig.class)
 * class AuthControllerTest { ... }
 * }</pre>
 *
 * <p>Ce n'est jamais chargée en dehors du contexte de test.
 * Le bean {@code @Primary} garantit sa priorité sur {@code SecurityConfig}.</p>
 */
@TestConfiguration
public class TestSecurityConfig {

  /**
   * Chaîne de filtres de sécurité permissive pour les tests unitaires web.
   *
   * <p>{@code @Primary} écrase le bean {@code securityFilterChain} de production
   * dans le contexte {@code @WebMvcTest}.</p>
   *
   * @param http configurateur de sécurité HTTP
   * @return la chaîne de filtres configurée sans restriction
   * @throws Exception si la configuration échoue
   */
  @Bean
  @Primary
  public SecurityFilterChain testSecurityFilterChain(HttpSecurity http)
      throws Exception {
    http
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .csrf(AbstractHttpConfigurer::disable);
    return http.build();
  }
}