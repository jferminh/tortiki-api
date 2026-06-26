package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.application.port.out.ReviewRepository;
import com.tortiki.api.domain.model.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adaptateur secondaire — implémentation JPA du port {@link ReviewRepository}.
 *
 * <p>La contrainte d'unicité est portée par {@code contact_request_id UNIQUE}
 * en base (V1). L'existence d'un doublon est vérifiée via la demande
 * de contact confirmée associée à l'annonce et à l'acheteur.</p>
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
  public Review save(final Review review) {
    ContactRequestJpaEntity contactRequestRef =
        contactRequestJpaRepository.findConfirmedByListingIdAndBuyerId(
                review.getListing().getId(), review.getReviewer().getId())
            .orElseThrow(() -> new IllegalStateException(
                "Aucune demande confirmée trouvée pour la sauvegarde de l'évaluation"));

    ListingJpaEntity listingRef =
        listingJpaRepository.getReferenceById(review.getListing().getId());
    UserJpaEntity reviewerRef =
        userJpaRepository.getReferenceById(review.getReviewer().getId());
    UserJpaEntity sellerRef =
        userJpaRepository.getReferenceById(
            contactRequestRef.getListing().getSeller().getId());

    ReviewJpaEntity entity = ReviewPersistenceMapper.toEntity(
        review, contactRequestRef, listingRef, reviewerRef, sellerRef);
    ReviewJpaEntity saved = reviewJpaRepository.save(entity);
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