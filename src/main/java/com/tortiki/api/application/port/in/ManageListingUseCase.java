package com.tortiki.api.application.port.in;

import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.ListingStatus;
import java.util.List;

/**
 * Port primaire — cas d'usage : gestion des annonces de plats.
 *
 * <p>Regroupe les opérations CRUD sur les annonces ainsi que
 * la gestion du statut. L'implémentation est assurée par
 * {@code ListingService} dans la couche {@code application/service/}.</p>
 *
 * <p>Ce port est appelé par {@code ListingController} dans
 * {@code infrastructure/adapter/in/web/}.</p>
 */
public interface ManageListingUseCase {

  /**
   * Crée une nouvelle annonce de plat pour un vendeur.
   *
   * @param sellerId identifiant du vendeur propriétaire
   * @param command  données de l'annonce à créer
   * @return l'annonce créée avec son identifiant technique
   * @throws com.tortiki.api.domain.exception.UserNotFoundException
   *         si le vendeur est introuvable
   * @throws com.tortiki.api.domain.exception.CuisineTypeNotFoundException
   *         si l'origine culinaire est introuvable
   */
  Listing create(Long sellerId, ListingCommand command);

  /**
   * Met à jour une annonce existante.
   *
   * <p>Seul le vendeur propriétaire peut modifier son annonce.</p>
   *
   * @param listingId identifiant de l'annonce à modifier
   * @param sellerId  identifiant du vendeur demandant la modification
   * @param command   nouvelles données de l'annonce
   * @return l'annonce mise à jour
   * @throws com.tortiki.api.domain.exception.ListingNotFoundException
   *         si l'annonce est introuvable
   * @throws com.tortiki.api.domain.exception.UnauthorizedActionException
   *         si le vendeur n'est pas propriétaire de l'annonce
   */
  Listing update(Long listingId, Long sellerId, ListingCommand command);

  /**
   * Met à jour la photo d'une annonce après upload MinIO.
   *
   * @param listingId identifiant de l'annonce
   * @param sellerId  identifiant du vendeur propriétaire
   * @param photoUrl  URL de la photo stockée dans MinIO
   * @return l'annonce avec la nouvelle photo
   * @throws com.tortiki.api.domain.exception.ListingNotFoundException
   *         si l'annonce est introuvable
   * @throws com.tortiki.api.domain.exception.UnauthorizedActionException
   *         si le vendeur n'est pas propriétaire de l'annonce
   */
  Listing updatePhoto(Long listingId, Long sellerId, String photoUrl);

  /**
   * Supprime une annonce (suppression logique — statut {@code INACTIVE}).
   *
   * @param listingId identifiant de l'annonce à supprimer
   * @param sellerId  identifiant du vendeur propriétaire
   * @throws com.tortiki.api.domain.exception.ListingNotFoundException
   *         si l'annonce est introuvable
   * @throws com.tortiki.api.domain.exception.UnauthorizedActionException
   *         si le vendeur n'est pas propriétaire de l'annonce
   */
  void delete(Long listingId, Long sellerId);

  /**
   * Retourne toutes les annonces actives d'un vendeur.
   *
   * @param sellerId identifiant du vendeur
   * @return liste des annonces actives, vide si aucune
   */
  List<Listing> findBySeller(Long sellerId);

  /**
   * Retourne une annonce par son identifiant.
   *
   * @param listingId identifiant de l'annonce
   * @return l'annonce correspondante
   * @throws com.tortiki.api.domain.exception.ListingNotFoundException
   *         si l'annonce est introuvable
   */
  Listing findById(Long listingId);

  /**
   * Change le statut d'une annonce.
   *
   * <p>La modération vers {@code MODERATED}, est réservée au rôle
   * {@code ROLE_ADMIN}. La vérification est faite par Spring Security
   * en amont dans le contrôleur.</p>
   *
   * @param listingId identifiant de l'annonce
   * @param status    nouveau statut
   * @return l'annonce avec le statut mis à jour
   * @throws com.tortiki.api.domain.exception.ListingNotFoundException
   *         si l'annonce est introuvable
   */
  Listing changeStatus(Long listingId, ListingStatus status);
}