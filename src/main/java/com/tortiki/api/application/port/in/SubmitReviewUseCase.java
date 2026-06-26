package com.tortiki.api.application.port.in;

import com.tortiki.api.domain.model.Review;

/**
 * Port primaire — cas d'usage de soumission d'une évaluation.
 *
 * <p>Un acheteur peut noter une annonce uniquement si sa demande
 * de contact est au statut {@code CONFIRMED}.
 * Une seule évaluation par acheteur par annonce est autorisée.</p>
 */
public interface SubmitReviewUseCase {

  /**
   * Soumet une évaluation pour une annonce.
   *
   * @param command commande contenant les données de l'évaluation
   * @return évaluation persistée
   */
  Review submit(Command command);

  /**
   * Commande immuable de soumission d'évaluation.
   *
   * @param listingId     identifiant de l'annonce évaluée
   * @param reviewerEmail email de l'acheteur authentifié
   * @param rating        note de 1 à 5 inclus
   * @param comment       commentaire libre (optionnel, peut être null)
   */
  record Command(
      Long listingId,
      String reviewerEmail,
      Integer rating,
      String comment
  ) {}
}