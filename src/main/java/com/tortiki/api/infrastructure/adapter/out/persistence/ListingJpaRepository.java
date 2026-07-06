package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.ListingStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository Spring Data JPA pour les annonces.
 *
 * <p>Interface technique — ne doit jamais être injectée hors de
 * {@link ListingJpaAdapter}. Le domaine passe toujours par le port
 * {@code ListingRepository}.</p>
 */
public interface ListingJpaRepository extends JpaRepository<ListingJpaEntity, Long> {

  /**
   * Recherche une annonce par identifiant avec toutes les associations
   * nécessaires au mapping domaine chargées en eager via JOIN FETCH.
   *
   * <p>Initialise {@code seller}, {@code cuisineType} et {@code allergens}
   * pour éviter toute {@code LazyInitializationException} dans
   * {@link ListingPersistenceMapper#toDomain(ListingJpaEntity)}.</p>
   *
   * <p>{@code DISTINCT} évite les doublons produits par le JOIN sur
   * la collection {@code allergens} (relation ManyToMany).</p>
   *
   * @param id identifiant de l'annonce
   * @return l'annonce avec toutes les associations initialisées, vide si absente
   */
  @Query(
      """
      SELECT DISTINCT l FROM ListingJpaEntity l
      JOIN FETCH l.seller
      JOIN FETCH l.cuisineType
      LEFT JOIN FETCH l.allergens
      WHERE l.id = :id
      """)
  Optional<ListingJpaEntity> findByIdWithSeller(@Param("id") Long id);

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

  /**
   * Recherche les entités annonce d'un vendeur, toutes statuts confondus,
   * avec les associations {@code cuisineType} et {@code allergens} chargées
   * en eager via JOIN FETCH.
   *
   * <p>Évite le problème N+1 : sans cette jointure explicite, chaque annonce
   * de la liste déclencherait une requête supplémentaire pour charger son
   * type de cuisine et ses allergènes lors du mapping vers le domaine
   * (voir {@link ListingPersistenceMapper#toDomain(ListingJpaEntity)}).</p>
   *
   * <p>{@code DISTINCT} évite les doublons produits par le LEFT JOIN sur
   * la collection {@code allergens} (relation ManyToMany), comme pour
   * {@link #findByIdWithSeller(Long)}.</p>
   *
   * @param sellerId identifiant du vendeur
   * @return liste des entités annonce du vendeur, triées par date de création décroissante
   */
  @Query(
      """
      SELECT DISTINCT l FROM ListingJpaEntity l
      JOIN FETCH l.cuisineType
      LEFT JOIN FETCH l.allergens
      WHERE l.seller.id = :sellerId
      ORDER BY l.createdAt DESC
      """)
  List<ListingJpaEntity> findBySellerIdOrderByCreatedAtDesc(@Param("sellerId") Long sellerId);

  /**
   * Recherche unifiée des annonces actives selon tous les critères optionnels.
   *
   * <p>Filtres appliqués côté base PostgreSQL :</p>
   * <ul>
   *   <li>Rayon géographique via formule Haversine (obligatoire)</li>
   *   <li>Type de cuisine — ignoré si {@code null}</li>
   *   <li>Prix maximum — ignoré si {@code null}</li>
   *   <li>Mot-clé titre/description — ignoré si {@code null}</li>
   * </ul>
   *
   * <p>L'exclusion des allergènes est appliquée en mémoire par
   * {@link SearchListingRepositoryAdapter} après cet appel.</p>
   *
   * <p>Résultats triés par {@code pickupDatetime} croissant et paginés
   * via {@link Pageable}.</p>
   *
   * @param lat           latitude du centre de recherche
   * @param lng           longitude du centre de recherche
   * @param radiusKm      rayon en kilomètres
   * @param cuisineTypeId filtre type de cuisine, {@code null} = tous
   * @param maxPrice      prix maximum, {@code null} = sans limites
   * @param query         mot-clé titre/description, {@code null} = sans filtre
   * @param pageable      pagination (page + taille)
   * @return liste paginée des entités correspondantes
   */
  @Query(
      """
      SELECT DISTINCT l FROM ListingJpaEntity l
      WHERE l.status = 'ACTIVE'
      AND (6371 * acos(
        cos(radians(:lat)) * cos(radians(l.pickupLat))
        * cos(radians(l.pickupLng) - radians(:lng))
        + sin(radians(:lat)) * sin(radians(l.pickupLat))
      )) <= :radiusKm
      AND (:cuisineTypeId IS NULL OR l.cuisineType.id = :cuisineTypeId)
      AND (:maxPrice IS NULL OR l.price <= :maxPrice)
      AND (:query IS NULL
        OR LOWER(CAST(l.title AS string))
             LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))
        OR LOWER(CAST(l.description AS string))
             LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')))
      ORDER BY l.pickupDatetime ASC
      """)
  List<ListingJpaEntity> searchByCriteria(
      @Param("lat") BigDecimal lat,
      @Param("lng") BigDecimal lng,
      @Param("radiusKm") double radiusKm,
      @Param("cuisineTypeId") Long cuisineTypeId,
      @Param("maxPrice") BigDecimal maxPrice,
      @Param("query") String query,
      Pageable pageable);
}