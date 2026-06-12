package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.ListingStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository Spring Data JPA pour les annonces.
 *
 * <p>Interface technique — ne doit jamais être injectée hors de
 * {@link ListingJpaAdapter}. Le domaine passe toujours par le port
 * {@code ListingRepository}.</p>
 */
public interface ListingJpaRepository extends JpaRepository<ListingJpaEntity, Long> {

  /**
   * Retourne les annonces d'un vendeur selon un statut donné.
   *
   * @param sellerId identifiant du vendeur
   * @param status   statut recherché
   * @return liste des entités JPA correspondantes
   */
  List<ListingJpaEntity> findBySellerIdAndStatus(Long sellerId, ListingStatus status);

  /**
   * Retourne toutes les annonces ayant un statut donné.
   *
   * @param status statut recherché
   * @return liste des entités JPA correspondantes
   */
  List<ListingJpaEntity> findByStatus(ListingStatus status);

  /**
   * Vérifie l'existence d'au moins une annonce pour une origine culinaire et un statut.
   *
   * <p>Utilisé par {@link CuisineTypeJpaAdapter#isUsedByActiveListing(Long)}
   * pour bloquer la suppression d'une origine référencée.</p>
   *
   * @param cuisineTypeId identifiant de l'origine culinaire
   * @param status        statut à vérifier
   * @return {@code true} si au moins une annonce existe
   */
  boolean existsByCuisineTypeIdAndStatus(Long cuisineTypeId, ListingStatus status);
}