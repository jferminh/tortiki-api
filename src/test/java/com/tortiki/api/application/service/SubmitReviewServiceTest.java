package com.tortiki.api.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tortiki.api.application.port.in.SubmitReviewUseCase.Command;
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
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitaires du service {@link SubmitReviewService}.
 *
 * <p>Vérifie les quatre règles métier : soumission nominale,
 * annonce introuvable, demande non confirmée et doublon d'évaluation.
 * La couverture transactionnelle (@Transactional) est déléguée
 * à un test d'intégration Testcontainers (tag {@code requires-integration-test}).</p>
 */
@Epic("Évaluation")
@Feature("Soumission d'une évaluation")
@Tag("requires-integration-test")
@ExtendWith(MockitoExtension.class)
@DisplayName("SubmitReviewService — Tests unitaires")
class SubmitReviewServiceTest {

  private static final Instant FIXED_INSTANT = Instant.parse("2026-06-25T20:00:00Z");
  private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

  private static final Long LISTING_ID = 10L;
  private static final Long SELLER_ID = 1L;
  private static final Long REVIEWER_ID = 2L;
  private static final String REVIEWER_EMAIL = "theo@tortiki.fr";

  @Mock
  private ReviewRepository reviewRepository;
  @Mock
  private ListingRepository listingRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ContactRequestRepository contactRequestRepository;

  private SubmitReviewService service;
  private Listing listing;
  private User reviewer;
  private Command command;

  @BeforeEach
  void setUp() {
    service = new SubmitReviewService(
        reviewRepository,
        listingRepository,
        userRepository,
        contactRequestRepository,
        FIXED_CLOCK
    );

    final User seller = new User();
    seller.setId(SELLER_ID);

    listing = new Listing();
    listing.setId(LISTING_ID);
    listing.setSeller(seller);

    reviewer = new User();
    reviewer.setId(REVIEWER_ID);
    reviewer.setEmail(REVIEWER_EMAIL);

    command = new Command(LISTING_ID, REVIEWER_EMAIL, 5, "Délicieux !");
  }

