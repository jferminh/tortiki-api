package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.application.port.in.SearchCriteria;
import com.tortiki.api.application.port.out.SearchListingRepository;
import com.tortiki.api.domain.model.Listing;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * Adaptateur secondaire — implémentation JPA de {@link SearchListingRepository}.
 *
 * <p>Traduit un {@link SearchCriteria} enrichi (coordonnées géocodées,
 * filtres optionnels) en requêtes JPQL via {@link ListingJpaRepository}.
 * La couche application ne connaît jamais cette classe concrète.</p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SearchListingRepositoryAdapter implements SearchListingRepository {

  /** Repository Spring Data JPA — accès direct à la table listings. */
  private final ListingJpaRepository listingJpaRepository;

  /** Mapper domaine ↔ entité JPA — bean Spring injecté. */
  private final ListingPersistenceMapper listingPersistenceMapper;

  /** {@inheritDoc} */
  @Override
  public List<Listing> search(SearchCriteria criteria) {
    log.debug(
        "Recherche annonces — lat={} lng={} rayon={}km cuisine={} prix={}",
        criteria.latitude(), criteria.longitude(), criteria.radiusKm(),
        criteria.cuisineTypeId(), criteria.maxPrice()
    );

    BigDecimal lat = BigDecimal.valueOf(criteria.latitude());
    BigDecimal lng = BigDecimal.valueOf(criteria.longitude());
    Pageable pageable = PageRequest.of(criteria.page(), criteria.size());

    List<ListingJpaEntity> entities = listingJpaRepository.searchByCriteria(
        lat,
        lng,
        criteria.radiusKm(),
        criteria.cuisineTypeId(),
        criteria.maxPrice(),
        criteria.query(),
        pageable
    );

    // Post-filtrage allergène en mémoire — liste courte (max 50 résultats paginés)
    List<Long> allergenIds = criteria.allergenIds();
    if (!allergenIds.isEmpty()) {
      entities = entities.stream()
          .filter(l -> l.getAllergens().stream()
              .noneMatch(a -> allergenIds.contains(a.getId())))
          .toList();
    }

    log.debug("{} annonce(s) trouvée(s) après filtrage allergènes", entities.size());
    return entities.stream()
        .map(listingPersistenceMapper::toDomain)
        .toList();
  }
}