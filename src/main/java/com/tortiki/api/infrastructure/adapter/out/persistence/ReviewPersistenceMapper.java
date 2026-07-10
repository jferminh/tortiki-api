package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.Review;
import com.tortiki.api.domain.model.User;

/**
 * Mapper de conversion entre le POJO domaine {@link Review}
 * et l'entité JPA {@link ReviewJpaEntity}.
 *
 * <p>Classe utilitaire statique — pas d'état, pas de bean Spring.</p>
 */
public final class ReviewPersistenceMapper {

  /** Constructeur privé — classe utilitaire non instantiable. */
  private ReviewPersistenceMapper() {}

  /**
   * Convertit un POJO domaine {@link Review} en entité JPA {@link ReviewJpaEntity}.
   *
   * @param review             POJO domaine à convertir
   * @param contactRequestRef  référence JPA de la demande confirmée (proxy Hibernate)
   * @param listingRef         référence JPA de l'annonce (proxy Hibernate)
   * @param reviewerRef        référence JPA de l'acheteur (proxy Hibernate)
   * @param sellerRef          référence JPA du vendeur (proxy Hibernate)
   * @return entité JPA prête à persister
   */
  public static ReviewJpaEntity toEntity(
      final Review review,
      final ContactRequestJpaEntity contactRequestRef,
      final ListingJpaEntity listingRef,
      final UserJpaEntity reviewerRef,
      final UserJpaEntity sellerRef) {
    ReviewJpaEntity entity = new ReviewJpaEntity();
    entity.setId(review.getId());
    entity.setContactRequest(contactRequestRef);
    entity.setListing(listingRef);
    entity.setReviewer(reviewerRef);
    entity.setSeller(sellerRef);
    entity.setRating(review.getRating());
    entity.setComment(review.getComment());
    entity.setCreatedAt(review.getCreatedAt());
    return entity;
  }

  /**
   * Convertit une entité JPA {@link ReviewJpaEntity} en POJO domaine {@link Review}.
   *
   * <p>Utilise uniquement les identifiants pour reconstruire les références domaine —
   * évite tout accès aux proxies Hibernate lazy hors session.</p>
   *
   * @param entity entité JPA chargée depuis la base
   * @return POJO domaine immuable
   */
  public static Review toDomain(final ReviewJpaEntity entity) {
    final Listing listing = new Listing();
    listing.setId(entity.getListing().getId());

    final User reviewer = new User();
    reviewer.setId(entity.getReviewer().getId());
    reviewer.setFirstName(entity.getReviewer().getFirstName());

    return new Review(
        entity.getId(),
        listing,
        reviewer,
        entity.getRating(),
        entity.getComment(),
        entity.getCreatedAt()
    );
  }
}