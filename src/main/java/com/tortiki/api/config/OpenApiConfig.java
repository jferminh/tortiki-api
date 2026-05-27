package com.tortiki.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration de la documentation OpenAPI 3 pour Tortiki API.
 *
 * <p>Génère automatiquement une interface Swagger UI accessible à l'adresse
 * {@code /swagger-ui.html} en environnement de développement.</p>
 *
 * <p>La documentation est désactivée en production via le profil Spring
 * {@code prod} — voir {@code application-prod.properties}.</p>
 */
@Configuration
public class OpenApiConfig {

  /**
   * Nom du schéma de sécurité par cookie de session.
   */
  private static final String SECURITY_SCHEME_SESSION = "cookieAuth";

  /**
   * Définit la configuration globale de l'API OpenAPI 3.
   *
   * <p>Inclut les métadonnées du projet, les serveurs disponibles
   * et le schéma de sécurité par session HTTP (cookie JSESSIONID).</p>
   *
   * @return la configuration {@link OpenAPI} complète
   */
  @Bean
  public OpenAPI tortikiOpenApi() {
    return new OpenAPI()
        .info(buildApiInfo())
        .servers(buildServers())
        .components(buildSecurityComponents())
        .addSecurityItem(
            new SecurityRequirement().addList(SECURITY_SCHEME_SESSION)
        )
        .externalDocs(
            new ExternalDocumentation()
                .description("Dépôt GitHub Tortiki API")
                .url("https://github.com/ton-compte/tortiki-api")
        );
  }

  /**
   * Construit les métadonnées descriptives de l'API.
   *
   * @return l'objet {@link Info} contenant titre, version et description
   */
  private Info buildApiInfo() {
    return new Info()
        .title("Tortiki API")
        .version("0.1.0-SNAPSHOT")
        .description(
            "API REST de la marketplace P2P de plats cuisinés maison Tortiki. "
                + "Permet la gestion des annonces, des utilisateurs, "
                + "des demandes de contact et des avis."
        )
        .contact(
            new Contact()
                .name("Équipe Tortiki")
                .email("contact@tortiki.fr")
        )
        .license(
            new License()
                .name("Licence privée — Projet CDA")
                .url("https://github.com/ton-compte/tortiki-api")
        );
  }

  /**
   * Déclare les serveurs disponibles pour les appels depuis Swagger UI.
   *
   * @return la liste des serveurs (local dev + recette)
   */
  private List<Server> buildServers() {
    Server localServer = new Server()
        .url("http://localhost:8080")
        .description("Serveur local — développement");

    Server stagingServer = new Server()
        .url("https://api-staging.tortiki.fr")
        .description("Serveur de recette");

    return List.of(localServer, stagingServer);
  }

  /**
   * Configure le schéma de sécurité basé sur le cookie de session HTTP.
   *
   * <p>En v1, l'authentification repose sur {@code JSESSIONID} — un cookie
   * de session géré automatiquement par Spring Security. Le JWT sera
   * introduit en v2 comme évolution documentée.</p>
   *
   * @return les composants OpenAPI incluant le schéma de sécurité
   */
  private Components buildSecurityComponents() {
    SecurityScheme sessionScheme = new SecurityScheme()
        .type(SecurityScheme.Type.APIKEY)
        .in(SecurityScheme.In.COOKIE)
        .name("JSESSIONID")
        .description(
            "Authentification par session HTTP stateful. "
                + "Connectez-vous via POST /api/auth/login — "
                + "le cookie JSESSIONID est automatiquement transmis."
        );

    return new Components()
        .addSecuritySchemes(SECURITY_SCHEME_SESSION, sessionScheme);
  }
}