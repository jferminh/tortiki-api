package com.tortiki.api.application.service;

import com.tortiki.api.application.port.in.SearchCriteria;
import com.tortiki.api.application.port.in.SearchListingsUseCase;
import com.tortiki.api.application.port.out.GeolocationPort;
import com.tortiki.api.application.port.out.SearchListingRepository;
import com.tortiki.api.domain.model.Listing;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service applicatif de recherche d'annonces Tortiki.
 *
 * <p>Orchestre la géolocalisation de la ville fournie via {@link GeolocationPort}
 * puis délègue la recherche filtrée à {@link SearchListingRepository}.
 * Si le géocodage échoue (ville inconnue ou Nominatim indisponible),
 * la recherche s'effectue sans filtre géographique.</p>
 *
 * <p>Implémente {@link SearchListingsUseCase} — port primaire.</p>
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SearchListingsService implements SearchListingsUseCase {

  /** Port secondaire de géolocalisation (Nominatim OSM). */
  private final GeolocationPort geolocationPort;

  /** Port secondaire de recherche d'annonces (PostgreSQL). */
  private final SearchListingRepository searchListingRepository;

  /**
   * Construit le service avec les ports secondaires requis.
   *
   * @param geolocationPort         port de géolocalisation
   * @param searchListingRepository port de recherche d'annonces
   */
  public SearchListingsService(
      GeolocationPort geolocationPort,
      SearchListingRepository searchListingRepository) {
    this.geolocationPort = geolocationPort;
    this.searchListingRepository = searchListingRepository;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Si {@code criteria.city()} est renseignée, tente un géocodage
   * via Nominatim pour enrichir les critères avec les coordonnées GPS.
   * En cas d'échec, la recherche continue sans filtre géographique.</p>
   */
  @Override
  public List<Listing> search(SearchCriteria criteria) {
    SearchCriteria enriched = enrichWithCoordinates(criteria);
    log.debug(
        "Recherche annonces — ville='{}' cuisineTypeId={} rayon={}km page={} size={}",
        enriched.city(),
        enriched.cuisineTypeId(),
        enriched.radiusKm(),
        enriched.page(),
        enriched.size());
    return searchListingRepository.search(enriched);
  }

  /**
   * Enrichit les critères avec les coordonnées GPS si une ville est fournie.
   *
   * <p>Si le géocodage retourne {@code Optional.empty()} (ville inconnue
   * ou Nominatim indisponible), retourne les critères inchangés.</p>
   *
   * @param criteria critères bruts issus du contrôleur
   * @return critères enrichis avec latitude/longitude, ou inchangés
   */
  private SearchCriteria enrichWithCoordinates(SearchCriteria criteria) {
    if (criteria.city() == null || criteria.city().isBlank()) {
      return criteria;
    }
    return geolocationPort.geocode(criteria.city())
        .map(coords -> new SearchCriteria(
            criteria.query(),
            criteria.city(),
            criteria.cuisineTypeId(),
            criteria.allergenIds(),
            criteria.maxPrice(),
            coords.latitude(),
            coords.longitude(),
            criteria.radiusKm(),
            criteria.page(),
            criteria.size()))
        .orElseGet(() -> {
          log.warn(
              "Géocodage échoué pour '{}' — recherche sans filtre géographique",
              criteria.city());
          return criteria;
        });
  }
}