package com.tortiki.api.application.service;

import com.tortiki.api.application.port.in.ManageListingUseCase;
import com.tortiki.api.application.port.out.AllergenRepository;
import com.tortiki.api.application.port.out.CuisineTypeRepository;
import com.tortiki.api.application.port.out.ListingRepository;
import com.tortiki.api.application.port.out.StoragePort;
import com.tortiki.api.application.port.out.UserRepository;
import com.tortiki.api.domain.exception.CuisineTypeNotFoundException;
import com.tortiki.api.domain.exception.ListingNotFoundException;
import com.tortiki.api.domain.exception.StorageException;
import com.tortiki.api.domain.exception.UnauthorizedActionException;
import com.tortiki.api.domain.exception.UserNotFoundException;
import com.tortiki.api.domain.model.Allergen;
import com.tortiki.api.domain.model.CuisineType;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.ListingStatus;
import com.tortiki.api.domain.model.User;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier gérant les annonces de plats cuisinés.
 *
 * <p>Implémente {@link ManageListingUseCase}. Dépend uniquement des ports
 * secondaires — aucune dépendance directe vers JPA, MinIO ou HTTP.</p>
 *
 * <p>Règles métier appliquées :</p>
 * <ul>
 *   <li>Seul le vendeur propriétaire peut modifier ou supprimer son annonce.</li>
 *   <li>La suppression est logique (statut {@code INACTIVE}).</li>
 *   <li>Les allergènes sont chargés depuis {@link AllergenRepository}.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ListingService implements ManageListingUseCase {

  private static final String LISTING_NOT_FOUND = "Annonce introuvable : ";
  private static final String SELLER_NOT_FOUND = "Vendeur introuvable : ";
  private static final String CUISINE_TYPE_NOT_FOUND = "Origine culinaire introuvable : ";

  private final ListingRepository listingRepository;
  private final UserRepository userRepository;
  private final CuisineTypeRepository cuisineTypeRepository;
  private final AllergenRepository allergenRepository;
  private final StoragePort storagePort;

  /** {@inheritDoc} */
  @Override
  @Transactional
  public Listing create(Long sellerId, ManageListingUseCase.Command command) {
    log.debug("Création annonce pour vendeur id={}", sellerId);

    User seller = userRepository.findById(sellerId)
        .orElseThrow(() -> new UserNotFoundException(SELLER_NOT_FOUND + sellerId));

    CuisineType cuisineType = cuisineTypeRepository.findById(command.cuisineTypeId())
        .orElseThrow(() -> new CuisineTypeNotFoundException(
            CUISINE_TYPE_NOT_FOUND + command.cuisineTypeId()));

    List<Allergen> allergens = allergenRepository.findAllByIdIn(command.allergenIds());

    Listing listing = new Listing();
    listing.setSeller(seller);
    listing.setTitle(command.title());
    listing.setDescription(command.description());
    listing.setPrice(command.price());
    listing.setPortions(command.portions());
    listing.setPickupAddress(command.pickupAddress());
    listing.setPickupDatetime(command.pickupDatetime());
    listing.setCuisineType(cuisineType);
    listing.setAllergens(allergens);
    listing.setStatus(ListingStatus.ACTIVE);
    listing.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
    listing.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));

    Listing saved = listingRepository.save(listing);
    log.info("Annonce créée id={} vendeur={}", saved.getId(), sellerId);
    return saved;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public Listing update(Long listingId, Long sellerId, ManageListingUseCase.Command command) {
    log.debug("Mise à jour annonce id={} vendeur id={}", listingId, sellerId);

    Listing existing = getListingOwnedBySeller(listingId, sellerId);
    List<Allergen> allergens = allergenRepository.findAllByIdIn(command.allergenIds());

    existing.setTitle(command.title());
    existing.setDescription(command.description());
    existing.setPrice(command.price());
    existing.setPortions(command.portions());
    existing.setPickupAddress(command.pickupAddress());
    existing.setPickupDatetime(command.pickupDatetime());
    existing.setAllergens(allergens);
    existing.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));

    Listing updated = listingRepository.save(existing);
    log.info("Annonce mise à jour id={}", listingId);
    return updated;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Délègue l'upload à {@link StoragePort} en passant directement
   * les bytes de {@link ManageListingUseCase.PhotoCommand}.
   * La conversion en flux est gérée par l'adaptateur MinIO.</p>
   */
  @Override
  @Transactional
  public Listing updatePhoto(
      Long listingId,
      Long sellerId,
      ManageListingUseCase.PhotoCommand command) {
    log.debug("Upload photo annonce id={} vendeur id={}", listingId, sellerId);

    Listing existing = getListingOwnedBySeller(listingId, sellerId);

    final String fileName = UUID.randomUUID() + "-listing-" + listingId;
    try {
      final String photoUrl = storagePort.upload(
          fileName,
          command.photoBytes(),       // ← byte[] directement, plus de ByteArrayInputStream
          command.contentType()
      );
      existing.setPhotoUrl(photoUrl);
      existing.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
    } catch (Exception ex) {
      throw new StorageException("Échec upload photo annonce id=" + listingId, ex);
    }

    Listing updated = listingRepository.save(existing);
    log.info("Photo uploadée annonce id={}", listingId);
    return updated;
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public void delete(Long listingId, Long sellerId) {
    log.debug("Suppression logique annonce id={} vendeur id={}", listingId, sellerId);
    Listing existing = getListingOwnedBySeller(listingId, sellerId);
    existing.setStatus(ListingStatus.INACTIVE);
    existing.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
    listingRepository.save(existing);
    log.info("Annonce désactivée id={}", listingId);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public List<Listing> findAll() {
    return listingRepository.findByStatus(ListingStatus.ACTIVE);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public List<Listing> findBySeller(Long sellerId) {
    return listingRepository.findBySellerIdAndStatus(sellerId, ListingStatus.ACTIVE);
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public Listing findById(Long listingId) {
    return listingRepository.findById(listingId)
        .orElseThrow(() -> new ListingNotFoundException(LISTING_NOT_FOUND + listingId));
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public Listing changeStatus(Long listingId, ListingStatus status) {
    log.debug("Changement statut annonce id={} vers {}", listingId, status);
    Listing existing = listingRepository.findById(listingId)
        .orElseThrow(() -> new ListingNotFoundException(LISTING_NOT_FOUND + listingId));
    existing.setStatus(status);
    existing.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
    Listing updated = listingRepository.save(existing);
    log.info("Statut annonce id={} → {}", listingId, status);
    return updated;
  }

  /**
   * Récupère une annonce et vérifie qu'elle appartient au vendeur demandeur.
   *
   * @param listingId identifiant de l'annonce
   * @param sellerId  identifiant du vendeur
   * @return l'annonce si elle existe et appartient au vendeur
   * @throws ListingNotFoundException    si l'annonce est introuvable
   * @throws UnauthorizedActionException si le vendeur n'est pas propriétaire
   */
  private Listing getListingOwnedBySeller(Long listingId, Long sellerId) {
    Listing listing = listingRepository.findById(listingId)
        .orElseThrow(() -> new ListingNotFoundException(LISTING_NOT_FOUND + listingId));
    if (!listing.getSeller().getId().equals(sellerId)) {
      throw new UnauthorizedActionException(
          "Vendeur id=" + sellerId + " non propriétaire de l'annonce id=" + listingId
      );
    }
    return listing;
  }
}