package com.tortiki.api.infrastructure.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository Spring Data JPA pour {@link ContactRequestJpaEntity}.
 *
 * <p>Interface technique de la couche persistence — ne doit jamais
 * être injectée directement dans la couche application.
 * L'accès passe obligatoirement par {@link ContactRequestRepositoryAdapter}.</p>
 */
public interface ContactRequestJpaRepository
    extends JpaRepository<ContactRequestJpaEntity, Long> {

  /**
   * Vérifie l'existence d'une demande pour une annonce et un acheteur donnés.
   *
   * @param listingId identifiant de l'annonce
   * @param buyerId   identifiant de l'acheteur
   * @return {@code true} si une demande existe déjà
   */
  boolean existsByListingIdAndBuyerId(Long listingId, Long buyerId);

  /**
   * Récupère toutes les demandes associées à une annonce.
   *
   * @param listingId identifiant de l'annonce
   * @return liste des entités demandes
   */
  List<ContactRequestJpaEntity> findByListingId(Long listingId);

  /**
   * Récupère toutes les demandes soumises par un acheteur.
   *
   * @param buyerId identifiant de l'acheteur
   * @return liste des entités demandes
   */
  List<ContactRequestJpaEntity> findByBuyerId(Long buyerId);
}