// src/test/java/com/tortiki/api/infrastructure/adapter/in/web/SellerDashboardControllerTest.java
package com.tortiki.api.infrastructure.adapter.in.web;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tortiki.api.application.port.in.ManageContactRequestUseCase;
import com.tortiki.api.config.SecurityConfig;
import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.User;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests unitaires du contrôleur {@link SellerDashboardController}.
 *
 * <p>Vérifie les cas nominaux et les contrôles d'accès RBAC :
 * ROLE_SELLER autorisé, ROLE_BUYER et non authentifié refusés.</p>
 */
@Epic("Demande de contact")
@Feature("Tableau de bord vendeur")
@WebMvcTest(SellerDashboardController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("SellerDashboardController")
class SellerDashboardControllerTest {

  private static final String DASHBOARD_URL = "/api/v1/seller/dashboard/contact-requests";
  private static final String SELLER_EMAIL = "sofia@tortiki.fr";

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ManageContactRequestUseCase manageContactRequestUseCase;

  @Test
  @Story("Consultation tableau de bord")
  @Description("Un vendeur authentifié consulte son tableau de bord — 200 avec la liste.")
  @DisplayName("Doit retourner 200 avec la liste des demandes pour ROLE_SELLER")
  void shouldReturn200WithDashboardForSeller() throws Exception {
    when(manageContactRequestUseCase.findBySeller(SELLER_EMAIL))
        .thenReturn(List.of(buildContactRequest()));

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
  @DisplayName("Doit retourner 403 pour ROLE_BUYER")
  void shouldReturn403ForBuyer() throws Exception {
    mockMvc.perform(get(DASHBOARD_URL)
            .with(user("theo@tortiki.fr").roles("BUYER")))
        .andExpect(status().isForbidden());
  }

  @Test
  @Story("Contrôle d'accès RBAC")
  @Description("Un utilisateur non authentifié accède au tableau de bord — 401 retourné.")
  @DisplayName("Doit retourner 401 pour un utilisateur non authentifié")
  void shouldReturn401ForUnauthenticated() throws Exception {
    mockMvc.perform(get(DASHBOARD_URL))
        .andExpect(status().isUnauthorized());
  }

  // ─────────────────────────────────────────────────────────
  // Helpers
  // ─────────────────────────────────────────────────────────

  private ContactRequest buildContactRequest() {
    User buyer = new User();
    buyer.setId(2L);
    buyer.setFirstName("Théo");

    Listing listing = new Listing();
    listing.setId(10L);
    listing.setTitle("Bortsch ukrainien");

    ContactRequest cr = new ContactRequest();
    cr.setId(1L);
    cr.setListing(listing);
    cr.setBuyer(buyer);
    cr.setMessage("Je suis très intéressé !");
    cr.setPortions(2);
    cr.setStatus(ContactRequestStatus.PENDING);
    return cr;
  }
}