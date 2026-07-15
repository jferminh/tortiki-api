package com.tortiki.api.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tortiki.api.application.port.in.FindBuyerContactRequestsUseCase;
import com.tortiki.api.config.SecurityConfig;
import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
import com.tortiki.api.domain.model.CuisineType;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.User;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests unitaires du contrôleur {@link BuyerContactRequestController}.
 *
 * <p>Utilise {@code @WebMvcTest} pour charger uniquement la couche web
 * sans démarrer le contexte Spring complet.</p>
 *
 * <p>Vérifie consultation nominale (200, liste pleine et liste vide),
 * accès non authentifié (401), et accès sans rôle BUYER (403).</p>
 */
@Epic("Demande de contact")
@Feature("Endpoint GET /api/v1/contact-requests/my")
@WebMvcTest(BuyerContactRequestController.class)
@Import(SecurityConfig.class)
@DisplayName("BuyerContactRequestController — Tests unitaires")
class BuyerContactRequestControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private FindBuyerContactRequestsUseCase findBuyerContactRequestsUseCase;

  private ContactRequest sampleRequest;

  private static final LocalDateTime FIXED_NOW =
      LocalDateTime.of(2026, Month.JULY, 15, 10, 0, 0);

  @BeforeEach
  void setUp() {
    User seller = new User();
    seller.setId(1L);
    seller.setFirstName("Sofia");

    CuisineType cuisineType = new CuisineType();
    cuisineType.setId(1L);
    cuisineType.setName("Ukrainienne");

    Listing listing = new Listing();
    listing.setId(10L);
    listing.setTitle("Bortsch ukrainien maison");
    listing.setSeller(seller);

    User buyer = new User();
    buyer.setId(2L);
    buyer.setFirstName("Théo");

    sampleRequest = new ContactRequest();
    sampleRequest.setId(100L);
    sampleRequest.setListing(listing);
    sampleRequest.setBuyer(buyer);
    sampleRequest.setStatus(ContactRequestStatus.PENDING);
    sampleRequest.setMessage("Je suis intéressé !");
    sampleRequest.setPortions(2);
    sampleRequest.setCreatedAt(FIXED_NOW);
  }

  @Test
  @Story("Consultation nominale")
  @Description("Un acheteur authentifié consulte son historique non vide — 200 OK.")
  @DisplayName("GET /contact-requests/my — 200 avec liste pour ROLE_BUYER authentifié")
  void shouldReturn200WithListWhenBuyerHasRequests() throws Exception {
    when(findBuyerContactRequestsUseCase.findByBuyer("theo@tortiki.fr"))
        .thenReturn(List.of(sampleRequest));

    mockMvc.perform(get("/api/v1/contact-requests/my")
            .with(user("theo@tortiki.fr").roles("BUYER"))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(100L))
        .andExpect(jsonPath("$[0].listingId").value(10L))
        .andExpect(jsonPath("$[0].listingTitle").value("Bortsch ukrainien maison"))
        .andExpect(jsonPath("$[0].sellerFirstName").value("Sofia"))
        .andExpect(jsonPath("$[0].status").value("PENDING"));
  }

  @Test
  @Story("Consultation nominale")
  @Description("Un acheteur sans demande obtient une liste vide — 200 OK.")
  @DisplayName("GET /contact-requests/my — 200 avec liste vide si aucune demande")
  void shouldReturn200WithEmptyListWhenBuyerHasNoRequests() throws Exception {
    when(findBuyerContactRequestsUseCase.findByBuyer(any()))
        .thenReturn(List.of());

    mockMvc.perform(get("/api/v1/contact-requests/my")
            .with(user("theo@tortiki.fr").roles("BUYER"))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  @Story("Sécurité")
  @Description("Un utilisateur non authentifié tente de consulter — 401 Unauthorized.")
  @DisplayName("GET /contact-requests/my — 401 sans authentification")
  void shouldReturn401WhenNotAuthenticated() throws Exception {
    mockMvc.perform(get("/api/v1/contact-requests/my")
            .with(csrf()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @Story("Sécurité")
  @Description("Un vendeur tente de consulter l'historique acheteur — 403 Forbidden.")
  @DisplayName("GET /contact-requests/my — 403 pour ROLE_SELLER")
  void shouldReturn403WhenSellerTriesToAccess() throws Exception {
    mockMvc.perform(get("/api/v1/contact-requests/my")
            .with(user("sofia@tortiki.fr").roles("SELLER"))
            .with(csrf()))
        .andExpect(status().isForbidden());
  }
}