package com.tortiki.api.infrastructure.adapter.in.web;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tortiki.api.application.port.in.ManageContactRequestUseCase;
import com.tortiki.api.application.port.in.ManageContactRequestUseCase.UpdateStatusCommand;
import com.tortiki.api.config.SecurityConfig;
import com.tortiki.api.domain.exception.ContactRequestNotFoundException;
import com.tortiki.api.domain.exception.InvalidStatusTransitionException;
import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.User;
import com.tortiki.api.infrastructure.adapter.in.web.dto.UpdateContactRequestStatusRequest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.List;
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
 * Tests unitaires du contrôleur {@link SellerDashboardController}.
 *
 * <p>Vérifie les cas nominaux et les contrôles d'accès RBAC pour les
 * deux endpoints : GET tableau de bord et PATCH mise à jour de statut.</p>
 */
@Epic("Demande de contact")
@Feature("Tableau de bord vendeur")
@WebMvcTest(SellerDashboardController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("SellerDashboardController")
class SellerDashboardControllerTest {

  private static final String DASHBOARD_URL = "/api/v1/seller-dashboard/contact-requests";
  private static final String SELLER_EMAIL = "sofia@tortiki.fr";
  private static final String BUYER_EMAIL = "theo@tortiki.fr";
  private static final Long REQUEST_ID = 1L;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ManageContactRequestUseCase manageContactRequestUseCase;

  // ─────────────────────────────────────────────────────────
  // GET /contact-requests
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Consultation tableau de bord")
  @Description("Un vendeur authentifié consulte son tableau de bord — 200 avec la liste.")
  @DisplayName("Doit retourner 200 avec la liste des demandes pour ROLE_SELLER")
  void shouldReturn200WithDashboardForSeller() throws Exception {
    when(manageContactRequestUseCase.findBySeller(SELLER_EMAIL))
        .thenReturn(List.of(buildContactRequest(REQUEST_ID, ContactRequestStatus.PENDING)));

    mockMvc.perform(get(DASHBOARD_URL)
            .with(user(SELLER_EMAIL).roles("SELLER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].status").value("PENDING"))
        .andExpect(jsonPath("$[0].buyerFirstName").value("Théo"));
  }

  @Test
  @Story("Contrôle d'accès RBAC")
  @Description("Un acheteur tente d'accéder au tableau de bord vendeur — 403 retourné.")
  @DisplayName("Doit retourner 403 pour ROLE_BUYER sur GET tableau de bord")
  void shouldReturn403ForBuyerOnGet() throws Exception {
    mockMvc.perform(get(DASHBOARD_URL)
            .with(user(BUYER_EMAIL).roles("BUYER")))
        .andExpect(status().isForbidden());
  }

  @Test
  @Story("Contrôle d'accès RBAC")
  @Description("Un utilisateur non authentifié accède au tableau de bord — 401 retourné.")
  @DisplayName("Doit retourner 401 pour un utilisateur non authentifié sur GET tableau de bord")
  void shouldReturn401ForUnauthenticatedOnGet() throws Exception {
    mockMvc.perform(get(DASHBOARD_URL))
        .andExpect(status().isUnauthorized());
  }

  // ─────────────────────────────────────────────────────────
  // PATCH /contact-requests/{id}/status
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Confirmation d'une demande")
  @Description("Le vendeur confirme une demande PENDING — 200 avec la demande mise à jour.")
  @DisplayName("Doit retourner 200 lors de la confirmation d'une demande PENDING")
  void shouldReturn200WhenConfirmingPendingRequest() throws Exception {
    ContactRequest confirmed = buildContactRequest(REQUEST_ID, ContactRequestStatus.CONFIRMED);
    UpdateStatusCommand command = new UpdateStatusCommand(
        REQUEST_ID, SELLER_EMAIL, ContactRequestStatus.CONFIRMED);

    when(manageContactRequestUseCase.updateStatus(command)).thenReturn(confirmed);

    mockMvc.perform(patch(DASHBOARD_URL + "/" + REQUEST_ID + "/status")
            .with(user(SELLER_EMAIL).roles("SELLER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new UpdateContactRequestStatusRequest(ContactRequestStatus.CONFIRMED))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CONFIRMED"));
  }

  @Test
  @Story("Règle métier : statut final irréversible")
  @Description("Le vendeur tente de modifier un statut final — 409 Conflict retourné.")
  @DisplayName("Doit retourner 409 si transition de statut interdite")
  void shouldReturn409WhenStatusTransitionForbidden() throws Exception {
    UpdateStatusCommand command = new UpdateStatusCommand(
        REQUEST_ID, SELLER_EMAIL, ContactRequestStatus.REFUSED);

    when(manageContactRequestUseCase.updateStatus(command))
        .thenThrow(new InvalidStatusTransitionException(
            ContactRequestStatus.CONFIRMED, ContactRequestStatus.REFUSED));

    mockMvc.perform(patch(DASHBOARD_URL + "/" + REQUEST_ID + "/status")
            .with(user(SELLER_EMAIL).roles("SELLER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new UpdateContactRequestStatusRequest(ContactRequestStatus.REFUSED))))
        .andExpect(status().isConflict());
  }

  @Test
  @Story("Règle métier : demande introuvable")
  @Description("La demande n'existe pas ou est hors périmètre vendeur — 404 retourné.")
  @DisplayName("Doit retourner 404 si demande introuvable ou hors périmètre")
  void shouldReturn404WhenContactRequestNotFound() throws Exception {
    Long unknownId = 99L;
    UpdateStatusCommand command = new UpdateStatusCommand(
        unknownId, SELLER_EMAIL, ContactRequestStatus.CONFIRMED);

    when(manageContactRequestUseCase.updateStatus(command))
        .thenThrow(new ContactRequestNotFoundException(unknownId));

    mockMvc.perform(patch(DASHBOARD_URL + "/" + unknownId + "/status")
            .with(user(SELLER_EMAIL).roles("SELLER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new UpdateContactRequestStatusRequest(ContactRequestStatus.CONFIRMED))))
        .andExpect(status().isNotFound());
  }

  @Test
  @Story("Contrôle d'accès RBAC")
  @Description("Un acheteur tente de modifier un statut — 403 retourné.")
  @DisplayName("Doit retourner 403 pour ROLE_BUYER sur PATCH status")
  void shouldReturn403ForBuyerOnPatchStatus() throws Exception {
    mockMvc.perform(patch(DASHBOARD_URL + "/" + REQUEST_ID + "/status")
            .with(user(BUYER_EMAIL).roles("BUYER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new UpdateContactRequestStatusRequest(ContactRequestStatus.CONFIRMED))))
        .andExpect(status().isForbidden());
  }

  // ─────────────────────────────────────────────────────────
  // Helpers
  // ─────────────────────────────────────────────────────────

  private ContactRequest buildContactRequest(
      final Long id,
      final ContactRequestStatus status) {
    User buyer = new User();
    buyer.setId(2L);
    buyer.setFirstName("Théo");

    Listing listing = new Listing();
    listing.setId(10L);
    listing.setTitle("Bortsch ukrainien");

    ContactRequest cr = new ContactRequest();
    cr.setId(id);
    cr.setListing(listing);
    cr.setBuyer(buyer);
    cr.setMessage("Je suis très intéressé !");
    cr.setPortions(2);
    cr.setStatus(status);
    return cr;
  }
}