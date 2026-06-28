package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.application.port.out.ReviewRepository;
import com.tortiki.api.domain.model.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptateur secondaire — implémentation JPA du port {@link ReviewRepository}.
 *
 * <p>La contrainte d'unicité est portée par {@code contact_request_id UNIQUE}
 * en base (V5). L'accès au vendeur est sécurisé par un {@code JOIN FETCH}
 * dans {@code ContactRequestJpaRepository.findConfirmedByListingIdAndBuyerId()}
 * — évite toute {@code LazyInitializationException} sur {@code listing.seller}.</p>
 */
@Component
@RequiredArgsConstructor
public class ReviewRepositoryAdapter implements ReviewRepository {

  private final ReviewJpaRepository reviewJpaRepository;
  private final ListingJpaRepository listingJpaRepository;
  private final UserJpaRepository userJpaRepository;
  private final ContactRequestJpaRepository contactRequestJpaRepository;

  /** {@inheritDoc} */
  @Override
  @Transactional
  public Review save(final Review review) {
    final Long listingId = review.getListing().getId();
    final Long buyerId = review.getReviewer().getId();

    // JOIN FETCH listing + seller dans la requête — pas de proxy lazy
    final ContactRequestJpaEntity contactRequestRef =
        contactRequestJpaRepository.findConfirmedByListingIdAndBuyerId(listingId, buyerId)
            .orElseThrow(() -> new IllegalStateException(
                "Aucune demande confirmée trouvée pour la sauvegarde de l'évaluation"));

    final ListingJpaEntity listingRef =
        listingJpaRepository.getReferenceById(listingId);
    final UserJpaEntity reviewerRef =
        userJpaRepository.getReferenceById(buyerId);

    // getSeller() initialisé par JOIN FETCH — zéro SELECT supplémentaire
    final UserJpaEntity sellerRef =
        userJpaRepository.getReferenceById(
            contactRequestRef.getListing().getSeller().getId());

    final ReviewJpaEntity entity = ReviewPersistenceMapper.toEntity(
        review, contactRequestRef, listingRef, reviewerRef, sellerRef);
    final ReviewJpaEntity saved = reviewJpaRepository.save(entity);
    return ReviewPersistenceMapper.toDomain(saved);
  }

  /** {@inheritDoc} */
  @Override
  public boolean existsByListingIdAndReviewerId(
      final Long listingId,
      final Long reviewerId) {
    return reviewJpaRepository.existsByListingIdAndReviewerId(listingId, reviewerId);
  }
}