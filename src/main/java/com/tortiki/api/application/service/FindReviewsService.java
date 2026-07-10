package com.tortiki.api.application.service;

import com.tortiki.api.application.port.in.FindReviewsUseCase;
import com.tortiki.api.application.port.out.ReviewRepository;
import com.tortiki.api.domain.model.Review;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service applicatif de consultation des évaluations d'une annonce.
 *
 * <p>Implémente {@link FindReviewsUseCase} — lecture pure, aucune
 * mutation d'état. Distinct de {@code SubmitReviewService} qui gère
 * l'écriture, conformément au principe CQS déjà appliqué dans ce
 * module.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FindReviewsService implements FindReviewsUseCase {

  private final ReviewRepository reviewRepository;

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(readOnly = true)
  public List<Review> findByListingId(final Long listingId) {
    log.debug("Recherche des évaluations pour l'annonce id={}", listingId);
    return reviewRepository.findByListingId(listingId);
  }
}