  // ─────────────────────────────────────────────────────────
  // Invariants Command (Bloc 1 — constructeur compact)
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Invariants Command")
  @Description("Note invalide (0) — IllegalArgumentException levée dès la construction.")
  @DisplayName("Command doit lever IllegalArgumentException si rating < 1")
  void shouldThrowWhenCommandRatingIsInvalid() {
    assertThatThrownBy(() ->
        new Command(LISTING_ID, REVIEWER_EMAIL, 0, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("note");
  }

  @Test
  @Story("Invariants Command")
  @Description("listingId null — IllegalArgumentException levée dès la construction.")
  @DisplayName("Command doit lever IllegalArgumentException si listingId est null")
  void shouldThrowWhenCommandListingIdIsNull() {
    assertThatThrownBy(() ->
        new Command(null, REVIEWER_EMAIL, 5, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("annonce");
  }

  // ─────────────────────────────────────────────────────────
  // Cas nominal
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Soumission nominale")
  @Description("Un acheteur avec demande confirmée soumet une évaluation — créée avec succès.")
  @DisplayName("Doit créer l'évaluation si toutes les règles sont respectées")
  void shouldCreateReviewWhenAllRulesPass() {
    givenListingExists();
    givenReviewerExists();
    givenConfirmedRequestExists();
    givenNoDuplicateReview();
    when(reviewRepository.save(any(Review.class))).thenReturn(buildSavedReview());

    final Review result = whenSubmitCommand();

    thenReviewIsCreated(result);
    verify(reviewRepository).save(any(Review.class));
  }

  // ─────────────────────────────────────────────────────────
  // Règle 1 : annonce introuvable
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Règle métier : annonce introuvable")
  @Description("L'annonce n'existe pas — ListingNotFoundException levée.")
  @DisplayName("Doit lever ListingNotFoundException si annonce absente")
  void shouldThrowWhenListingNotFound() {
    when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.submit(command))
        .isInstanceOf(ListingNotFoundException.class);

    verify(reviewRepository, never()).save(any());
  }

  // ─────────────────────────────────────────────────────────
  // Règle 2 : acheteur introuvable
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Règle métier : acheteur introuvable")
  @Description("L'acheteur est introuvable ou inactif — UserNotFoundException levée.")
  @DisplayName("Doit lever UserNotFoundException si acheteur absent ou inactif")
  void shouldThrowWhenReviewerNotFound() {
    givenListingExists();
    when(userRepository.findByEmailAndEnabledTrue(REVIEWER_EMAIL))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.submit(command))
        .isInstanceOf(UserNotFoundException.class);

    verify(reviewRepository, never()).save(any());
  }

  // ─────────────────────────────────────────────────────────
  // Règle 3 : pas de demande confirmée
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Règle métier : demande non confirmée")
  @Description("Aucune demande CONFIRMED — ReviewNotAllowedException levée.")
  @DisplayName("Doit lever ReviewNotAllowedException si pas de demande confirmée")
  void shouldThrowWhenNoConfirmedRequest() {
    givenListingExists();
    givenReviewerExists();
    when(contactRequestRepository.existsConfirmedByListingIdAndBuyerId(
        LISTING_ID, REVIEWER_ID)).thenReturn(false);

    assertThatThrownBy(() -> service.submit(command))
        .isInstanceOf(ReviewNotAllowedException.class);

    verify(reviewRepository, never()).save(any());
  }

  // ─────────────────────────────────────────────────────────
  // Règle 4 : doublon
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Règle métier : doublon d'évaluation")
  @Description("L'acheteur a déjà évalué cette annonce — ReviewAlreadyExistsException levée.")
  @DisplayName("Doit lever ReviewAlreadyExistsException si évaluation déjà soumise")
  void shouldThrowWhenReviewAlreadyExists() {
    givenListingExists();
    givenReviewerExists();
    givenConfirmedRequestExists();
    when(reviewRepository.existsByListingIdAndReviewerId(LISTING_ID, REVIEWER_ID))
        .thenReturn(true);

    assertThatThrownBy(() -> service.submit(command))
        .isInstanceOf(ReviewAlreadyExistsException.class);

    verify(reviewRepository, never()).save(any());
  }

  // ─────────────────────────────────────────────────────────
  // Méthodes Given / When / Then (pattern Allure)
  // ─────────────────────────────────────────────────────────

  @Step("Étant donné que l'annonce {LISTING_ID} existe")
  private void givenListingExists() {
    when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
  }

  @Step("Étant donné que l'acheteur theo@tortiki.fr existe et est actif")
  private void givenReviewerExists() {
    when(userRepository.findByEmailAndEnabledTrue(REVIEWER_EMAIL))
        .thenReturn(Optional.of(reviewer));
  }

  @Step("Étant donné qu'une demande CONFIRMED existe pour cet acheteur")
  private void givenConfirmedRequestExists() {
    when(contactRequestRepository.existsConfirmedByListingIdAndBuyerId(
        LISTING_ID, REVIEWER_ID)).thenReturn(true);
  }

  @Step("Étant donné qu'aucune évaluation n'existe pour cet acheteur sur cette annonce")
  private void givenNoDuplicateReview() {
    when(reviewRepository.existsByListingIdAndReviewerId(LISTING_ID, REVIEWER_ID))
        .thenReturn(false);
  }

  @Step("Quand la commande de soumission d'évaluation est exécutée")
  private Review whenSubmitCommand() {
    return service.submit(command);
  }

  @Step("Alors l'évaluation est créée avec la note 5 et la date fixe")
  private void thenReviewIsCreated(final Review result) {
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(100L);
    assertThat(result.getRating()).isEqualTo(5);
    assertThat(result.getListing().getId()).isEqualTo(LISTING_ID);
    assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.now(FIXED_CLOCK));
  }

  // ─────────────────────────────────────────────────────────
  // Helpers
  // ─────────────────────────────────────────────────────────

  private Review buildSavedReview() {
    return new Review(
        100L,
        listing,
        reviewer,
        5,
        "Délicieux !",
        LocalDateTime.now(FIXED_CLOCK)
    );
  }
}