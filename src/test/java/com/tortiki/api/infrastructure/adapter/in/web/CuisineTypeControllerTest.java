package com.tortiki.api.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.tortiki.api.application.port.in.ManageCuisineTypeUseCase;
import com.tortiki.api.config.SecurityConfig;
import com.tortiki.api.domain.exception.CuisineTypeNotFoundException;
import com.tortiki.api.domain.model.CuisineType;
import com.tortiki.api.infrastructure.adapter.in.web.dto.CuisineTypeResponse;
import com.tortiki.api.infrastructure.adapter.out.persistence.UserDetailsServiceImpl;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests unitaires de la couche REST {@link CuisineTypeController}.
 *
 * <p>Utilise {@code @WebMvcTest} pour charger uniquement la couche web.
 * {@link ManageCuisineTypeUseCase}, {@link CuisineTypeWebMapper}
 * et {@link UserDetailsServiceImpl} sont mockés — aucune base sollicitée.</p>
 *
 * <p>{@link GlobalExceptionHandler} est importé pour que les handlers
 * {@code @ExceptionHandler} soient actifs dans le contexte de test.</p>
 *
 * <p>Le cas 403 vérifie que {@code @PreAuthorize("hasRole('ADMIN')")}
 * bloque les requêtes sans le rôle {@code ROLE_ADMIN}.</p>
 */
