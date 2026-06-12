package com.tortiki.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriétés de configuration du service de géolocalisation Nominatim.
 *
 * <p>Mappées depuis les clés {@code nominatim.*} des fichiers YAML.
 * Injectées dans {@code NominatimGateway} via le constructeur.</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "nominatim")
public class NominatimProperties {

  /** URL de base de l'API Nominatim. */
  private String baseUrl;

  /**
   * En-tête User-Agent obligatoire selon les CGU Nominatim.
   * Doit identifier l'application et fournir un contact.
   */
  private String userAgent;

  /** Délai maximum d'attente en secondes avant timeout. */
  private int timeoutSeconds;
}