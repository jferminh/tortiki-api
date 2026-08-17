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

  /**
   * {@inheritDoc}
   *
   * <p>Appelle {@code GET /search?q={address}&format=json&limit=1&addressdetails=1}
   * sur l'API Nominatim. Le paramètre {@code addressdetails=1} demande la
   * décomposition structurée de l'adresse (ville, code postal, pays...),
   * indispensable pour résoudre {@code GeocodedAddress.city()} — une simple
   * recherche par ville comme dans {@link #geocode(String)} ne suffit pas ici,
   * car l'entrée est une adresse complète (numéro, rue, ville).</p>
   *
   * <p>En cas d'erreur réseau, de timeout, d'adresse inconnue ou de localité
   * non résolue dans la réponse, retourne {@link Optional#empty()} sans
   * propager d'exception — {@code ListingService} traduira cette absence en
   * erreur métier explicite plutôt que de laisser échouer l'insertion en base.</p>
   */
  @Override
  public Optional<GeocodedAddress> geocodeAddress(String address) {
    if (address == null || address.isBlank()) {
      return Optional.empty();
    }
    try {
      List<NominatimResponse> results = webClient.get()
              .uri(uriBuilder -> uriBuilder
                      .path("/search")
                      .queryParam("q", address)
                      .queryParam("format", "json")
                      .queryParam("limit", "1")
                      .queryParam("addressdetails", "1")
                      .build())
              .retrieve()
              .bodyToFlux(NominatimResponse.class)
              .collectList()
              .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
              .block();

      if (results == null || results.isEmpty()) {
        log.warn("Nominatim : aucun résultat pour l'adresse '{}'", address);
        return Optional.empty();
      }

      NominatimResponse first = results.getFirst();
      String city = resolveLocality(first.address());
      if (city == null) {
        log.warn("Nominatim : aucune localité résolue pour l'adresse '{}'", address);
        return Optional.empty();
      }

      double latitude = Double.parseDouble(first.lat());
      double longitude = Double.parseDouble(first.lon());
      log.debug("Nominatim : '{}' géocodé → lat={}, lng={}, city={}",
              address, latitude, longitude, city);
      return Optional.of(new GeocodedAddress(latitude, longitude, city));

    } catch (WebClientException ex) {
      log.error("Nominatim : erreur réseau pour '{}' : {}", address, ex.getMessage());
      return Optional.empty();
    } catch (NumberFormatException ex) {
      log.error("Nominatim : coordonnées invalides pour '{}' : {}", address, ex.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Résout la localité à partir de la décomposition d'adresse Nominatim.
   *
   * <p>Nominatim ne renseigne pas systématiquement le champ {@code city} :
   * une adresse rurale peut être classée en {@code town}, {@code village}
   * ou {@code municipality}. Cette méthode applique une chaîne de repli
   * dans cet ordre de spécificité décroissante.</p>
   *
   * @param address décomposition d'adresse retournée par Nominatim, peut-être
   *                {@code null} si {@code addressdetails} n'a pas été honoré
   * @return la première localité non vide trouvée, ou {@code null} si aucune
   */
  private String resolveLocality(NominatimAddress address) {
    if (address == null) {
      return null;
    }
    return firstNonBlank(address.city(), address.town(), address.village(),
            address.municipality(), address.county());
  }

  /**
   * Retourne la première chaîne non nulle et non vide parmi les candidats.
   *
   * @param candidates valeurs à tester dans l'ordre de priorité
   * @return la première valeur non vide, ou {@code null} si toutes le sont
   */
  private String firstNonBlank(String... candidates) {
    for (String candidate : candidates) {
      if (candidate != null && !candidate.isBlank()) {
        return candidate;
      }
    }
    return null;
  }

  /**
   * Réponse brute désérialisée depuis l'API Nominatim.
   *
   * @param lat     latitude au format texte (Nominatim ne renvoie pas de nombre)
   * @param lon     longitude au format texte
   * @param address décomposition structurée de l'adresse, présente uniquement
   *                si {@code addressdetails=1} a été demandé
   */
  private record NominatimResponse(String lat, String lon, NominatimAddress address) {
  }

  /**
   * Décomposition structurée d'une adresse Nominatim ({@code addressdetails=1}).
   *
   * <p>Tous les champs sont optionnels et dépendent du niveau de détail
   * disponible dans OpenStreetMap pour l'adresse recherchée.</p>
   *
   * @param city         ville, si Nominatim la classe ainsi
   * @param town         bourg, alternative à {@code city} en zone rurale
   * @param village      village, alternative en zone très rurale
   * @param municipality commune administrative, dernier repli avant le comté
   * @param county       comté ou département, repli ultime
   */
  private record NominatimAddress(
          String city, String town, String village, String municipality, String county) {
  }
}