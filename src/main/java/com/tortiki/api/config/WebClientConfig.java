package com.tortiki.api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration du client HTTP réactif {@link WebClient}.
 *
 * <p>Déclare un bean {@code WebClient} dédié à Nominatim,
 * pré-configuré avec l'URL de base et l'en-tête {@code User-Agent}
 * obligatoire selon les CGU d'OpenStreetMap.</p>
 */
@Configuration
@EnableConfigurationProperties(NominatimProperties.class)
public class WebClientConfig {

  /**
   * Bean WebClient pré-configuré pour l'API Nominatim OSM.
   *
   * @param properties propriétés Nominatim issues du YAML
   * @return instance WebClient configurée
   */
  @Bean
  public WebClient nominatimWebClient(NominatimProperties properties) {
    return WebClient.builder()
        .baseUrl(properties.getBaseUrl())
        .defaultHeader("User-Agent", properties.getUserAgent())
        .defaultHeader("Accept-Language", "fr")
        .build();
  }
}