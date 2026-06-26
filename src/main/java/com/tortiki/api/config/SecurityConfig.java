package com.tortiki.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration principale de Spring Security pour Tortiki API.
 *
 * <p>Stratégie retenue pour le MVP v1 : sessions HTTP stateful.
 * Le JWT est volontairement reporté en v2 afin de maîtriser
 * la complexité initiale et de privilégier la lisibilité pédagogique.</p>
 *
 * <p>Spring Boot auto-configure {@code DaoAuthenticationProvider} automatiquement
 * dès qu'un bean {@link org.springframework.security.core.userdetails.UserDetailsService}
 * et un bean {@link PasswordEncoder} sont présents dans le contexte.
 * La déclaration manuelle du provider est inutile et dépréciée
 * depuis Spring Security 6.4.</p>
 *
 * <p>CSRF désactivé sur {@code /api/v1/**} : l'API REST Tortiki est consommée
 * exclusivement par des clients HTTP (OpenFeign, HTTP Client IntelliJ, curl)
 * avec {@code Content-Type: application/json}. Les attaques CSRF ciblent les
 * formulaires HTML soumis depuis un navigateur — ce vecteur n'existe pas dans
 * {@code tortiki-api}. La protection CSRF reste active sur les routes non-API.
 * Référence OWASP : <a href="https://owasp.org/www-community/attacks/csrf">...</a>
 * Décision documentée — Section 5 dossier CDA.</p>
 *
 * <p>Contrôle d'accès par rôle (RBAC) :</p>
 * <ul>
 *   <li>{@code ROLE_ADMIN}  — administration de la plateforme</li>
 *   <li>{@code ROLE_SELLER} — gestion des annonces et des demandes reçues</li>
 *   <li>{@code ROLE_BUYER}  — recherche, contact et notation</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  /**
   * Encodeur de mots de passe BCrypt (force 12, recommandation OWASP).
   *
   * @return une instance de {@link BCryptPasswordEncoder}
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  /**
   * Gestionnaire d'authentification exposé comme bean Spring.
   *
   * @param config la configuration d'authentification auto-configurée
   * @return le gestionnaire d'authentification
   * @throws Exception en cas d'erreur de configuration
   */
  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * Chaîne de filtres de sécurité principale.
   *
   * <p>Règles d'accès par ordre de priorité :</p>
   * <ol>
   *   <li>Documentation API (Swagger) — accès public en dev</li>
   *   <li>Endpoints publics (inscription, connexion, lecture annonces)</li>
   *   <li>Endpoints acheteur — rôle BUYER requis</li>
   *   <li>Endpoints vendeur — rôle SELLER requis</li>
   *   <li>Endpoints administration — rôle ADMIN requis</li>
   *   <li>Tout le reste — authentification requise</li>
   * </ol>
   *
   * @param http le constructeur de sécurité HTTP
   * @return la chaîne de filtres configurée
   * @throws Exception en cas d'erreur de configuration
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
      throws Exception {

    http
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            .maximumSessions(1)
            .maxSessionsPreventsLogin(false)
        )

        // CSRF désactivé sur /api/v1/** — justification complète en Javadoc de classe
        // SonarCloud Hotspot : choix conscient, documenté Section 5 dossier CDA
        .csrf(csrf -> csrf

            .ignoringRequestMatchers(SecurityConstants.API_V1 + "/**")
        )

        .authorizeHttpRequests(auth -> auth

            // ── Documentation API ─────────────────────────────────────────
            .requestMatchers(
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/api-docs/**",
                "/v3/api-docs/**"
            ).permitAll()

            // ── Authentification — public ─────────────────────────────────
            .requestMatchers(HttpMethod.POST,
                SecurityConstants.ROUTE_AUTH_REGISTER,
                SecurityConstants.ROUTE_AUTH_LOGIN
            ).permitAll()
            .requestMatchers(HttpMethod.POST,
                SecurityConstants.ROUTE_AUTH_LOGOUT
            ).permitAll()

            // ── Annonces — lecture publique ───────────────────────────────
            .requestMatchers(HttpMethod.GET,
                SecurityConstants.ROUTE_LISTINGS,
                SecurityConstants.ROUTE_LISTINGS_SEARCH,
                SecurityConstants.ROUTE_LISTING_BY_ID
            ).permitAll()

            // ── Origines culinaires — lecture publique ────────────────────
            .requestMatchers(HttpMethod.GET,
                SecurityConstants.ROUTE_CUISINE_TYPES,
                SecurityConstants.ROUTE_CUISINE_TYPES_ALL
            ).permitAll()

            // ── Allergènes — lecture publique ─────────────────────────────
            .requestMatchers(HttpMethod.GET,
                SecurityConstants.ROUTE_ALLERGENS,
                SecurityConstants.ROUTE_ALLERGENS_ALL
            ).permitAll()

            // ── Acheteur — ROLE_BUYER requis ──────────────────────────────
            .requestMatchers(HttpMethod.POST,
                SecurityConstants.ROUTE_CONTACT_REQUESTS
            ).hasRole(SecurityConstants.ROLE_BUYER)
            .requestMatchers(HttpMethod.GET,
                SecurityConstants.ROUTE_CONTACT_MY
            ).hasRole(SecurityConstants.ROLE_BUYER)
            .requestMatchers(HttpMethod.POST,
                SecurityConstants.ROUTE_REVIEWS
            ).hasRole(SecurityConstants.ROLE_BUYER)

            // ── Vendeur — ROLE_SELLER requis ──────────────────────────────
            .requestMatchers(HttpMethod.POST,
                SecurityConstants.ROUTE_LISTINGS
            ).hasRole(SecurityConstants.ROLE_SELLER)
            .requestMatchers(HttpMethod.PUT,
                SecurityConstants.ROUTE_LISTING_BY_ID
            ).hasRole(SecurityConstants.ROLE_SELLER)
            .requestMatchers(HttpMethod.DELETE,
                SecurityConstants.ROUTE_LISTING_BY_ID
            ).hasRole(SecurityConstants.ROLE_SELLER)
            .requestMatchers(HttpMethod.GET,
                SecurityConstants.ROUTE_SELLER_DASHBOARD_CONTACT_REQUESTS
            ).hasRole(SecurityConstants.ROLE_SELLER)
            .requestMatchers(HttpMethod.PATCH,
                SecurityConstants.ROUTE_SELLER_DASHBOARD_CONTACT_STATUS
            ).hasRole(SecurityConstants.ROLE_SELLER)

            // ── Administration — ROLE_ADMIN requis ────────────────────────
            .requestMatchers(HttpMethod.POST,
                SecurityConstants.ROUTE_CUISINE_TYPES
            ).hasRole(SecurityConstants.ROLE_ADMIN)
            .requestMatchers(HttpMethod.PUT,
                SecurityConstants.ROUTE_CUISINE_TYPES_ALL
            ).hasRole(SecurityConstants.ROLE_ADMIN)
            .requestMatchers(HttpMethod.DELETE,
                SecurityConstants.ROUTE_CUISINE_TYPES_ALL
            ).hasRole(SecurityConstants.ROLE_ADMIN)
            .requestMatchers(HttpMethod.PATCH,
                SecurityConstants.ROUTE_ADMIN_LISTINGS_ALL
            ).hasRole(SecurityConstants.ROLE_ADMIN)
            .requestMatchers(
                SecurityConstants.ROUTE_ADMIN_ALL
            ).hasRole(SecurityConstants.ROLE_ADMIN)

            // ── Tout le reste — authentification requise ──────────────────
            .anyRequest().authenticated()
        )

        .exceptionHandling(ex -> ex
            .authenticationEntryPoint((request, response, authException) -> {
              response.setStatus(401);
              response.setContentType("application/json;charset=UTF-8");
              response.getWriter().write(
                  "{\"error\": \"Non authentifié\", "
                      + "\"message\": \"Connexion requise\"}"
              );
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.setStatus(403);
              response.setContentType("application/json;charset=UTF-8");
              response.getWriter().write(
                  "{\"error\": \"Accès refusé\", "
                      + "\"message\": \"Droits insuffisants\"}"
              );
            })
        );

    return http.build();
  }
}