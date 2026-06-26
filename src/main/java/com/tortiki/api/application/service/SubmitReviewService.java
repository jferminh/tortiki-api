package com.tortiki.api.application.service;

import com.tortiki.api.application.port.in.SubmitReviewUseCase;
import com.tortiki.api.application.port.out.ContactRequestRepository;
import com.tortiki.api.application.port.out.ListingRepository;
import com.tortiki.api.application.port.out.ReviewRepository;
import com.tortiki.api.application.port.out.UserRepository;
import com.tortiki.api.domain.exception.ListingNotFoundException;
import com.tortiki.api.domain.exception.ReviewAlreadyExistsException;
import com.tortiki.api.domain.exception.ReviewNotAllowedException;
import com.tortiki.api.domain.exception.UserNotFoundException;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.Review;
import com.tortiki.api.domain.model.User;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service applicatif de soumission d'une évaluation.
 *
 * <p>Orchestre les quatre règles métier dans l'ordre :</p>
 * <ol>
 *   <li>L'annonce doit exister.</li>
 *   <li>L'acheteur doit exister et avoir un compte actif.</li>
 *   <li>Sa demande de contact doit être au statut {@code CONFIRMED}.</li>
 *   <li>Il ne doit pas avoir déjà évalué cette annonce.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmitReviewService implements SubmitReviewUseCase {

  private final ReviewRepository reviewRepository;
  private final ListingRepository listingRepository;
  private final UserRepository userRepository;
  private final ContactRequestRepository contactRequestRepository;
  private final Clock clock;

  /**
   * {@inheritDoc}
   *
   * @throws ListingNotFoundException    si l'annonce est introuvable
   * @throws UserNotFoundException       si l'acheteur est introuvable ou inactif
   * @throws ReviewNotAllowedException   si aucune demande confirmée n'existe
   * @throws ReviewAlreadyExistsException si une évaluation existe déjà
   */
  @Override
  public Review submit(final Command command) {
    log.info("Soumission évaluation annonce {} par {}",
        command.listingId(), command.reviewerEmail());

    Listing listing = listingRepository.findById(command.listingId())
        .orElseThrow(() -> {
          log.warn("Annonce introuvable : id={}", command.listingId());
          return new ListingNotFoundException(
              "Annonce introuvable pour l'identifiant : " + command.listingId());
        });

    User reviewer = userRepository.findByEmailAndEnabledTrue(command.reviewerEmail())
        .orElseThrow(() -> {
          log.warn("Acheteur introuvable ou inactif : email={}", command.reviewerEmail());
          return new UserNotFoundException(command.reviewerEmail());
        });

    if (!contactRequestRepository.existsConfirmedByListingIdAndBuyerId(
        listing.getId(), reviewer.getId())) {
      log.warn("Évaluation refusée — pas de demande confirmée : buyerId={} listingId={}",
          reviewer.getId(), listing.getId());
      throw new ReviewNotAllowedException(
          "Évaluation non autorisée", listing.getId());
    }

    if (reviewRepository.existsByListingIdAndReviewerId(
        listing.getId(), reviewer.getId())) {
      log.warn("Doublon évaluation détecté : reviewerId={} listingId={}",
          reviewer.getId(), listing.getId());
      throw new ReviewAlreadyExistsException(
          "Évaluation déjà soumise pour ce vendeur", listing.getId());
    }

    Review review = new Review(
        null,
        listing,
        reviewer,
        command.rating(),
        command.comment(),
        LocalDateTime.now(clock)
    );

    Review saved = reviewRepository.save(review);
    log.info("Évaluation créée : id={} annonce={} acheteur={} note={}",
        saved.getId(), listing.getId(), reviewer.getId(), command.rating());
    return saved;
  }
}