package com.tortiki.api.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tortiki.api.application.port.in.FindUserUseCase;
import com.tortiki.api.application.port.in.ManageListingUseCase;
import com.tortiki.api.domain.exception.ListingNotFoundException;
import com.tortiki.api.domain.model.CuisineType;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.ListingStatus;
import com.tortiki.api.domain.model.Role;
import com.tortiki.api.domain.model.RoleName;
import com.tortiki.api.domain.model.User;
import com.tortiki.api.infrastructure.adapter.in.web.dto.ListingResponse;
import com.tortiki.api.infrastructure.adapter.in.web.support.TestSecurityConfig;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests unitaires de la couche REST {@link ListingController}.
 *
 * <p>Utilise {@code @WebMvcTest} pour charger uniquement la couche web.
 * {@link ManageListingUseCase}, {@link FindUserUseCase} et
 * {@link ListingWebMapper} sont mockés — aucune base de données sollicitée.</p>
 *
 * <p>Les endpoints publics ({@code GET}) sont testés sans authentification.
 * Endpoint vendeur ({@code POST}) utilise {@code .with(user(...))}
 * pour simuler un principal Spring Security.</p>
 */
@Epic("Annonces")
@Feature("Endpoints REST listings")
@WebMvcTest(ListingController.class)
@Import(TestSecurityConfig.class)
@DisplayName("ListingController — Tests unitaires WebMvcTest")
class ListingControllerTest {

  // ── Constantes de test ────────────────────────────────────────────────────

  private static final Long   LISTING_ID       = 1L;
  private static final Long   SELLER_ID        = 10L;
  private static final Long   CUISINE_ID       = 2L;
  private static final String SELLER_EMAIL     = "sofia@example.com";
  private static final String LISTING_TITLE    = "Bortsch ukrainien";
  private static final String LISTING_DESC     = "Soupe traditionnelle ukrainienne";
  private static final String PICKUP_ADDRESS   = "12 rue de la Paix, 54000 Nancy";
  private static final String CUISINE_NAME     = "Ukrainienne";
  private static final LocalDateTime PICKUP_DATETIME =
      LocalDateTime.of(2026, Month.JUNE, 21, 14, 0, 0);
  private static final LocalDateTime TEST_CREATED_AT =
      LocalDateTime.of(2026, Month.JUNE, 1, 12, 0, 0);

  // ── Injection MockMvc ─────────────────────────────────────────────────────

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  // ── Mocks des dépendances du contrôleur ───────────────────────────────────

  @MockitoBean
  private ManageListingUseCase manageListingUseCase;

  @MockitoBean
  private FindUserUseCase findUserUseCase;

  @MockitoBean
  private ListingWebMapper listingWebMapper;

  // ── Données communes ──────────────────────────────────────────────────────

  /** Annonce domaine utilisée dans les mocks. */
  private Listing listing;

  /** DTO de réponse retournée par le mapper web. */
  private ListingResponse listingResponse;

  /** Vendeur associé à l'annonce. */
  private User sofia;

  /** Initialisation des données communes avant chaque test. */
  @BeforeEach
  void setUp() {
    sofia = new User();
    sofia.setId(SELLER_ID);
    sofia.setEmail(SELLER_EMAIL);
    sofia.addRole(new Role(1L, RoleName.SELLER));

    CuisineType cuisineType = new CuisineType();
    cuisineType.setId(CUISINE_ID);
    cuisineType.setName(CUISINE_NAME);

    listing = new Listing();
    listing.setId(LISTING_ID);
    listing.setTitle(LISTING_TITLE);
    listing.setDescription(LISTING_DESC);
    listing.setPrice(new BigDecimal("12.50"));
    listing.setPortions(4);
    listing.setPickupAddress(PICKUP_ADDRESS);       // ← était setCity + setPostalCode
    listing.setPickupDatetime(PICKUP_DATETIME);     // ← était setPickupSlot
    listing.setCuisineType(cuisineType);
    listing.setSeller(sofia);
    listing.setStatus(ListingStatus.ACTIVE);
    listing.setCreatedAt(TEST_CREATED_AT);

    listingResponse = new ListingResponse(
        LISTING_ID,
        LISTING_TITLE,
        LISTING_DESC,
        new BigDecimal("12.50"),
        4,
        PICKUP_ADDRESS,        // ← pickupAddress
        PICKUP_DATETIME,       // ← pickupDatetime
        null,                  // photoUrl
        ListingStatus.ACTIVE,
        CUISINE_NAME,
        SELLER_EMAIL,
        List.of(),             // ← allergenNames (vide pour le test)
        TEST_CREATED_AT
    );
  }

  // ── GET /api/listings ─────────────────────────────────────────────────────

