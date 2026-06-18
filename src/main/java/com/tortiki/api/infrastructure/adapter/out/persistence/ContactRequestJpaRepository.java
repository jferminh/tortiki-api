package com.tortiki.api.infrastructure.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
   * <p>Requête JPQL explicite — évite l'ambiguïté de la dérivation
   * Spring Data sur les relations {@code @ManyToOne} imbriquées.</p>
   *
   * @param listingId identifiant de l'annonce
   * @param buyerId   identifiant de l'acheteur
   * @return {@code true} si une demande existe déjà
   */
  @Query("SELECT COUNT(cr) > 0 FROM ContactRequestJpaEntity cr "
      + "WHERE cr.listing.id = :listingId AND cr.buyer.id = :buyerId")
  boolean existsByListingIdAndBuyerId(
      @Param("listingId") Long listingId,
      @Param("buyerId") Long buyerId);

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