package com.tortiki.api.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tortiki.api.application.port.in.SubmitReviewUseCase;
import com.tortiki.api.config.SecurityConfig;
import com.tortiki.api.domain.exception.ListingNotFoundException;
import com.tortiki.api.domain.exception.ReviewAlreadyExistsException;
import com.tortiki.api.domain.exception.ReviewNotAllowedException;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.Review;
import com.tortiki.api.domain.model.User;
import com.tortiki.api.infrastructure.adapter.in.web.dto.SubmitReviewRequest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests unitaires du contrôleur {@link ReviewController}.
 *
 * <p>Vérifie les cas nominaux, les règles métier et les contrôles
 * d'accès RBAC pour {@code POST /api/v1/reviews}.</p>
 */
@Epic("Évaluations")
@Feature("Soumission d'une évaluation")
@Owner("Tortiki")
@WebMvcTest(ReviewController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("ReviewController — Tests unitaires")
class ReviewControllerTest {

  private static final String REVIEWS_URL  = "/api/v1/reviews";
  private static final String BUYER_EMAIL  = "theo@tortiki.fr";
  private static final String SELLER_EMAIL = "sofia@tortiki.fr";
  private static final Long   LISTING_ID   = 4L;
  private static final Long   REVIEW_ID    = 1L;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private SubmitReviewUseCase submitReviewUseCase;

  private SubmitReviewRequest validRequest;
  private Review savedReview;

  /** Initialise les fixtures partagées. */
  @BeforeEach
  void setUp() {
    validRequest = new SubmitReviewRequest(LISTING_ID, 5, "Excellent bortsch !");

    User reviewer = new User();
    reviewer.setId(2L);
    reviewer.setFirstName("Théo");
    reviewer.setEmail(BUYER_EMAIL);

    Listing listing = new Listing();
    listing.setId(LISTING_ID);
    listing.setTitle("Bortsch ukrainien maison");

    savedReview = new Review(
        REVIEW_ID,
        listing,
        reviewer,
        5,
        "Excellent bortsch !",
        LocalDateTime.of(2026, 6, 28, 12, 0)
    );
  }

  // ── CAS NOMINAL ───────────────────────────────────────────────────────────

  @Test
  @Story("Soumission d'une évaluation")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Théo soumet une évaluation valide — 201 Created avec le corps de la réponse.")
  @DisplayName("POST /reviews — 201 Created pour un BUYER avec demande CONFIRMED")
  void shouldReturn201WhenReviewIsSubmitted() throws Exception {
    when(submitReviewUseCase.submit(any(SubmitReviewUseCase.Command.class)))
        .thenReturn(savedReview);

    mockMvc.perform(post(REVIEWS_URL)
            .with(user(BUYER_EMAIL).roles("BUYER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(REVIEW_ID))
        .andExpect(jsonPath("$.rating").value(5))
        .andExpect(jsonPath("$.comment").value("Excellent bortsch !"))
        .andExpect(jsonPath("$.listingId").value(LISTING_ID));
  }

  // ── VALIDATION ────────────────────────────────────────────────────────────

  @Test
  @Story("Validation des données")
  @Severity(SeverityLevel.NORMAL)
  @Description("listingId absent — 400 Bad Request retourné par Bean Validation.")
  @DisplayName("POST /reviews — 400 si listingId est null")
  void shouldReturn400WhenListingIdIsNull() throws Exception {
    SubmitReviewRequest invalid = new SubmitReviewRequest(null, 5, "Bon plat");

    mockMvc.perform(post(REVIEWS_URL)
            .with(user(BUYER_EMAIL).roles("BUYER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @Story("Validation des données")
  @Severity(SeverityLevel.NORMAL)
  @Description("Note inférieure à 1 — 400 Bad Request retourné par @Min(1).")
  @DisplayName("POST /reviews — 400 si rating < 1")
  void shouldReturn400WhenRatingIsBelowMinimum() throws Exception {
    SubmitReviewRequest invalid = new SubmitReviewRequest(LISTING_ID, 0, "Mauvais");

    mockMvc.perform(post(REVIEWS_URL)
            .with(user(BUYER_EMAIL).roles("BUYER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @Story("Validation des données")
  @Severity(SeverityLevel.NORMAL)
  @Description("Note supérieure à 5 — 400 Bad Request retourné par @Max(5).")
  @DisplayName("POST /reviews — 400 si rating > 5")
  void shouldReturn400WhenRatingExceedsMaximum() throws Exception {
    SubmitReviewRequest invalid = new SubmitReviewRequest(LISTING_ID, 6, "Trop bien");

    mockMvc.perform(post(REVIEWS_URL)
            .with(user(BUYER_EMAIL).roles("BUYER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @Story("Validation des données")
  @Severity(SeverityLevel.NORMAL)
  @Description("Note absente — 400 Bad Request retourné par @NotNull.")
  @DisplayName("POST /reviews — 400 si rating est null")
  void shouldReturn400WhenRatingIsNull() throws Exception {
    SubmitReviewRequest invalid = new SubmitReviewRequest(LISTING_ID, null, "Commentaire");

    mockMvc.perform(post(REVIEWS_URL)
            .with(user(BUYER_EMAIL).roles("BUYER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest());
  }

  // ── RÈGLES MÉTIER ─────────────────────────────────────────────────────────

  @Test
  @Story("Règle métier : doublon évaluation")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Théo tente de noter deux fois la même annonce — 409 Conflict retourné.")
  @DisplayName("POST /reviews — 409 si l'acheteur a déjà évalué cette annonce")
  void shouldReturn409WhenReviewAlreadyExists() throws Exception {
    when(submitReviewUseCase.submit(any(SubmitReviewUseCase.Command.class)))
        .thenThrow(new ReviewAlreadyExistsException(BUYER_EMAIL, LISTING_ID));

    mockMvc.perform(post(REVIEWS_URL)
            .with(user(BUYER_EMAIL).roles("BUYER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isConflict());
  }

  @Test
  @Story("Règle métier : demande non confirmée")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Théo n'a pas de demande CONFIRMED — 403 Forbidden retourné.")
  @DisplayName("POST /reviews — 403 si aucune demande CONFIRMED pour cette annonce")
  void shouldReturn403WhenNoConfirmedContactRequest() throws Exception {
    when(submitReviewUseCase.submit(any(SubmitReviewUseCase.Command.class)))
        .thenThrow(new ReviewNotAllowedException(BUYER_EMAIL, LISTING_ID));

    mockMvc.perform(post(REVIEWS_URL)
            .with(user(BUYER_EMAIL).roles("BUYER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isForbidden());
  }

  @Test
  @Story("Règle métier : annonce introuvable")
  @Severity(SeverityLevel.NORMAL)
  @Description("L'annonce ciblée n'existe pas — 404 Not Found retourné.")
  @DisplayName("POST /reviews — 404 si l'annonce est introuvable")
  void shouldReturn404WhenListingNotFound() throws Exception {
    when(submitReviewUseCase.submit(any(SubmitReviewUseCase.Command.class)))
        .thenThrow(new ListingNotFoundException("Annonce introuvable : " + LISTING_ID));

    mockMvc.perform(post(REVIEWS_URL)
            .with(user(BUYER_EMAIL).roles("BUYER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isNotFound());
  }

  // ── RBAC ──────────────────────────────────────────────────────────────────

  @Test
  @Story("Contrôle d'accès RBAC")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Sofia (SELLER) tente de soumettre une évaluation — 403 Forbidden retourné.")
  @DisplayName("POST /reviews — 403 pour ROLE_SELLER")
  void shouldReturn403ForSeller() throws Exception {
    mockMvc.perform(post(REVIEWS_URL)
            .with(user(SELLER_EMAIL).roles("SELLER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isForbidden());
  }

  @Test
  @Story("Contrôle d'accès RBAC")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Utilisateur non authentifié tente de soumettre une évaluation — 401 retourné.")
  @DisplayName("POST /reviews — 401 pour un utilisateur non authentifié")
  void shouldReturn401ForUnauthenticated() throws Exception {
    mockMvc.perform(post(REVIEWS_URL)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isUnauthorized());
  }

  // ── COMMENTAIRE OPTIONNEL ─────────────────────────────────────────────────

  @Test
  @Story("Soumission d'une évaluation")
  @Severity(SeverityLevel.MINOR)
  @Description("Évaluation sans commentaire — champ optionnel, 201 Created retourné.")
  @DisplayName("POST /reviews — 201 si comment est null (champ optionnel)")
  void shouldReturn201WhenCommentIsNull() throws Exception {
    SubmitReviewRequest noComment = new SubmitReviewRequest(LISTING_ID, 4, null);

    Review reviewNoComment = new Review(
        2L,
        savedReview.getListing(),
        savedReview.getReviewer(),
        4,
        null,
        LocalDateTime.of(2026, 6, 28, 14, 0)
    );

    when(submitReviewUseCase.submit(any(SubmitReviewUseCase.Command.class)))
        .thenReturn(reviewNoComment);

    mockMvc.perform(post(REVIEWS_URL)
            .with(user(BUYER_EMAIL).roles("BUYER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(noComment)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.rating").value(4));
  }
}