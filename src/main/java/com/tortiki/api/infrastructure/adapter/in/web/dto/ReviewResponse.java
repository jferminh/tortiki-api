// infrastructure/adapter/in/web/dto/ReviewResponse.java

package com.tortiki.api.infrastructure.adapter.in.web.dto;

import com.tortiki.api.domain.model.Review;
import java.time.LocalDateTime;

/**
 * DTO de réponse représentant une évaluation.
 *
 * <p>Utilisé à la fois pour la création ({@code POST /api/v1/reviews})
 * et pour la consultation publique ({@code GET /api/v1/reviews}).
 * Expose uniquement le prénom du reviewer — jamais son email,
 * conformément à la minimisation RGPD déjà appliquée sur
 * {@code ListingResponse.sellerEmail} (email autorisé côté vendeur
 * uniquement, jamais côté acheteur anonyme).</p>
 *
 * @param id               identifiant de l'évaluation
 * @param listingId        identifiant de l'annonce évaluée
 * @param reviewerFirstName prénom de l'auteur de l'évaluation
 * @param rating           note attribuée
 * @param comment          commentaire libre
 * @param createdAt        date de création
 */
public record ReviewResponse(
    Long id,
    Long listingId,
    String reviewerFirstName,
    Integer rating,
    String comment,
    LocalDateTime createdAt
) {

  /**
   * Construit le DTO depuis le POJO domaine {@link Review}.
   *
   * @param review POJO domaine
   * @return DTO prêt à sérialiser
   */
  public static ReviewResponse from(final Review review) {
    return new ReviewResponse(
        review.getId(),
        review.getListing() != null ? review.getListing().getId() : null,
        review.getReviewer() != null ? review.getReviewer().getFirstName() : null,
        review.getRating(),
        review.getComment(),
        review.getCreatedAt()
    );
  }
}