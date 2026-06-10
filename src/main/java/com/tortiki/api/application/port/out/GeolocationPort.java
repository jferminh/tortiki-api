package com.tortiki.api.application.port.out;

import java.util.Optional;

/**
 * Port secondaire — contrat de géolocalisation d'une adresse.
 *
 * <p>Définit le contrat entre la couche {@code application/service/}
 * et l'adaptateur externe {@code NominatimGateway}. Le service ne
 * connaît pas Nominatim — il utilise uniquement ce port.</p>
 *
 * <p>L'implémentation concrète {@code NominatimGateway} vit dans
 * {@code infrastructure/adapter/out/geolocation/}.</p>
 */
public interface GeolocationPort {

  /**
   * Géocode une ville ou un code postal en coordonnées GPS.
   *
   * <p>Retourne {@link Optional#empty()} si la ville est introuvable
   * ou si le service externe est indisponible — jamais d'exception
   * propagée au domaine.</p>
   *
   * @param city ville ou code postal à géocoder
   * @return coordonnées GPS ou {@code Optional.empty()} si introuvable
   */
  Optional<Coordinates> geocode(String city);

  /**
   * Coordonnés GPS retournés par le géocodage.
   *
   * @param latitude  latitude en degrés décimaux
   * @param longitude longitude en degrés décimaux
   */
  record Coordinates(double latitude, double longitude) {
  }
}