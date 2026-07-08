package com.tortiki.api.application.service;

import com.tortiki.api.application.port.in.ManageAdminListingsUseCase;
import com.tortiki.api.application.port.out.ListingRepository;
import com.tortiki.api.domain.exception.ListingNotFoundException;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.ListingStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation du port primaire {@link ManageAdminListingsUseCase}.
 *
 * <p>Couche {@code application/service} : orchestre les appels au port
 * secondaire {@link ListingRepository} sans jamais dépendre de JPA,
 * Spring Web ou d'un quelconque détail d'infrastructure.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminListingService implements ManageAdminListingsUseCase {

  private final ListingRepository listingRepository;

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public List<Listing> findAll() {
    log.debug("Récupération de toutes les annonces pour modération admin");
    return listingRepository.findAll();
  }

  /** {@inheritDoc} */
  @Override
  @Transactional
  public Listing updateStatus(final Long listingId, final String newStatus) {
    Listing listing = listingRepository.findById(listingId)
        .orElseThrow(() -> new ListingNotFoundException(
            "Annonce introuvable pour l'identifiant " + listingId));

    ListingStatus status = ListingStatus.valueOf(newStatus);
    listing.setStatus(status);

    log.info("Statut de l'annonce {} changé en {} par un administrateur",
        listingId, status);
    return listingRepository.save(listing);
  }
}