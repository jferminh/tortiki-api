package com.tortiki.api.application.port.out;

import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.ListingStatus;
import java.util.List;
import java.util.Optional;

/**
 * Port secondaire — contrat de persistance des annonces.
 *
 * <p>Ne dépend d'aucune technologie JPA ou SQL. L'implémentation
 * est assurée par {@code ListingJpaAdapter} dans
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
   * Recherche toutes les annonces appartenant à un vendeur donné,
   * quel que soit leur statut, triées par date de création décroissante.
   *
   * @param sellerId identifiant du vendeur
   * @return liste des annonces du vendeur
   */
  List<Listing> findBySellerId(Long sellerId);

  /**
   * Retourne l'intégralité des annonces de la plateforme, tous vendeurs
   * et tous statuts confondus.
   *
   * <p>Réservé à l'usage administrateur via
   * {@code ManageAdminListingsUseCase}. Aucun autre cas d'usage ne doit
   * appeler cette méthode : la modération admin est le seul contexte
   * légitime pour consulter des annonces sans filtre de statut ni de
   * vendeur.</p>
   *
   * @return liste complète des annonces, vide si aucune n'existe
   */
  List<Listing> findAll();

  /**
   * Récupère la liste des villes distinctes ayant au moins une annonce active.
   *
   * <p>Utilisé pour alimenter l'autocomplétion de recherche côté frontend,
   * sans exposer d'identifiant ni de donnée personnelle du vendeur.</p>
   *
   * @return liste triée des villes distinctes, annonces {@code ACTIVE} uniquement
   */
  List<String> findDistinctActiveCities();
}