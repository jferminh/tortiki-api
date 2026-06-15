package com.tortiki.api.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tortiki.api.application.port.in.SubmitContactRequestUseCase;
import com.tortiki.api.application.port.out.UserRepository;
import com.tortiki.api.config.SecurityConfig;
import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.User;
import com.tortiki.api.infrastructure.adapter.in.web.dto.CreateContactRequestRequest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests unitaires du contrôleur {@link ContactRequestController}.
 *
 * <p>Utilise {@code @WebMvcTest} pour charger uniquement la couche web
 * sans démarrer le contexte Spring complet.</p>
 *
 * <p>Vérifie : soumission nominale 201, accès non authentifié 401,
 * accès sans rôle BUYER 403, et body invalide 400.</p>
 */
@Epic("Demande de contact")
@Feature("Endpoint POST /api/contact-requests")
@WebMvcTest(ContactRequestController.class)
@Import(SecurityConfig.class)
@DisplayName("ContactRequestController")
class ContactRequestControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private SubmitContactRequestUseCase submitContactRequestUseCase;

  @MockitoBean
  private UserRepository userRepository;

  private User buyer;
  private ContactRequest savedContactRequest;
  private CreateContactRequestRequest validRequest;

  private static final LocalDateTime FIXED_NOW =
      LocalDateTime.of(2026, Month.JUNE, 15, 10, 0, 0);

  @BeforeEach
  void setUp() {
    buyer = new User();
    buyer.setId(2L);
    buyer.setEmail("theo@tortiki.fr");

    Listing listing = new Listing();
    listing.setId(10L);

    savedContactRequest = new ContactRequest();
    savedContactRequest.setId(100L);
    savedContactRequest.setListing(listing);
    savedContactRequest.setBuyer(buyer);
    savedContactRequest.setStatus(ContactRequestStatus.PENDING);
    savedContactRequest.setMessage("Je suis intéressé !");
    savedContactRequest.setPortions(2);
    savedContactRequest.setCreatedAt(FIXED_NOW);

    validRequest = new CreateContactRequestRequest(10L, "Je suis intéressé !", 2);
  }

  // ─────────────────────────────────────────────────────────
  // Cas nominal — 201 Created
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Soumission nominale")
  @Description("Un acheteur authentifié soumet une demande valide — 201 Created.")
  @DisplayName("POST /api/contact-requests → 201 pour ROLE_BUYER authentifié")
  void shouldReturn201WhenBuyerSubmitsValidRequest() throws Exception {
    when(userRepository.findByEmailAndEnabledTrue("theo@tortiki.fr"))
        .thenReturn(Optional.of(buyer));
    when(submitContactRequestUseCase.submit(any(SubmitContactRequestUseCase.Command.class)))
        .thenReturn(savedContactRequest);

    mockMvc.perform(post("/api/contact-requests")
            .with(user("theo@tortiki.fr").roles("BUYER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(100L))
        .andExpect(jsonPath("$.listingId").value(10L))
        .andExpect(jsonPath("$.status").value("PENDING"))
        .andExpect(jsonPath("$.portions").value(2));
  }

  // ─────────────────────────────────────────────────────────
  // Sécurité — 401 non authentifié
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Sécurité")
  @Description("Un utilisateur non authentifié tente de soumettre — 401 Unauthorized.")
  @DisplayName("POST /api/contact-requests → 401 sans authentification")
  void shouldReturn401WhenNotAuthenticated() throws Exception {
    mockMvc.perform(post("/api/contact-requests")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isUnauthorized());
  }

  // ─────────────────────────────────────────────────────────
  // Sécurité — 403 mauvais rôle
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Sécurité")
  @Description("Un vendeur tente de soumettre une demande — 403 Forbidden.")
  @DisplayName("POST /api/contact-requests → 403 pour ROLE_SELLER")
  void shouldReturn403WhenSellerTriesToSubmit() throws Exception {
    mockMvc.perform(post("/api/contact-requests")
            .with(user("sofia@tortiki.fr").roles("SELLER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isForbidden());
  }

  // ─────────────────────────────────────────────────────────
  // Validation — 400 body invalide
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Validation")
  @Description("Le body ne contient pas listingId — 400 Bad Request.")
  @DisplayName("POST /api/contact-requests → 400 si listingId absent")
  void shouldReturn400WhenListingIdIsNull() throws Exception {
    CreateContactRequestRequest invalidRequest =
        new CreateContactRequestRequest(null, "message", 2);

    mockMvc.perform(post("/api/contact-requests")
            .with(user("theo@tortiki.fr").roles("BUYER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }
}