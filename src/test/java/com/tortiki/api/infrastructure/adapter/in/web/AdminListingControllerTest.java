package com.tortiki.api.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tortiki.api.application.port.in.ManageAdminListingsUseCase;
import com.tortiki.api.config.SecurityConfig;
import com.tortiki.api.domain.exception.ListingNotFoundException;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.ListingStatus;
import com.tortiki.api.domain.model.User;
import com.tortiki.api.infrastructure.adapter.in.web.dto.AdminListingResponse;
import com.tortiki.api.infrastructure.adapter.out.persistence.UserDetailsServiceImpl;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.math.BigDecimal;
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
 * Tests unitaires de la couche REST {@link AdminListingController}.
 *
 * <p>Utilise {@code @WebMvcTest} pour charger uniquement la couche web.
 * {@link ManageAdminListingsUseCase} et {@link AdminListingWebMapper} sont
 * mockés — aucune base de données sollicitée.</p>
 *
 * <p>{@link UserDetailsServiceImpl} est mocké afin que
 * {@link SecurityConfig} trouve un bean {@code UserDetailsService} dans
 * le contexte de test, évitant le fallback silencieux sur
 * {@code InMemoryUserDetailsManager} qui casserait le routage MVC.</p>
 */
@Epic("Administration")
@Feature("Endpoints REST admin-listings")
@WebMvcTest(AdminListingController.class)
@Import(SecurityConfig.class)
@DisplayName("AdminListingController — Tests unitaires")
class AdminListingControllerTest {

  private static final Long LISTING_ID = 1L;
  private static final Long UNKNOWN_ID = 99L;
  private static final String ADMIN_EMAIL = "admin@tortiki.fr";
  private static final String SELLER_EMAIL = "sofia@tortiki.fr";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ManageAdminListingsUseCase manageAdminListingsUseCase;

  @MockitoBean
  private AdminListingWebMapper adminListingWebMapper;

  @MockitoBean
  private UserDetailsServiceImpl userDetailsService;

  private Listing listing;

  @BeforeEach
  void setUp() {
    User seller = new User();
    seller.setId(2L);
    seller.setEmail(SELLER_EMAIL);

    listing = new Listing();
    listing.setId(LISTING_ID);
    listing.setTitle("Bortsch ukrainien");
    listing.setPrice(new BigDecimal("8.50"));
    listing.setPickupAddress("12 rue de la Paix, Nancy");
    listing.setStatus(ListingStatus.ACTIVE);
    listing.setSeller(seller);
  }

  // ═══════════════════════════════════════════════════════
  // GET /api/v1/admin/listings
  // ═══════════════════════════════════════════════════════

