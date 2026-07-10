package com.tortiki.api.application.port.in;

import com.tortiki.api.domain.model.Review;
import java.util.List;

/**
 * Port primaire — cas d'usage de consultation des évaluations d'une annonce.
 *
 * <p>Distinct de {@link SubmitReviewUseCase}, qui gère uniquement l'écriture.
 * Sépare clairement lecture et écriture (principe CQS — Command Query
 * Separation), cohérent avec la séparation déjà appliquée entre
 * {@code ManageListingUseCase.findBySeller} (lecture) et {@code create}/
 * {@code update} (écriture).</p>
 */
public interface FindReviewsUseCase {

  /**
   * Retourne toutes les évaluations publiées pour une annonce donnée.
   *
   * <p>Endpoint public — aucune authentification requise. Les avis sont
   * un contenu de confiance affiché sur la fiche plat, consultable par
   * tout visiteur, acheteur ou vendeur.</p>
   *
   * @param listingId identifiant de l'annonce évaluée
   * @return liste des évaluations, triée par date de création décroissante,
   *     vide si aucune évaluation n'existe encore
   */
  List<Review> findByListingId(Long listingId);
}