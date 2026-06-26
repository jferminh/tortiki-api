package com.tortiki.api.application.port.out;

import com.tortiki.api.domain.model.Review;

/**
 * Port secondaire — contrat de persistance des évaluations.
 *
 * <p>Interface du domaine applicatif : aucune référence JPA ou Spring Data.
 * L'implémentation concrète est dans {@code infrastructure/adapter/out/persistence/}.</p>
 */
public interface ReviewRepository {

  /**
   * Persiste une nouvelle évaluation.
   *
   * @param review évaluation à sauvegarder
   * @return évaluation persistée avec son identifiant généré
   */
  Review save(Review review);

  /**
   * Vérifie l'existence d'une évaluation pour une annonce et un acheteur donnés.
   *
   * @param listingId  identifiant de l'annonce
   * @param reviewerId identifiant de l'acheteur
   * @return {@code true} si une évaluation existe déjà
   */
  boolean existsByListingIdAndReviewerId(Long listingId, Long reviewerId);
}