  @Test
  @Story("Consultation modération")
  @Severity(SeverityLevel.NORMAL)
  @Description("Liste non vide, HTTP 200 avec les annonces au format admin")
  @DisplayName("GET /admin/listings retourne 200 avec la liste des annonces")
  void findAll_shouldReturn200withList() throws Exception {
    when(manageAdminListingsUseCase.findAll()).thenReturn(List.of(listing));
    when(adminListingWebMapper.toResponse(listing)).thenReturn(
        new AdminListingResponse(LISTING_ID, "Bortsch ukrainien", SELLER_EMAIL,
            "12 rue de la Paix, Nancy", new BigDecimal("8.50"), "ACTIVE", null));

    mockMvc.perform(get("/api/v1/admin/listings")
            .with(user(ADMIN_EMAIL).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(LISTING_ID))
        .andExpect(jsonPath("$[0].sellerEmail").value(SELLER_EMAIL))
        .andExpect(jsonPath("$[0].status").value("ACTIVE"));
  }

  @Test
  @Story("Consultation modération")
  @Severity(SeverityLevel.NORMAL)
  @Description("Aucune annonce en base, HTTP 200 avec tableau JSON vide")
  @DisplayName("GET /admin/listings retourne 200 avec tableau vide si aucune annonce")
  void findAll_shouldReturn200withEmptyList() throws Exception {
    when(manageAdminListingsUseCase.findAll()).thenReturn(List.of());

    mockMvc.perform(get("/api/v1/admin/listings")
            .with(user(ADMIN_EMAIL).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @Story("Sécurité")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Utilisateur sans ROLE_ADMIN, HTTP 403 Forbidden")
  @DisplayName("GET /admin/listings retourne 403 si le rôle ADMIN est absent")
  void findAll_shouldReturn403_whenNotAdmin() throws Exception {
    mockMvc.perform(get("/api/v1/admin/listings")
            .with(user(SELLER_EMAIL).authorities(new SimpleGrantedAuthority("ROLE_SELLER"))))
        .andExpect(status().isForbidden());

    verify(manageAdminListingsUseCase, never()).findAll();
  }

  // ═══════════════════════════════════════════════════════
  // PATCH /api/v1/admin/listings/{id}/status
  // ═══════════════════════════════════════════════════════

  @Test
  @Story("Changement de statut")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Admin désactive une annonce, HTTP 200 avec le statut mis à jour")
  @DisplayName("PATCH /admin/listings/{id}/status retourne 200 si la modération réussit")
  void updateStatus_shouldReturn200_whenAdminUpdates() throws Exception {
    listing.setStatus(ListingStatus.INACTIVE);
    when(manageAdminListingsUseCase.updateStatus(LISTING_ID, "INACTIVE"))
        .thenReturn(listing);
    when(adminListingWebMapper.toResponse(listing)).thenReturn(
        new AdminListingResponse(LISTING_ID, "Bortsch ukrainien", SELLER_EMAIL,
            "12 rue de la Paix, Nancy", new BigDecimal("8.50"), "INACTIVE", null));

    mockMvc.perform(patch("/api/v1/admin/listings/{id}/status", LISTING_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(buildStatusBody("INACTIVE"))
            .with(csrf())
            .with(user(ADMIN_EMAIL).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("INACTIVE"));
  }

  @Test
  @Story("Changement de statut")
  @Severity(SeverityLevel.NORMAL)
  @Description("Statut invalide, HTTP 400, le use case n'est pas appelé")
  @DisplayName("PATCH /admin/listings/{id}/status retourne 400 si le statut est invalide")
  void updateStatus_shouldReturn400_whenStatusInvalid() throws Exception {
    mockMvc.perform(patch("/api/v1/admin/listings/{id}/status", LISTING_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(buildStatusBody("CANCELLED"))
            .with(csrf())
            .with(user(ADMIN_EMAIL).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isBadRequest());

    verify(manageAdminListingsUseCase, never()).updateStatus(anyLong(), anyString());
  }

  @Test
  @Story("Changement de statut")
  @Severity(SeverityLevel.NORMAL)
  @Description("Statut vide, HTTP 400, le use case n'est pas appelé")
  @DisplayName("PATCH /admin/listings/{id}/status retourne 400 si le statut est vide")
  void updateStatus_shouldReturn400_whenStatusBlank() throws Exception {
    mockMvc.perform(patch("/api/v1/admin/listings/{id}/status", LISTING_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(buildStatusBody(""))
            .with(csrf())
            .with(user(ADMIN_EMAIL).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isBadRequest());

    verify(manageAdminListingsUseCase, never()).updateStatus(anyLong(), anyString());
  }

  @Test
  @Story("Changement de statut")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Annonce introuvable, HTTP 404 via GlobalExceptionHandler")
  @DisplayName("PATCH /admin/listings/{id}/status retourne 404 si l'annonce est introuvable")
  void updateStatus_shouldReturn404_whenListingNotFound() throws Exception {
    when(manageAdminListingsUseCase.updateStatus(UNKNOWN_ID, "INACTIVE"))
        .thenThrow(new ListingNotFoundException(
            "Annonce introuvable pour l'identifiant " + UNKNOWN_ID));

    mockMvc.perform(patch("/api/v1/admin/listings/{id}/status", UNKNOWN_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(buildStatusBody("INACTIVE"))
            .with(csrf())
            .with(user(ADMIN_EMAIL).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isNotFound());

    verify(adminListingWebMapper, never()).toResponse(any());
  }

  @Test
  @Story("Sécurité")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Utilisateur sans ROLE_ADMIN, HTTP 403 Forbidden")
  @DisplayName("PATCH /admin/listings/{id}/status retourne 403 si le rôle ADMIN est absent")
  void updateStatus_shouldReturn403_whenNotAdmin() throws Exception {
    mockMvc.perform(patch("/api/v1/admin/listings/{id}/status", LISTING_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content(buildStatusBody("INACTIVE"))
            .with(csrf())
            .with(user(SELLER_EMAIL).authorities(new SimpleGrantedAuthority("ROLE_SELLER"))))
        .andExpect(status().isForbidden());

    verify(manageAdminListingsUseCase, never()).updateStatus(anyLong(), anyString());
  }

  /**
   * Corps JSON d'une requête de changement de statut.
   *
   * @param newStatus valeur du statut à transmettre
   * @return corps JSON sérialisé
   * @throws Exception en cas d'échec de sérialisation
   */
  private String buildStatusBody(final String newStatus) throws Exception {
    return objectMapper.writeValueAsString(Map.of("newStatus", newStatus));
  }
}