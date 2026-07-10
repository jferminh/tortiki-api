package com.tortiki.api.infrastructure.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository Spring Data JPA pour les évaluations.
 *
 * <p>Les noms de méthodes dérivés Spring Data utilisent des underscores
 * pour naviguer dans les associations ({@code listing_id}),
 * ce qui viole la règle Checkstyle Google Style {@code MemberName}.
 * On utilise {@code @Query} JPQL pour rester conforme.</p>
 */
public interface ReviewJpaRepository extends JpaRepository<ReviewJpaEntity, Long> {

  /**
   * Vérifie l'existence d'une évaluation pour une annonce et un acheteur donnés.
   *
   * @param listingId  identifiant de l'annonce
   * @param reviewerId identifiant de l'acheteur
   * @return {@code true} si une évaluation existe déjà
   */
  @Query("SELECT COUNT(r) > 0 FROM ReviewJpaEntity r "
      + "WHERE r.listing.id = :listingId "
      + "AND r.reviewer.id = :reviewerId")
  boolean existsByListingIdAndReviewerId(
      @Param("listingId") Long listingId,
      @Param("reviewerId") Long reviewerId);

  /**
   * Recherche les entités évaluation d'une annonce donnée, avec
   * l'association {@code reviewer} chargée en eager via JOIN FETCH.
   *
   * <p>Évite le problème N+1 : sans cette jointure explicite, chaque
   * évaluation de la liste déclencherait une requête supplémentaire
   * pour charger son auteur, lors du mapping vers
   * {@code ReviewResponse.reviewerFirstName}. Même pattern que
   * {@code ListingJpaRepository.findBySellerIdOrderByCreatedAtDesc}.</p>
   *
   * @param listingId identifiant de l'annonce évaluée
   * @return liste des entités évaluation, triées par date de création
   *     décroissante
   */
  @Query("SELECT r FROM ReviewJpaEntity r "
      + "JOIN FETCH r.reviewer "
      + "WHERE r.listing.id = :listingId "
      + "ORDER BY r.createdAt DESC")
  List<ReviewJpaEntity> findByListingIdWithReviewer(@Param("listingId") Long listingId);
}