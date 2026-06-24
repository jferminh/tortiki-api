package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.application.port.out.ListingRepository;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.ListingStatus;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adaptateur secondaire de persistance des annonces.
 *
 * <p>Implémente {@link ListingRepository} (port out) en déléguant
 * à {@link ListingJpaRepository} (Spring Data JPA).
 * Seule classe autorisée à dépendre de {@link ListingJpaEntity}.</p>
 *
 * <p>Applique le pattern Adapter : le domaine ne connaît jamais
 * {@link ListingJpaEntity}, uniquement {@code Listing} POJO pur.</p>
 */
@Component
@RequiredArgsConstructor
public class ListingJpaAdapter implements ListingRepository {

  private final ListingJpaRepository jpaRepository;
  private final ListingPersistenceMapper mapper;

  /** {@inheritDoc} */
  @Override
  public Listing save(Listing listing) {
    ListingJpaEntity entity = mapper.toEntity(listing);
    return mapper.toDomain(jpaRepository.save(entity));
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Listing> findById(Long id) {
    return jpaRepository.findByIdWithSeller(id).map(mapper::toDomain); // ← JOIN FETCH seller
  }

  /** {@inheritDoc} */
  @Override
  public List<Listing> findBySellerIdAndStatus(Long sellerId, ListingStatus status) {
    return jpaRepository.findBySellerIdAndStatus(sellerId, status)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  /** {@inheritDoc} */
  @Override
  public List<Listing> findByStatus(ListingStatus status) {
    return jpaRepository.findByStatus(status)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }
}