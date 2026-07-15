package com.tortiki.api.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
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

  /**
   * Recherche les demandes reçues pour les annonces d'un vendeur, avec
   * l'annonce et l'acheteur chargés en une seule requête.
   *
   * <p>Le {@code JOIN FETCH} est indispensable ici : {@code
   * ContactRequestSummaryResponse} a besoin de {@code listing.title}
   * et {@code buyer.firstName}, deux champs lazy par défaut. Sans cette
   * anticipation, le mapping produirait des champs {@code null} ou lèverait
   * une {@code LazyInitializationException} hors session Hibernate.</p>
   *
   * @param sellerId identifiant du vendeur propriétaire des annonces
   * @return liste des demandes reçues, triées par date de création décroissante
   */
  @Query("SELECT cr FROM ContactRequestJpaEntity cr"
      + " JOIN FETCH cr.listing"
      + " JOIN FETCH cr.buyer"
      + " WHERE cr.listing.seller.id = :sellerId"
      + " ORDER BY cr.createdAt DESC")
  List<ContactRequestJpaEntity> findBySellerId(@Param("sellerId") Long sellerId);

  /**
   * Recherche une demande par son identifiant et le vendeur propriétaire.
   *
   * <p>Contrôle d'accès métier — un vendeur ne peut accéder
   * qu'aux demandes de ses propres annonces. {@code JOIN FETCH} nécessaire
   * pour le même motif que {@link #findBySellerId(Long)}.</p>
   *
   * @param contactRequestId identifiant de la demande
   * @param sellerId         identifiant du vendeur propriétaire
   * @return la demande si elle appartient bien au vendeur
   */
  @Query("SELECT cr FROM ContactRequestJpaEntity cr"
      + " JOIN FETCH cr.listing"
      + " JOIN FETCH cr.buyer"
      + " WHERE cr.id = :contactRequestId"
      + " AND cr.listing.seller.id = :sellerId")
  Optional<ContactRequestJpaEntity> findByIdForSeller(
      @Param("contactRequestId") Long contactRequestId,
      @Param("sellerId") Long sellerId);

  /**
   * Vérifie qu'une demande confirmée existe entre un acheteur et une annonce.
   *
   * @param listingId identifiant de l'annonce
   * @param buyerId   identifiant de l'acheteur
   * @return {@code true} si une demande CONFIRMED existe
   */
  @Query("SELECT COUNT(cr) > 0 FROM ContactRequestJpaEntity cr "
      + "WHERE cr.listing.id = :listingId "
      + "AND cr.buyer.id = :buyerId "
      + "AND cr.status = 'CONFIRMED'")
  boolean existsConfirmedByListingIdAndBuyerId(
      @Param("listingId") Long listingId,
      @Param("buyerId") Long buyerId);

  /**
   * Récupère la demande confirmée d'un acheteur pour une annonce donnée.
   *
   * <p>Utilisé par {@code ReviewRepositoryAdapter} pour résoudre
   * la FK {@code contact_request_id} lors de la persistance d'une évaluation.</p>
   *
   * @param listingId identifiant de l'annonce
   * @param buyerId   identifiant de l'acheteur
   * @return demande confirmée si elle existe
   */
  @Query(
        """
        SELECT cr FROM ContactRequestJpaEntity cr
        JOIN FETCH cr.listing l
        JOIN FETCH l.seller
        WHERE cr.listing.id = :listingId
            AND cr.buyer.id = :buyerId
            AND cr.status = 'CONFIRMED'
        """)
  Optional<ContactRequestJpaEntity> findConfirmedByListingIdAndBuyerId(
      @Param("listingId") Long listingId,
      @Param("buyerId") Long buyerId);
}