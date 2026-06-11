package com.tortiki.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriétés de configuration du service de géolocalisation Nominatim.
 *
 * <p>Mappées depuis les clés {@code nominatim.*} des fichiers YAML.
 * Injectées dans {@code NominatimGateway} via le constructeur.</p>
 */
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

  /**
   * Retourne l'URL de base de l'API Nominatim.
   *
   * @return URL de base
   */
  public String getBaseUrl() {
    return baseUrl;
  }

  /**
   * Définit l'URL de base de l'API Nominatim.
   *
   * @param baseUrl URL de base
   */
  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  /**
   * Retourne l'en-tête User-Agent de l'application.
   *
   * @return User-Agent
   */
  public String getUserAgent() {
    return userAgent;
  }

  /**
   * Définit l'en-tête User-Agent de l'application.
   *
   * @param userAgent User-Agent
   */
  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  /**
   * Retourne le délai maximum d'attente en secondes.
   *
   * @return timeout en secondes
   */
  public int getTimeoutSeconds() {
    return timeoutSeconds;
  }

  /**
   * Définit le délai maximum d'attente en secondes.
   *
   * @param timeoutSeconds timeout en secondes
   */
  public void setTimeoutSeconds(int timeoutSeconds) {
    this.timeoutSeconds = timeoutSeconds;
  }
}