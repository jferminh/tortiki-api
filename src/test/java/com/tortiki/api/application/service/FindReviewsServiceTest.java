package com.tortiki.api.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tortiki.api.application.port.out.ReviewRepository;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.Review;
import com.tortiki.api.domain.model.User;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitaires du service {@link FindReviewsService}.
 *
 * <p>Vérifie la consultation en lecture pure des évaluations d'une
 * annonce — cas nominal et cas sans évaluation.</p>
 */
@Epic("Évaluations")
@Feature("Consultation des évaluations")
@ExtendWith(MockitoExtension.class)
@DisplayName("FindReviewsService — Tests unitaires")
class FindReviewsServiceTest {

  private static final Long LISTING_ID = 10L;

  @Mock
  private ReviewRepository reviewRepository;

  @InjectMocks
  private FindReviewsService service;

  private Review review;

  @BeforeEach
  void setUp() {
    final Listing listing = new Listing();
    listing.setId(LISTING_ID);

    final User reviewer = new User();
    reviewer.setId(2L);
    reviewer.setFirstName("Théo");

    review = new Review(1L, listing, reviewer, 5, "Excellent bortsch !",
        LocalDateTime.of(2026, Month.JUNE, 28, 12, 0));
  }

  @Test
  @Story("Consultation nominale")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Une annonce avec des évaluations retourne la liste complète.")
  @DisplayName("Doit retourner la liste des évaluations pour une annonce évaluée")
  void findByListingId_shouldReturnReviews_whenReviewsExist() {
    when(reviewRepository.findByListingId(LISTING_ID)).thenReturn(List.of(review));

    final List<Review> result = service.findByListingId(LISTING_ID);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getRating()).isEqualTo(5);
    assertThat(result.getFirst().getReviewer().getFirstName()).isEqualTo("Théo");
  }

  @Test
  @Story("Consultation sans évaluation")
  @Severity(SeverityLevel.NORMAL)
  @Description("Une annonce sans évaluation retourne une liste vide, sans exception.")
  @DisplayName("Doit retourner une liste vide si l'annonce n'a aucune évaluation")
  void findByListingId_shouldReturnEmptyList_whenNoReviewsExist() {
    when(reviewRepository.findByListingId(LISTING_ID)).thenReturn(List.of());

    final List<Review> result = service.findByListingId(LISTING_ID);

    assertThat(result).isEmpty();
  }
}