package com.tortiki.api.infrastructure.adapter.out.geolocation;

import com.tortiki.api.application.port.out.GeolocationPort;
import com.tortiki.api.config.NominatimProperties;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

/**
 * Adaptateur de géolocalisation utilisant l'API Nominatim OpenStreetMap.
 *
 * <p>Implémente {@link GeolocationPort} — le service applicatif
 * {@code SearchListingsService} ne connaît que le port, jamais
 * cette classe concrète.</p>
 *
 * <p>Règles CGU Nominatim respectées :</p>
 * <ul>
 *   <li>En-tête {@code User-Agent} identifiant l'application (configuré dans
 *       {@code WebClientConfig})</li>
 *   <li>Maximum 1 requête par seconde (acceptable pour un MVP CDA)</li>
 *   <li>Aucune donnée personnelle transmise — uniquement ville/code postal</li>
 * </ul>
 */
@Slf4j
@Component
public class NominatimGateway implements GeolocationPort {

  /** Client HTTP pré-configuré avec l'URL de base et le User-Agent Nominatim. */
  private final WebClient webClient;

  /** Propriétés de configuration Nominatim issues du YAML. */
  private final NominatimProperties properties;

  /**
   * Construit le gateway avec le client HTTP et les propriétés de configuration.
   *
   * @param nominatimWebClient client HTTP Nominatim pré-configuré
   * @param properties         propriétés de configuration Nominatim
   */
  public NominatimGateway(WebClient nominatimWebClient, NominatimProperties properties) {
    this.webClient = nominatimWebClient;
    this.properties = properties;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Appelle {@code GET /search?q={city}&format=json&limit=1}
   * sur l'API Nominatim. En cas d'erreur réseau, de timeout ou de ville
   * inconnue, retourne {@link Optional#empty()} sans propager d'exception.</p>
   */
  @Override
  public Optional<Coordinates> geocode(String city) {
    if (city == null || city.isBlank()) {
      return Optional.empty();
    }
    try {
      List<NominatimResponse> results = webClient.get()
          .uri(uriBuilder -> uriBuilder
              .path("/search")
              .queryParam("q", city)
              .queryParam("format", "json")
              .queryParam("limit", "1")
              .build())
          .retrieve()
          .bodyToFlux(NominatimResponse.class)
          .collectList()
          .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
          .block();

      if (results == null || results.isEmpty()) {
        log.warn("Nominatim : aucun résultat pour la ville '{}'", city);
        return Optional.empty();
      }

      NominatimResponse first = results.getFirst();
      double latitude = Double.parseDouble(first.lat());
      double longitude = Double.parseDouble(first.lon());
      log.debug("Nominatim : '{}' géocodé → lat={}, lng={}", city, latitude, longitude);
      return Optional.of(new Coordinates(latitude, longitude));

    } catch (WebClientException ex) {
      log.error("Nominatim : erreur réseau pour '{}' : {}", city, ex.getMessage());
      return Optional.empty();
    } catch (NumberFormatException ex) {
      log.error("Nominatim : coordonnées invalides pour '{}' : {}", city, ex.getMessage());
      return Optional.empty();
    }
  }
}