  @Test
  @Story("Consultation catalogue")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Catalogue non vide — HTTP 200 avec la liste des annonces.")
  @DisplayName("GET /listings — retourne 200 avec la liste des annonces actives")
  void findAll_shouldReturn200_withListingList() throws Exception {
    when(manageListingUseCase.findAll()).thenReturn(List.of(listing));
    when(listingWebMapper.toResponse(listing)).thenReturn(listingResponse);

    mockMvc.perform(get("/api/v1/listings"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(LISTING_ID))
        .andExpect(jsonPath("$[0].title").value(LISTING_TITLE))
        .andExpect(jsonPath("$[0].pickupAddress").value(PICKUP_ADDRESS)); // ← était city
  }

  @Test
  @Story("Consultation catalogue")
  @Severity(SeverityLevel.NORMAL)
  @Description("Catalogue vide — HTTP 200 avec un tableau JSON vide.")
  @DisplayName("GET /listings — retourne 200 avec tableau vide si aucune annonce")
  void findAll_shouldReturn200_withEmptyList() throws Exception {
    when(manageListingUseCase.findAll()).thenReturn(List.of());

    mockMvc.perform(get("/api/v1/listings"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  // ── GET /api/listings/{id} ────────────────────────────────────────────────

  @Test
  @Story("Détail annonce")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Annonce trouvée — HTTP 200 avec le détail complet.")
  @DisplayName("GET /listings/{id} — retourne 200 avec l'annonce correspondante")
  void findById_shouldReturn200_whenListingExists() throws Exception {
    when(manageListingUseCase.findById(LISTING_ID)).thenReturn(listing);
    when(listingWebMapper.toResponse(listing)).thenReturn(listingResponse);

    mockMvc.perform(get("/api/v1/listings/{id}", LISTING_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(LISTING_ID))
        .andExpect(jsonPath("$.title").value(LISTING_TITLE))
        .andExpect(jsonPath("$.cuisineTypeName").value(CUISINE_NAME))
        .andExpect(jsonPath("$.sellerEmail").value(SELLER_EMAIL));
  }

  @Test
  @Story("Détail annonce")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Annonce introuvable — HTTP 404 via GlobalExceptionHandler.")
  @DisplayName("GET /listings/{id} — retourne 404 si l'annonce est introuvable")
  void findById_shouldReturn404_whenListingNotFound() throws Exception {
    when(manageListingUseCase.findById(anyLong()))
        .thenThrow(new ListingNotFoundException(
            "Annonce introuvable pour l'identifiant : 99"
        ));

    mockMvc.perform(get("/api/v1/listings/{id}", 99L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"));

    verify(listingWebMapper, never()).toResponse(any());
  }

  // ── POST /api/listings ────────────────────────────────────────────────────

  @Test
  @Story("Création annonce")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Sofia crée une annonce en tant que vendeur — HTTP 201.")
  @DisplayName("POST /listings — retourne 201 si la création réussit (SELLER)")
  void create_shouldReturn201_whenSellerCreatesListing() throws Exception {
    when(findUserUseCase.findByEmail(SELLER_EMAIL)).thenReturn(sofia);
    when(manageListingUseCase.create(anyLong(), any())).thenReturn(listing);
    when(listingWebMapper.toResponse(listing)).thenReturn(listingResponse);
    when(listingWebMapper.toCommand(any())).thenReturn(null);

    mockMvc.perform(post("/api/v1/listings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(buildCreateBody())
            .with(csrf())
            .with(user(SELLER_EMAIL)
                .authorities(new SimpleGrantedAuthority("ROLE_SELLER"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(LISTING_ID))
        .andExpect(jsonPath("$.title").value(LISTING_TITLE));
  }

  @Test
  @Story("Création annonce")
  @Severity(SeverityLevel.NORMAL)
  @Description("Corps invalide — HTTP 400 Bad Request, le use case n'est pas appelé.")
  @DisplayName("POST /listings — retourne 400 si le corps est invalide")
  void create_shouldReturn400_whenBodyIsInvalid() throws Exception {
    mockMvc.perform(post("/api/v1/listings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of(
                "title", "",
                "price", "-5"
            )))
            .with(csrf())
            .with(user(SELLER_EMAIL)
                .authorities(new SimpleGrantedAuthority("ROLE_SELLER"))))
        .andExpect(status().isBadRequest());

    verify(manageListingUseCase, never()).create(anyLong(), any());
  }

  // ── Helper ────────────────────────────────────────────────────────────────

  /**
   * Corps JSON d'une requête de création d'annonce valide pour Sofia.
   *
   * <p>Aligné sur {@link com.tortiki.api.infrastructure.adapter.in.web.dto.CreateListingRequest}
   * après refactor : {@code pickupAddress} + {@code pickupDatetime}
   * remplacent {@code city}, {@code postalCode} et {@code pickupSlot}.</p>
   */
  private String buildCreateBody() throws Exception {
    return objectMapper.writeValueAsString(Map.of(
        "title", LISTING_TITLE,
        "description", LISTING_DESC,
        "price", "12.50",
        "portions", 4,
        "pickupAddress", PICKUP_ADDRESS,          // ← était city + postalCode
        "pickupDatetime", "2026-06-21T14:00:00",  // ← était pickupSlot
        "cuisineTypeId", CUISINE_ID,
        "allergenIds", List.of()                  // ← nouveau champ
    ));
  }
}