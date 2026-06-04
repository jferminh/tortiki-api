package com.tortiki.api.infrastructure.adapter.in.web;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration Spring Security dédiée aux tests {@code @WebMvcTest}.
 *
 * <p>Désactive les restrictions d'accès et le CSRF strict pour permettre
 * aux tests de contrôleur de s'exécuter sans authentification réelle.
 * N'est jamais chargée en profil {@code dev} ou {@code prod}.</p>
 */
@TestConfiguration
public class TestSecurityConfig {

  /**
   * Chaîne de filtres de sécurité permissive pour les tests unitaires web.
   *
   * @param http configurateur de sécurité HTTP
   * @return la chaîne de filtres configurée
   * @throws Exception si la configuration échoue
   */
  @Bean
  public SecurityFilterChain testSecurityFilterChain(HttpSecurity http)
      throws Exception {
    http
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .csrf(AbstractHttpConfigurer::disable);
    return http.build();
  }
}