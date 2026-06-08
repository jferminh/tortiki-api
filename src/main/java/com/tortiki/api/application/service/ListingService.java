package com.tortiki.api.application.service;

import com.tortiki.api.application.port.in.ListingCommand;
import com.tortiki.api.application.port.in.ManageListingUseCase;
import com.tortiki.api.application.port.out.CuisineTypeRepository;
import com.tortiki.api.application.port.out.ListingRepository;
import com.tortiki.api.application.port.out.UserRepository;
import com.tortiki.api.domain.exception.CuisineTypeNotFoundException;
import com.tortiki.api.domain.exception.ListingNotFoundException;
import com.tortiki.api.domain.exception.UnauthorizedActionException;
import com.tortiki.api.domain.exception.UserNotFoundException;
import com.tortiki.api.domain.model.CuisineType;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.ListingStatus;
import com.tortiki.api.domain.model.User;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier gérant les annonces de plats cuisinés.
 *
 * <p>Implémente le port primaire {@link ManageListingUseCase}.
 * Dépend des ports secondaires {@link ListingRepository},
 * {@link UserRepository} et {@link CuisineTypeRepository} —
 * aucune dépendance directe vers JPA ou la base de données.</p>
 *
 * <p>La règle de propriété est appliquée ici : seul le vendeur
 * propriétaire peut modifier ou supprimer son annonce.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class ListingService implements ManageListingUseCase {

  /** Message d'erreur pour une annonce introuvable. */
  private static final String LISTING_NOT_FOUND =
      "Annonce introuvable pour l'identifiant : ";

  /** Message d'erreur pour un vendeur introuvable. */
  private static final String SELLER_NOT_FOUND =
      "Vendeur introuvable pour l'identifiant : ";

  /** Message d'erreur pour une origine culinaire introuvable. */
  private static final String CUISINE_TYPE_NOT_FOUND =
      "Origine culinaire introuvable pour l'identifiant : ";

  /** Port secondaire de persistance des annonces. */
  private final ListingRepository listingRepository;

  /** Port secondaire de persistance des utilisateurs. */
  private final UserRepository userRepository;

  /** Port secondaire de persistance des origines culinaires. */
  private final CuisineTypeRepository cuisineTypeRepository;

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  public Listing create(Long sellerId, ListingCommand command) {
    log.debug("Création d'une annonce pour le vendeur id={}", sellerId);

    User seller = userRepository.findById(sellerId)
        .orElseThrow(() -> new UserNotFoundException(
            SELLER_NOT_FOUND + sellerId
        ));

    CuisineType cuisineType = cuisineTypeRepository.findById(command.cuisineTypeId())
        .orElseThrow(() -> new CuisineTypeNotFoundException(
            CUISINE_TYPE_NOT_FOUND + command.cuisineTypeId()
        ));

    Listing listing = new Listing();
    listing.setSeller(seller);
    listing.setTitle(command.title());
    listing.setDescription(command.description());
    listing.setPrice(command.price());
    listing.setPortions(command.portions());
    listing.setPickupSlot(command.pickupSlot());
    listing.setCity(command.city());
    listing.setPostalCode(command.postalCode());
    listing.setCuisineType(cuisineType);
    listing.setStatus(ListingStatus.ACTIVE);
    listing.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

    Listing saved = listingRepository.save(listing);
    log.info("Annonce créée : id={} vendeur={}", saved.getId(), sellerId);
    return saved;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  public Listing update(Long listingId, Long sellerId, ListingCommand command) {
    log.debug("Mise à jour annonce id={} par vendeur id={}", listingId, sellerId);

    Listing existing = getListingOwnedBySeller(listingId, sellerId);
    existing.setTitle(command.title());
    existing.setDescription(command.description());
    existing.setPrice(command.price());
    existing.setPortions(command.portions());
    existing.setPickupSlot(command.pickupSlot());
    existing.setCity(command.city());
    existing.setPostalCode(command.postalCode());

    Listing updated = listingRepository.save(existing);
    log.info("Annonce mise à jour : id={}", listingId);
    return updated;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  public Listing updatePhoto(Long listingId, Long sellerId, String photoUrl) {
    log.debug("Mise à jour photo annonce id={}", listingId);
    Listing existing = getListingOwnedBySeller(listingId, sellerId);
    existing.setPhotoUrl(photoUrl);
    Listing updated = listingRepository.save(existing);
    log.info("Photo mise à jour annonce id={}", listingId);
    return updated;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Suppression logique : le statut passe à {@code INACTIVE},
   * l'annonce reste en base pour l'historique.</p>
   */
  @Override
  @Transactional
  public void delete(Long listingId, Long sellerId) {
    log.debug("Suppression logique annonce id={} par vendeur id={}", listingId, sellerId);
    Listing existing = getListingOwnedBySeller(listingId, sellerId);
    existing.setStatus(ListingStatus.INACTIVE);
    listingRepository.save(existing);
    log.info("Annonce désactivée (suppression logique) : id={}", listingId);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public List<Listing> findAll() {
    log.debug("Récupération de toutes les annonces actives");
    return listingRepository.findByStatus(ListingStatus.ACTIVE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(readOnly = true)
  public List<Listing> findBySeller(Long sellerId) {
    log.debug("Récupération annonces actives vendeur id={}", sellerId);
    return listingRepository.findBySellerIdAndStatus(sellerId, ListingStatus.ACTIVE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(readOnly = true)
  public Listing findById(Long listingId) {
    log.debug("Recherche annonce id={}", listingId);
    return listingRepository.findById(listingId)
        .orElseThrow(() -> new ListingNotFoundException(
            LISTING_NOT_FOUND + listingId
        ));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  public Listing changeStatus(Long listingId, ListingStatus status) {
    log.debug("Changement statut annonce id={} vers {}", listingId, status);
    Listing existing = listingRepository.findById(listingId)
        .orElseThrow(() -> new ListingNotFoundException(
            LISTING_NOT_FOUND + listingId
        ));
    existing.setStatus(status);
    Listing updated = listingRepository.save(existing);
    log.info("Statut annonce id={} changé vers {}", listingId, status);
    return updated;
  }

  /**
   * Récupère une annonce et vérifie qu'elle appartient au vendeur demandeur.
   *
   * <p>Méthode privée mutualisée pour éviter la duplication de la
   * logique de propriété dans {@code update}, {@code updatePhoto}
   * et {@code delete}.</p>
   *
   * @param listingId identifiant de l'annonce
   * @param sellerId  identifiant du vendeur demandeur
   * @return l'annonce si elle existe et appartient au vendeur
   * @throws ListingNotFoundException     si l'annonce est introuvable
   * @throws UnauthorizedActionException  si le vendeur n'est pas propriétaire
   */
  private Listing getListingOwnedBySeller(Long listingId, Long sellerId) {
    Listing listing = listingRepository.findById(listingId)
        .orElseThrow(() -> new ListingNotFoundException(
            LISTING_NOT_FOUND + listingId
        ));
    if (!listing.getSeller().getId().equals(sellerId)) {
      throw new UnauthorizedActionException(
          "Le vendeur id=" + sellerId
              + " n'est pas propriétaire de l'annonce id=" + listingId
      );
    }
    return listing;
  }
}