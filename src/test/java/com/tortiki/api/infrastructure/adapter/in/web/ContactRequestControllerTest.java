package com.tortiki.api.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tortiki.api.application.port.in.ManageContactRequestUseCase;
import com.tortiki.api.application.port.in.SubmitContactRequestUseCase;
import com.tortiki.api.config.SecurityConfig;
import com.tortiki.api.config.SecurityConstants;
import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.User;
import com.tortiki.api.infrastructure.adapter.in.web.dto.CreateContactRequestRequest;
import com.tortiki.api.infrastructure.adapter.in.web.dto.UpdateContactRequestStatusRequest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
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
 * Tests unitaires du contrôleur {@link ContactRequestController}.
 *
 * <p>Utilise {@code @WebMvcTest} pour charger uniquement la couche web
 * sans démarrer le contexte Spring complet.</p>
 *
 * <p>Vérifie : soumission nominale 201, accès non authentifié 401,
 * accès sans rôle BUYER 403, body invalide 400 (listingId absent,
 * portions absentes), dashboard vendeur 200, confirmation de statut
 * 200, refus d'accès PATCH pour un acheteur 403.</p>
 */
@Epic("Demande de contact")
@Feature("Endpoint /api/v1/contact-requests")
@WebMvcTest(ContactRequestController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("ContactRequestController")
class ContactRequestControllerTest {

  private static final LocalDateTime FIXED_NOW =
      LocalDateTime.of(2026, Month.JUNE, 15, 10, 0, 0);

  private static final Long LISTING_ID = 10L;
  private static final Long CONTACT_REQUEST_ID = 100L;
  private static final Long BUYER_ID = 2L;
  private static final String BUYER_EMAIL = "theo@tortiki.fr";
  private static final String SELLER_EMAIL = "sofia@tortiki.fr";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private SubmitContactRequestUseCase submitContactRequestUseCase;

  @MockitoBean
  private ManageContactRequestUseCase manageContactRequestUseCase;

  private ContactRequest savedContactRequest;
  private ContactRequest confirmedContactRequest;
  private CreateContactRequestRequest validRequest;

  @BeforeEach
  void setUp() {
    User buyer = new User();
    buyer.setId(BUYER_ID);
    buyer.setEmail(BUYER_EMAIL);

    Listing listing = new Listing();
    listing.setId(LISTING_ID);

    savedContactRequest = new ContactRequest();
    savedContactRequest.setId(CONTACT_REQUEST_ID);
    savedContactRequest.setListing(listing);
    savedContactRequest.setBuyer(buyer);
    savedContactRequest.setStatus(ContactRequestStatus.PENDING);
    savedContactRequest.setMessage("Je suis intéressé !");
    savedContactRequest.setPortions(2);
    savedContactRequest.setCreatedAt(FIXED_NOW);

    confirmedContactRequest = new ContactRequest();
    confirmedContactRequest.setId(CONTACT_REQUEST_ID);
    confirmedContactRequest.setListing(listing);
    confirmedContactRequest.setBuyer(buyer);
    confirmedContactRequest.setStatus(ContactRequestStatus.CONFIRMED);
    confirmedContactRequest.setMessage("Je suis intéressé !");
    confirmedContactRequest.setPortions(2);
    confirmedContactRequest.setCreatedAt(FIXED_NOW);

    validRequest = new CreateContactRequestRequest(
        LISTING_ID, "Je suis intéressé !", 2);
  }

  // ─────────────────────────────────────────────────────────
  // Cas nominal — 201 Created
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Soumission nominale")
  @Description("Un acheteur authentifié soumet une demande valide — 201 Créé.")
  @DisplayName("POST /api/v1/contact-requests → 201 pour ROLE_BUYER authentifié")
  void shouldReturn201WhenBuyerSubmitsValidRequest() throws Exception {
    when(submitContactRequestUseCase.submit(
        any(SubmitContactRequestUseCase.Command.class)))
        .thenReturn(savedContactRequest);

    mockMvc.perform(post(SecurityConstants.ROUTE_CONTACT_REQUESTS)
            .with(user(BUYER_EMAIL).roles("BUYER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(CONTACT_REQUEST_ID))
        .andExpect(jsonPath("$.listingId").value(LISTING_ID))
        .andExpect(jsonPath("$.status").value("PENDING"))
        .andExpect(jsonPath("$.message").value("Je suis intéressé !"))
        .andExpect(jsonPath("$.portions").value(2));
  }

  // ─────────────────────────────────────────────────────────
  // Sécurité — 401 non authentifié
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Sécurité")
  @Description("Un utilisateur non authentifié tente de soumettre — 401 Non autorisé.")
  @DisplayName("POST /api/v1/contact-requests → 401 sans authentification")
  void shouldReturn401WhenNotAuthenticated() throws Exception {
    mockMvc.perform(post(SecurityConstants.ROUTE_CONTACT_REQUESTS)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isUnauthorized());
  }

  // ─────────────────────────────────────────────────────────
  // Sécurité — 403 mauvais rôle sur POST
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Sécurité")
  @Description("Un vendeur tente de soumettre une demande — 403 Interdit.")
  @DisplayName("POST /api/v1/contact-requests → 403 pour ROLE_SELLER")
  void shouldReturn403WhenSellerTriesToSubmit() throws Exception {
    mockMvc.perform(post(SecurityConstants.ROUTE_CONTACT_REQUESTS)
            .with(user(SELLER_EMAIL).roles("SELLER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isForbidden());
  }

  // ─────────────────────────────────────────────────────────
  // Validation — 400 listingId
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Validation")
  @Description("Le body ne contient pas listingId — 400 Requête invalide.")
  @DisplayName("POST /api/v1/contact-requests → 400 si listingId absent")
  void shouldReturn400WhenListingIdIsNull() throws Exception {
    CreateContactRequestRequest invalidRequest =
        new CreateContactRequestRequest(null, "message", 2);

    mockMvc.perform(post(SecurityConstants.ROUTE_CONTACT_REQUESTS)
            .with(user(BUYER_EMAIL).roles("BUYER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  // ─────────────────────────────────────────────────────────
  // Validation — 400 portions
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Validation")
  @Description("Le body ne contient pas portions — 400 Requête invalide.")
  @DisplayName("POST /api/v1/contact-requests → 400 si portions absent")
  void shouldReturn400WhenPortionsIsNull() throws Exception {
    CreateContactRequestRequest invalidRequest =
        new CreateContactRequestRequest(LISTING_ID, "message", null);

    mockMvc.perform(post(SecurityConstants.ROUTE_CONTACT_REQUESTS)
            .with(user(BUYER_EMAIL).roles("BUYER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  // ─────────────────────────────────────────────────────────
  // Dashboard vendeur — 200 OK
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Tableau de bord vendeur")
  @Description("Un vendeur authentifié consulte ses demandes reçues — 200 OK.")
  @DisplayName("GET /api/v1/contact-requests → 200 pour ROLE_SELLER authentifié")
  void shouldReturn200WhenSellerGetsDashboard() throws Exception {
    when(manageContactRequestUseCase.findBySeller(SELLER_EMAIL))
        .thenReturn(List.of(savedContactRequest));

    mockMvc.perform(get(SecurityConstants.ROUTE_CONTACT_REQUESTS)
            .with(user(SELLER_EMAIL).roles("SELLER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(CONTACT_REQUEST_ID))
        .andExpect(jsonPath("$[0].status").value("PENDING"));
  }

  // ─────────────────────────────────────────────────────────
  // Mise à jour statut — 200 OK
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Confirmation de demande")
  @Description("Un vendeur confirme une demande PENDING — 200 OK, statut CONFIRMED.")
  @DisplayName("PATCH /api/v1/contact-requests/{id}/status → 200 pour ROLE_SELLER")
  void shouldReturn200WhenSellerConfirmsRequest() throws Exception {
    UpdateContactRequestStatusRequest request =
        new UpdateContactRequestStatusRequest(ContactRequestStatus.CONFIRMED);

    when(manageContactRequestUseCase.updateStatus(
        any(ManageContactRequestUseCase.UpdateStatusCommand.class)))
        .thenReturn(confirmedContactRequest);

    mockMvc.perform(patch(SecurityConstants.ROUTE_CONTACT_REQUESTS + "/{id}/status",
            CONTACT_REQUEST_ID)
            .with(user(SELLER_EMAIL).roles("SELLER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(CONTACT_REQUEST_ID))
        .andExpect(jsonPath("$.status").value("CONFIRMED"));
  }

  // ─────────────────────────────────────────────────────────
  // Sécurité — 403 mauvais rôle sur PATCH
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Sécurité")
  @Description("Un acheteur tente de modifier le statut d'une demande — 403 Interdit.")
  @DisplayName("PATCH /api/v1/contact-requests/{id}/status → 403 pour ROLE_BUYER")
  void shouldReturn403WhenBuyerTriesToUpdateStatus() throws Exception {
    UpdateContactRequestStatusRequest request =
        new UpdateContactRequestStatusRequest(ContactRequestStatus.CONFIRMED);

    mockMvc.perform(patch(SecurityConstants.ROUTE_CONTACT_REQUESTS + "/{id}/status",
            CONTACT_REQUEST_ID)
            .with(user(BUYER_EMAIL).roles("BUYER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }
}