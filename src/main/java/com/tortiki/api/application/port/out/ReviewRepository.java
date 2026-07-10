package com.tortiki.api.application.port.out;

import com.tortiki.api.domain.model.Review;
import java.util.List;

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

  /**
   * Retourne toutes les évaluations d'une annonce, triées par date
   * de création décroissante.
   *
   * <p>Utilisé par {@code FindReviewsService} pour la consultation
   * publique des avis sur la fiche plat. Distinct de
   * {@link #existsByListingIdAndReviewerId(Long, Long)} qui sert
   * uniquement à la règle métier anti-doublon côté écriture.</p>
   *
   * @param listingId identifiant de l'annonce évaluée
   * @return liste des évaluations, vide si aucune n'existe encore
   */
  List<Review> findByListingId(Long listingId);
}