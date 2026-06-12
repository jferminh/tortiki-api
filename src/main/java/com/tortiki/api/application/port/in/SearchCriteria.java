package com.tortiki.api.application.port.in;

import java.math.BigDecimal;
import java.util.List;

/**
 * Critères de recherche d'annonces sur la plateforme Tortiki.
 *
 * <p>Record immuable transmis du contrôleur REST au service
 * {@code SearchListingsUseCase}. Regroupe tous les paramètres
 * de filtrage disponibles en v1.</p>
 *
 * <p>Les champs {@code latitude} et {@code longitude} sont renseignés
 * par le service après géocodage de la {@code city} via
 * {@code GeolocationPort}. Ils ne sont jamais fournis directement
 * par le client HTTP.</p>
 *
 * @param query        mot-clé libre sur le titre ou la description
 * @param city         ville de retrait saisie par l'utilisateur
 * @param cuisineTypeId identifiant de l'origine culinaire (filtre optionnel)
 * @param allergenIds  identifiants des allergènes à exclure (filtre optionnel)
 * @param maxPrice     prix maximum en euros (filtre optionnel)
 * @param latitude     latitude géocodée par Nominatim (renseignée par le service)
 * @param longitude    longitude géocodée par Nominatim (renseignée par le service)
 * @param radiusKm     rayon de recherche en kilomètres (défaut : 10 km)
 * @param page         numéro de page pour la pagination (défaut : 0)
 * @param size         nombre de résultats par page (défaut : 10)
 */
public record SearchCriteria(
    String query,
    String city,
    Long cuisineTypeId,
    List<Long> allergenIds,
    BigDecimal maxPrice,
    Double latitude,
    Double longitude,
    Double radiusKm,
    int page,
    int size
) {

  /**
   * Constructeur compact — valeurs par défaut pour les champs optionnels.
   *
   * @param query         mot-clé libre
   * @param city          ville de retrait
   * @param cuisineTypeId filtre origine culinaire
   * @param allergenIds   allergènes à exclure
   * @param maxPrice      prix maximum
   * @param latitude      latitude géocodée
   * @param longitude     longitude géocodée
   * @param radiusKm      rayon de recherche
   * @param page          numéro de page
   * @param size          taille de page
   */
  public SearchCriteria {
    allergenIds = allergenIds != null ? List.copyOf(allergenIds) : List.of();
    radiusKm = radiusKm != null ? radiusKm : 10.0;
    page = page < 0 ? 0 : page;
    size = (size <= 0 || size > 50) ? 10 : size;
  }
}