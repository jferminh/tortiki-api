package com.tortiki.api.infrastructure.adapter.in.web.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
 * <p>Configure explicitement {@code exceptionHandling()} pour que
 * {@code AccessDeniedException} (levée par {@code @PreAuthorize})
 * soit traduite en HTTP 403 et non en HTTP 500.</p>
 *
 * <p>Usage : importer explicitement dans chaque classe {@code @WebMvcTest} :</p>
 * <pre>{@code
 * @WebMvcTest(CuisineTypeController.class)
 * @Import(TestSecurityConfig.class)
 * class CuisineTypeControllerTest { ... }
 * }</pre>
 *
 * <p>Ce n'est jamais chargée en profil {@code dev} ou {@code prod}.</p>
 */
@TestConfiguration
@EnableMethodSecurity
public class TestSecurityConfig {

  /**
   * Chaîne de filtres de sécurité permissive pour les tests unitaires web.
   *
   * <p>{@code @Primary} écrase le bean {@code securityFilterChain} de production
   * dans le contexte {@code @WebMvcTest}.</p>
   *
   * <p>Le handler {@code exceptionHandling()} traduit {@code AccessDeniedException}
   * en HTTP 403 — nécessaire pour que {@code @PreAuthorize} retourne 403
   * et non 500 dans les tests.</p>
   *
   * @param http configurateur de sécurité HTTP
   * @return la chaîne de filtres configurée
   * @throws Exception si la configuration échoue
   */
  @Bean
  @Primary
  public SecurityFilterChain testSecurityFilterChain(HttpSecurity http)
      throws Exception {
    http
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(ex -> ex
            .accessDeniedHandler((request, response, accessDeniedException) ->
                response.sendError(HttpStatus.FORBIDDEN.value())
            )
        );
    return http.build();
  }
}