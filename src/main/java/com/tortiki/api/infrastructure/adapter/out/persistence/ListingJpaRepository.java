// infrastructure/adapter/out/persistence/ListingJpaRepository.java
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
}