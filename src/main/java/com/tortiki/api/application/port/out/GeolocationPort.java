package com.tortiki.api.application.port.out;

import java.util.Optional;

/**
 * Port secondaire — contrat de géolocalisation d'une ville ou d'une adresse.
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
   * <p>Utilisé par {@code SearchListingsUseCase} : la ville est déjà connue
   * et saisie par l'utilisateur pour centrer une recherche par rayon —
   * elle n'a pas besoin d'être résolue en retour.</p>
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
   * Géocode une adresse complète et résout sa localité.
   *
   * <p>Utilisé par {@code ListingService} à la création et à la
   * modification d'une annonce : l'adresse de retrait (numéro, rue, code
   * postal, ville) doit produire des coordonnées GPS et un nom de ville
   * exploitable, car la colonne {@code listings.city} porte une contrainte
   * {@code NOT NULL} (issue #147, recherche par ville). Contrairement à
   * {@link #geocode(String)}, la ville n'est pas connue à l'avance — c'est
   * précisément ce que cette méthode extrait de la réponse Nominatim.</p>
   *
   * <p>Retourne {@link Optional#empty()} si l'adresse est introuvable
   * ou si le service externe est indisponible — jamais d'exception
   * propagée au domaine.</p>
   *
   * @param address adresse complète de retrait saisie par le vendeur
   * @return coordonnées GPS et ville résolues, ou {@code Optional.empty()}
   *         si l'adresse est introuvable
   */
  Optional<GeocodedAddress> geocodeAddress(String address);

  /**
   * Coordonnées GPS retournées par le géocodage d'une ville déjà connue.
   *
   * @param latitude  latitude en degrés décimaux
   * @param longitude longitude en degrés décimaux
   */
  record Coordinates(double latitude, double longitude) {
  }

  /**
   * Coordonnées GPS et ville résolues par le géocodage d'une adresse complète.
   *
   * @param latitude  latitude en degrés décimaux
   * @param longitude longitude en degrés décimaux
   * @param city      localité résolue (ville, village ou commune) — jamais vide
   */
  record GeocodedAddress(double latitude, double longitude, String city) {
  }
}