package com.tortiki.api.application.service;

import com.tortiki.api.application.port.in.SubmitContactRequestUseCase;
import com.tortiki.api.application.port.out.ContactRequestRepository;
import com.tortiki.api.application.port.out.ListingRepository;
import com.tortiki.api.application.port.out.UserRepository;
import com.tortiki.api.domain.exception.ContactRequestAlreadyExistsException;
import com.tortiki.api.domain.exception.ListingNotFoundException;
import com.tortiki.api.domain.exception.SelfContactException;
import com.tortiki.api.domain.exception.UserNotFoundException;
import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.User;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service applicatif pour la soumission d'une demande de contact.
 *
 * <p>Implémente {@link SubmitContactRequestUseCase} et orchestre
 * les règles métier avant persistance :
 * <ul>
 *   <li>L'annonce doit exister.</li>
 *   <li>L'acheteur doit exister et son compte doit être actif.</li>
 *   <li>L'acheteur ne peut pas contacter sa propre annonce.</li>
 *   <li>Un seul contact par acheteur par annonce (unicité).</li>
 * </ul>
 *
 * <p>Appartient à la couche {@code application/service} —
 * dépend uniquement des ports, jamais des adaptateurs.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactRequestService implements SubmitContactRequestUseCase {

  /** Port secondaire de persistance des demandes de contact. */
  private final ContactRequestRepository contactRequestRepository;

  /** Port secondaire de persistance des annonces. */
  private final ListingRepository listingRepository;

  /** Port secondaire de persistance des utilisateurs. */
  private final UserRepository userRepository;

  /** Horloge injectable — permet le test déterministe. */
  private final Clock clock;

  /**
   * {@inheritDoc}
   *
   * <p>Ordre d'application des règles métier :
   * <ol>
   *   <li>Vérification existence de l'annonce.</li>
   *   <li>Résolution et vérification de l'acheteur (compte actif).</li>
   *   <li>Vérification que l'acheteur n'est pas le vendeur.</li>
   *   <li>Vérification unicité de la demande.</li>
   *   <li>Création et persistance avec statut {@code PENDING}.</li>
   * </ol>
   */
  @Override
  public ContactRequest submit(final Command command) {
    log.info("Soumission demande de contact — annonce {} par {}",
        command.listingId(), command.buyerEmail());

    // Règle 1 — L'annonce doit exister (fail-fast sur la ressource principale)
    Listing listing = listingRepository.findById(command.listingId())
        .orElseThrow(() -> {
          log.warn("Annonce introuvable — id={}", command.listingId());
          return new ListingNotFoundException(
              "Annonce introuvable avec l'identifiant : "
                  + command.listingId()
          );
        });

    // Règle 2 — Résolution de l'acheteur depuis son email (session Spring Security)
    User buyer = userRepository.findByEmailAndEnabledTrue(command.buyerEmail())
        .orElseThrow(() -> {
          log.warn("Acheteur introuvable ou inactif — email={}",
              command.buyerEmail());
          return new UserNotFoundException(
              "Acheteur introuvable ou inactif : "
                  + command.buyerEmail()
          );
        });

    log.debug("Acheteur résolu — buyerId={} email={}",
        buyer.getId(), buyer.getEmail());

    // Règle 3 — L'acheteur ne peut pas contacter sa propre annonce
    if (listing.getSeller().getId().equals(buyer.getId())) {
      log.warn("Self-contact détecté — buyerId={} listingId={}",
          buyer.getId(), command.listingId());
      throw new SelfContactException(
          "L'acheteur " + buyer.getId()
              + " est le vendeur de l'annonce "
              + command.listingId()
      );
    }

    // Règle 4 — Unicité : un seul contact par acheteur par annonce
    if (contactRequestRepository.existsByListingIdAndBuyerId(
        command.listingId(), buyer.getId())) {
      log.warn("Doublon détecté — buyerId={} listingId={}",
          buyer.getId(), command.listingId());
      throw new ContactRequestAlreadyExistsException(
          "Une demande existe déjà pour l'annonce "
              + command.listingId()
              + " par l'acheteur "
              + buyer.getId()
      );
    }

    ContactRequest contactRequest = new ContactRequest();
    contactRequest.setListing(listing);
    contactRequest.setBuyer(buyer);
    contactRequest.setMessage(command.message());
    contactRequest.setPortions(command.portions());
    contactRequest.setStatus(ContactRequestStatus.PENDING);
    contactRequest.setCreatedAt(LocalDateTime.now(clock));

    ContactRequest saved = contactRequestRepository.save(contactRequest);

    log.info("Demande de contact créée — id={} annonce={} acheteur={} statut={}",
        saved.getId(),
        saved.getListing().getId(),
        saved.getBuyer().getId(),
        saved.getStatus());

    return saved;
  }
}