@Epic("Référentiel")
@Feature("Endpoints REST cuisine-types")
@WebMvcTest(CuisineTypeController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
@DisplayName("CuisineTypeController — Tests unitaires WebMvcTest")
class CuisineTypeControllerTest {

  // ── Constantes de test ────────────────────────────────────────────────────

  private static final Long   CUISINE_ID   = 1L;
  private static final String CUISINE_NAME = "Ukrainienne";
  private static final String CUISINE_DESC = "Cuisine traditionnelle d'Ukraine";
  private static final String ADMIN_EMAIL  = "admin@tortiki.com";
  private static final Long   UNKNOWN_ID   = 99L;

  // ── MockMvc ───────────────────────────────────────────────────────────────

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  // ── Mocks ─────────────────────────────────────────────────────────────────

  @MockitoBean
  private ManageCuisineTypeUseCase manageCuisineTypeUseCase;

  @MockitoBean
  private CuisineTypeWebMapper cuisineTypeWebMapper;

  /**
   * Mock obligatoire : permet à {@link SecurityConfig} de trouver un
   * {@code UserDetailsService} et d'initialiser correctement la
   * {@code SecurityFilterChain} dans le contexte {@code @WebMvcTest}.
   * Sans ce mock, Spring Boot crée un {@code InMemoryUserDetailsManager}
   * de secours et le routage MVC vers le contrôleur est rompu (HTTP 500).
   */
  @MockitoBean
  private UserDetailsServiceImpl userDetailsService;

  // ── Données communes ──────────────────────────────────────────────────────

  private CuisineType cuisineType;
  private CuisineTypeResponse cuisineTypeResponse;

  @BeforeEach
  void setUp() {
    cuisineType = new CuisineType();
    cuisineType.setId(CUISINE_ID);
    cuisineType.setName(CUISINE_NAME);
    cuisineType.setDescription(CUISINE_DESC);

    cuisineTypeResponse = new CuisineTypeResponse(
        CUISINE_ID, CUISINE_NAME, CUISINE_DESC
    );
  }

  // ── GET /api/v1/cuisine-types ─────────────────────────────────────────────

  @Test
  @Story("Consultation référentiel")
  @Severity(SeverityLevel.NORMAL)
  @Description("Liste non vide — HTTP 200 avec les origines culinaires.")
  @DisplayName("GET /cuisine-types — retourne 200 avec la liste des origines culinaires")
  void findAll_shouldReturn200_withList() throws Exception {
    when(manageCuisineTypeUseCase.findAll()).thenReturn(List.of(cuisineType));
    when(cuisineTypeWebMapper.toResponse(cuisineType)).thenReturn(cuisineTypeResponse);

    mockMvc.perform(get("/api/v1/cuisine-types"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(CUISINE_ID))
        .andExpect(jsonPath("$[0].name").value(CUISINE_NAME))
        .andExpect(jsonPath("$[0].description").value(CUISINE_DESC));
  }

  @Test
  @Story("Consultation référentiel")
  @Severity(SeverityLevel.NORMAL)
  @Description("Référentiel vide — HTTP 200 avec tableau JSON vide.")
  @DisplayName("GET /cuisine-types — retourne 200 avec tableau vide si aucune origine")
  void findAll_shouldReturn200_withEmptyList() throws Exception {
    when(manageCuisineTypeUseCase.findAll()).thenReturn(List.of());

    mockMvc.perform(get("/api/v1/cuisine-types"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  // ── GET /api/v1/cuisine-types/{id} ───────────────────────────────────────

  @Test
  @Story("Détail origine culinaire")
  @Severity(SeverityLevel.NORMAL)
  @Description("Origine culinaire trouvée — HTTP 200 avec le détail.")
  @DisplayName("GET /cuisine-types/{id} — retourne 200 avec l'origine culinaire")
  void findById_shouldReturn200_whenFound() throws Exception {
    when(manageCuisineTypeUseCase.findById(CUISINE_ID)).thenReturn(cuisineType);
    when(cuisineTypeWebMapper.toResponse(cuisineType)).thenReturn(cuisineTypeResponse);

    mockMvc.perform(get("/api/v1/cuisine-types/{id}", CUISINE_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(CUISINE_ID))
        .andExpect(jsonPath("$.name").value(CUISINE_NAME));
  }

  @Test
  @Story("Détail origine culinaire")
  @Severity(SeverityLevel.NORMAL)
  @Description("Origine culinaire introuvable — HTTP 404 via GlobalExceptionHandler.")
  @DisplayName("GET /cuisine-types/{id} — retourne 404 si l'origine est introuvable")
  void findById_shouldReturn404_whenNotFound() throws Exception {
    when(manageCuisineTypeUseCase.findById(UNKNOWN_ID))
        .thenThrow(new CuisineTypeNotFoundException(
            "Origine culinaire introuvable pour l'identifiant : " + UNKNOWN_ID
        ));

    mockMvc.perform(get("/api/v1/cuisine-types/{id}", UNKNOWN_ID))
        .andExpect(status().isNotFound());

    verify(cuisineTypeWebMapper, never()).toResponse(any());
  }

  // ── POST /api/v1/cuisine-types ────────────────────────────────────────────

  @Test
  @Story("Création référentiel")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Admin crée une origine culinaire — HTTP 201.")
  @DisplayName("POST /cuisine-types — retourne 201 si la création réussit (ADMIN)")
  void create_shouldReturn201_whenAdminCreates() throws Exception {
    when(manageCuisineTypeUseCase.create(CUISINE_NAME, CUISINE_DESC))
        .thenReturn(cuisineType);
    when(cuisineTypeWebMapper.toResponse(cuisineType)).thenReturn(cuisineTypeResponse);

    mockMvc.perform(post("/api/v1/cuisine-types")
            .contentType(MediaType.APPLICATION_JSON)
            .content(buildCreateBody())
            .with(csrf())
            .with(user(ADMIN_EMAIL)
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(CUISINE_ID))
        .andExpect(jsonPath("$.name").value(CUISINE_NAME));
  }

  @Test
  @Story("Création référentiel")
  @Severity(SeverityLevel.NORMAL)
  @Description("Corps invalide — HTTP 400, le use case n'est pas appelé.")
  @DisplayName("POST /cuisine-types — retourne 400 si le nom est vide")
  void create_shouldReturn400_whenBodyIsInvalid() throws Exception {
    mockMvc.perform(post("/api/v1/cuisine-types")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(Map.of("name", "")))
            .with(csrf())
            .with(user(ADMIN_EMAIL)
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isBadRequest());

    verify(manageCuisineTypeUseCase, never()).create(anyString(), any());
  }

  @Test
  @Story("Création référentiel")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Utilisateur sans ROLE_ADMIN — HTTP 403 Forbidden.")
  @DisplayName("POST /cuisine-types — retourne 403 si le rôle ADMIN est absent")
  void create_shouldReturn403_whenNotAdmin() throws Exception {
    mockMvc.perform(post("/api/v1/cuisine-types")
            .contentType(MediaType.APPLICATION_JSON)
            .content(buildCreateBody())
            .with(csrf())
            .with(user("seller@tortiki.com")
                .authorities(new SimpleGrantedAuthority("ROLE_SELLER"))))
        .andExpect(status().isForbidden());

    verify(manageCuisineTypeUseCase, never()).create(anyString(), any());
  }

  // ── Helper ────────────────────────────────────────────────────────────────

  /**
   * Corps JSON d'une requête de création d'origine culinaire valide.
   */
  private String buildCreateBody() throws Exception {
    return objectMapper.writeValueAsString(Map.of(
        "name", CUISINE_NAME,
        "description", CUISINE_DESC
    ));
  }
}