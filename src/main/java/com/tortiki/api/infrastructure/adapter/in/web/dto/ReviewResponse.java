package com.tortiki.api.infrastructure.adapter.in.web.dto;

import com.tortiki.api.domain.model.Review;
import java.time.LocalDateTime;

/**
 * DTO de réponse représentant une évaluation créée.
 *
 * @param id        identifiant de l'évaluation
 * @param listingId identifiant de l'annonce évaluée
 * @param rating    note attribuée
 * @param comment   commentaire libre
 * @param createdAt date de création
 */
public record ReviewResponse(
    Long id,
    Long listingId,
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
        review.getRating(),
        review.getComment(),
        review.getCreatedAt()
    );
  }
}