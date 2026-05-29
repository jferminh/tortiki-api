package com.tortiki.api.application.port.out;

import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.ListingStatus;
import java.util.List;
import java.util.Optional;

/**
 * Port secondaire — contrat de persistance des annonces.
 *
 * <p>Ne dépend d'aucune technologie JPA ou SQL. L'implémentation
 * est assurée par {@code ListingRepositoryAdapter} dans
 * {@code infrastructure/adapter/out/persistence/}.</p>
 */
public interface ListingRepository {

  /**
   * Persiste une nouvelle annonce ou met à jour une annonce existante.
   *
   * @param listing annonce à persister
   * @return l'annonce persistée avec son identifiant généré
   */
  Listing save(Listing listing);

  /**
   * Recherche une annonce par son identifiant.
   *
   * @param id identifiant de l'annonce
   * @return un {@link Optional} contenant l'annonce, ou vide si absente
   */
  Optional<Listing> findById(Long id);

  /**
   * Retourne toutes les annonces d'un vendeur selon un statut donné.
   *
   * @param sellerId identifiant du vendeur
   * @param status   statut des annonces à récupérer
   * @return liste des annonces correspondantes, vide si aucune
   */
  List<Listing> findBySellerIdAndStatus(Long sellerId, ListingStatus status);

  /**
   * Retourne toutes les annonces ayant un statut donné.
   *
   * @param status statut des annonces à récupérer
   * @return liste des annonces correspondantes, vide si aucune
   */
  List<Listing> findByStatus(ListingStatus status);

  /**
   * Supprime une annonce par son identifiant.
   *
   * @param id identifiant de l'annonce à supprimer
   */
  void deleteById(Long id);
}