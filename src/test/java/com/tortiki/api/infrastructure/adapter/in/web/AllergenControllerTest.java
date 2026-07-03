package com.tortiki.api.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tortiki.api.application.port.in.ManageAllergenUseCase;
import com.tortiki.api.config.SecurityConfig;
import com.tortiki.api.config.SecurityConstants;
import com.tortiki.api.domain.model.Allergen;
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
 * Tests unitaires du contrôleur {@link AllergenController}.
 *
 * <p>Utilise {@code @WebMvcTest} pour charger uniquement la couche web.
 * {@link ManageAllergenUseCase} est mocké — aucune base de données sollicitée.</p>
 *
 * <p>Vérifie la lecture publique et le CRUD réservé à {@code ROLE_ADMIN},
 * conformément au pattern déjà appliqué sur {@code CuisineTypeControllerTest}.</p>
 */
@Epic("Allergènes")
@Feature("Endpoints REST allergens")
@WebMvcTest(AllergenController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("AllergenController — Tests unitaires")
class AllergenControllerTest {

  private static final Long ALLERGEN_ID = 1L;
  private static final String ALLERGEN_NAME = "Gluten";
  private static final String ADMIN_EMAIL = "admin@tortiki.fr";
  private static final String SELLER_EMAIL = "sofia@tortiki.fr";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ManageAllergenUseCase manageAllergenUseCase;

  private Allergen buildAllergen() {
    Allergen allergen = new Allergen();
    allergen.setId(ALLERGEN_ID);
    allergen.setName(ALLERGEN_NAME);
    allergen.setEnabled(true);
    return allergen;
  }

  @Test
  @Story("Lecture publique")
  @Description("Tout visiteur, authentifié ou non, peut consulter la liste des allergènes.")
  @DisplayName("GET /api/v1/allergens — retourne 200 sans authentification")
  void shouldReturn200WhenListingAllergensWithoutAuth() throws Exception {
    when(manageAllergenUseCase.findAll()).thenReturn(List.of(buildAllergen()));

    mockMvc.perform(get(SecurityConstants.ROUTE_ALLERGENS))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value(ALLERGEN_NAME));
  }

  @Test
  @Story("Contrôle d'accès RBAC")
  @Description("Un administrateur authentifié peut créer un nouvel allergène.")
  @DisplayName("POST /api/v1/allergens — retourne 201 pour ROLE_ADMIN")
  void shouldReturn201WhenAdminCreatesAllergen() throws Exception {
    when(manageAllergenUseCase.create(any())).thenReturn(buildAllergen());

    mockMvc.perform(post(SecurityConstants.ROUTE_ALLERGENS)
            .with(user(ADMIN_EMAIL).roles("ADMIN"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(buildAllergen())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value(ALLERGEN_NAME));
  }

  @Test
  @Story("Contrôle d'accès RBAC")
  @Description("Un vendeur ne peut pas créer d'allergène — réservé à ROLE_ADMIN.")
  @DisplayName("POST /api/v1/allergens — retourne 403 pour ROLE_SELLER")
  void shouldReturn403WhenSellerTriesToCreateAllergen() throws Exception {
    mockMvc.perform(post(SecurityConstants.ROUTE_ALLERGENS)
            .with(user(SELLER_EMAIL).roles("SELLER"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(buildAllergen())))
        .andExpect(status().isForbidden());

    verify(manageAllergenUseCase, never()).create(any());
  }

  @Test
  @Story("Contrôle d'accès RBAC")
  @Description("Un utilisateur non authentifié ne peut pas créer d'allergène.")
  @DisplayName("POST /api/v1/allergens — retourne 401 sans authentification")
  void shouldReturn401WhenUnauthenticatedTriesToCreateAllergen() throws Exception {
    mockMvc.perform(post(SecurityConstants.ROUTE_ALLERGENS)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(buildAllergen())))
        .andExpect(status().isUnauthorized());

    verify(manageAllergenUseCase, never()).create(any());
  }

  @Test
  @Story("Suppression")
  @Description("Un administrateur peut supprimer un allergène existant.")
  @DisplayName("DELETE /api/v1/allergens/{id} — retourne 204 pour ROLE_ADMIN")
  void shouldReturn204WhenAdminDeletesAllergen() throws Exception {
    mockMvc.perform(delete(SecurityConstants.ROUTE_ALLERGENS_ALL, ALLERGEN_ID)
            .with(user(ADMIN_EMAIL).roles("ADMIN"))
            .with(csrf()))
        .andExpect(status().isNoContent());

    verify(manageAllergenUseCase).delete(ALLERGEN_ID);
  }
}