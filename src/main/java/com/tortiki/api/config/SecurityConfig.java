package com.tortiki.api.config;

import lombok.RequiredArgsConstructor;
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
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

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
 * La déclaration manuelle du provider est donc inutile et dépréciée
 * depuis Spring Security 6.4.</p>
 *
 * <p>Contrôle d'accès par rôle (RBAC) :
 * </p>
 * <ul>
 *   <li>{@code ROLE_ADMIN}  — administration de la plateforme</li>
 *   <li>{@code ROLE_SELLER} — gestion des annonces et des demandes</li>
 *   <li>{@code ROLE_BUYER}  — recherche et expression d'intérêt</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  // ===== CONSTANTES — RÔLES =====

  /**
   * Rôle administrateur de la plateforme.
   */
  private static final String ROLE_ADMIN = "ADMIN";

  /**
   * Rôle vendeur — gestion des annonces et des demandes.
   */
  private static final String ROLE_SELLER = "SELLER";

  /**
   * Rôle acheteur — recherche et expression d'intérêt.
   */
  private static final String ROLE_BUYER = "BUYER";

  // ===== CONSTANTES — ROUTES =====

  /**
   * Route d'une annonce par identifiant.
   */
  private static final String ROUTE_LISTING_BY_ID = "/api/listings/{id}";

  /**
   * Route de confirmation d'une demande de contact.
   */
  private static final String ROUTE_CONTACT_CONFIRM = "/api/contact-requests/*/confirm";

  /**
   * Route de refus d'une demande de contact.
   */
  private static final String ROUTE_CONTACT_REFUSE = "/api/contact-requests/*/refuse";

  /**
   * Encodeur de mots de passe BCrypt (force 12).
   *
   * <p>BCrypt est recommandé par l'OWASP pour le stockage des mots de passe.
   * Le facteur de coût 12 offre un bon équilibre sécurité / performance.</p>
   *
   * <p>Spring Boot détecte automatiquement ce bean et configure
   * {@code DaoAuthenticationProvider} sans déclaration manuelle.</p>
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
   * <p>Nécessaire pour déclencher l'authentification manuellement
   * depuis le contrôleur de connexion.</p>
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
        // ===== GESTION DE SESSION =====
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .maximumSessions(1)
            .maxSessionsPreventsLogin(false)
        )
        .securityContext(context -> context
            .securityContextRepository(
                new HttpSessionSecurityContextRepository()
            )
        )

        // ===== RÈGLES D'ACCÈS =====
        .authorizeHttpRequests(auth -> auth

            // Documentation API — accessible publiquement (désactivé en prod)
            .requestMatchers(
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/api-docs/**",
                "/v3/api-docs/**"
            ).permitAll()

            // Endpoints publics — visiteurs non authentifiés
            .requestMatchers(HttpMethod.POST,
                "/api/auth/register",
                "/api/auth/login"
            ).permitAll()
            .requestMatchers(HttpMethod.GET,
                "/api/listings",
                ROUTE_LISTING_BY_ID,
                "/api/listings/search",
                "/api/cuisine-types"
            ).permitAll()

            // Endpoints vendeur
            .requestMatchers(HttpMethod.POST,
                "/api/listings"
            ).hasRole(ROLE_SELLER)
            .requestMatchers(HttpMethod.PUT,
                ROUTE_LISTING_BY_ID
            ).hasRole(ROLE_SELLER)
            .requestMatchers(HttpMethod.DELETE,
                ROUTE_LISTING_BY_ID
            ).hasRole(ROLE_SELLER)
            .requestMatchers(
                ROUTE_CONTACT_CONFIRM,
                ROUTE_CONTACT_REFUSE
            ).hasRole(ROLE_SELLER)

            // Endpoints administration
            .requestMatchers("/api/admin/**").hasRole(ROLE_ADMIN)

            // Tout le reste nécessite une authentification
            .anyRequest().authenticated()
        )

        // ===== CSRF =====
        .csrf(csrf -> csrf
            .ignoringRequestMatchers("/api/auth/**")
        )

        // ===== GESTION DES ERREURS D'AUTHENTIFICATION =====
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint((request, response, authException) -> {
              response.setStatus(401);
              response.setContentType("application/json");
              response.getWriter().write(
                  "{\"error\": \"Non authentifié\", "
                      + "\"message\": \"Connexion requise\"}"
              );
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.setStatus(403);
              response.setContentType("application/json");
              response.getWriter().write(
                  "{\"error\": \"Accès refusé\", "
                      + "\"message\": \"Droits insuffisants\"}"
              );
            })
        );

    return http.build();
  }
}