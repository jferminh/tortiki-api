package com.tortiki.api.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tortiki.api.application.port.in.RegisterUserUseCase;
import com.tortiki.api.config.SecurityConfig;
import com.tortiki.api.config.SecurityConstants;
import com.tortiki.api.domain.exception.UserAlreadyExistsException;
import com.tortiki.api.domain.model.Role;
import com.tortiki.api.domain.model.RoleName;
import com.tortiki.api.domain.model.User;
import com.tortiki.api.infrastructure.adapter.in.web.dto.UserResponse;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests unitaires de la couche REST {@link AuthController}.
 *
 * <p>Utilise {@code @WebMvcTest} pour charger uniquement la couche web.
 * Les ports primaires et {@link AuthenticationManager} sont mockés
 * via {@code @MockitoBean} — aucune base de données n'est sollicitée.</p>
 *
 * <p>Le CSRF est activé via {@code .with(csrf())} pour simuler
 * les requêtes légitimes dans le contexte Spring Security stateful.</p>
 */
@Epic("Authentification")
@Feature("Endpoints REST auth")
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("AuthController — Tests unitaires WebMvcTest")
class AuthControllerTest {

  // ── Constantes de test ────────────────────────────────────────────────────

  private static final String TEST_EMAIL    = "sofia@example.com";
  private static final String TEST_PASSWORD = "password123";
  private static final String TEST_FIRST    = "Sofia";
  private static final String TEST_LAST     = "Kovalenko";
  private static final String TEST_ROLE     = "SELLER";

  private static final String INVALID_EMAIL  = "pas-un-email";
  private static final String EMPTY_PASSWORD = "";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private RegisterUserUseCase registerUserUseCase;

  @MockitoBean
  private AuthenticationManager authenticationManager;

  @MockitoBean
  private UserWebMapper userWebMapper;

  /** Utilisateur domaine retourné par les mocks. */
  private User sofia;

  /** UserResponse retourné par le mapper web. */
  private UserResponse sofiaResponse;

  /**
   * Initialisation des données communes avant chaque test.
   */
  @BeforeEach
  void setUp() {
    sofia = new User();
    sofia.setId(1L);
    sofia.setEmail("sofia@example.com");
    sofia.setFirstName("Sofia");
    sofia.setLastName("Kovalenko");
    sofia.setEnabled(true);
    sofia.addRole(new Role(1L, RoleName.SELLER));

    sofiaResponse = new UserResponse(
        1L, "sofia@example.com", "Sofia", "Kovalenko", Set.of(RoleName.SELLER)
    );
  }

  // ── POST /api/v1/auth/register ────────────────────────────────────────────

  @Test
  @Story("Inscription")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Sofia s'inscrit — HTTP 201 avec le profil utilisateur en réponse.")
  @DisplayName("POST /register — retourne 201 avec UserResponse si l'inscription réussit")
  void register_shouldReturn201_whenSuccessful() throws Exception {
    when(registerUserUseCase.register(
        new RegisterUserUseCase.Command(
            TEST_EMAIL, TEST_PASSWORD, TEST_FIRST, TEST_LAST, RoleName.SELLER)
    )).thenReturn(sofia);
    when(userWebMapper.toResponse(sofia)).thenReturn(sofiaResponse);

    mockMvc.perform(post(SecurityConstants.ROUTE_AUTH_REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(buildRegisterBody())
            .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.email").value("sofia@example.com"))
        .andExpect(jsonPath("$.firstName").value("Sofia"))
        .andExpect(jsonPath("$.lastName").value("Kovalenko"));
  }

  @Test
  @Story("Inscription")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Email déjà utilisé — HTTP 409 Conflict avec body ErrorResponse.")
  @DisplayName("POST /register — retourne 409 si l'email est déjà utilisé")
  void register_shouldReturn409_whenEmailAlreadyExists() throws Exception {
    when(registerUserUseCase.register(any(RegisterUserUseCase.Command.class)))
        .thenThrow(new UserAlreadyExistsException("Email déjà utilisé"));

    mockMvc.perform(post(SecurityConstants.ROUTE_AUTH_REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(buildRegisterBody())
            .with(csrf()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.message").exists());

    verify(userWebMapper, never()).toResponse(any(User.class));
  }

  @Test
  @Story("Inscription")
  @Severity(SeverityLevel.NORMAL)
  @Description("Champs manquants dans la requête — HTTP 400 Bad Request.")
  @DisplayName("POST /register — retourne 400 si les champs obligatoires sont absents")
  void register_shouldReturn400_whenBodyIsInvalid() throws Exception {
    mockMvc.perform(post(SecurityConstants.ROUTE_AUTH_REGISTER)
            .contentType(MediaType.APPLICATION_JSON)
            .content(buildInvalidRegisterBody())
            .with(csrf()))
        .andExpect(status().isBadRequest());

    verify(registerUserUseCase, never()).register(any(RegisterUserUseCase.Command.class));
  }

  // ── POST /api/v1/auth/login ───────────────────────────────────────────────

  @Test
  @Story("Connexion")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Sofia se connecte avec ses credentials valides — HTTP 200 avec profil.")
  @DisplayName("POST /login — retourne 200 avec UserResponse si les credentials sont valides")
  void login_shouldReturn200_whenCredentialsAreValid() throws Exception {
    UserDetails principalMock = new org.springframework.security.core.userdetails.User(
        "sofia@example.com",
        "",
        List.of(new SimpleGrantedAuthority("ROLE_SELLER"))
    );
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(
            principalMock,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_SELLER"))
        );

    when(authenticationManager.authenticate(any())).thenReturn(authToken);
    when(userWebMapper.toResponse(any(UserDetails.class))).thenReturn(sofiaResponse);

    mockMvc.perform(post(SecurityConstants.ROUTE_AUTH_LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(buildLoginBody())
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("sofia@example.com"))
        .andExpect(jsonPath("$.firstName").value("Sofia"));
  }

  @Test
  @Story("Connexion")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Mauvais mot de passe — HTTP 401 Unauthorized avec body ErrorResponse OWASP.")
  @DisplayName("POST /login — retourne 401 si les credentials sont invalides")
  void login_shouldReturn401_whenCredentialsAreInvalid() throws Exception {
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Credentials invalides"));

    mockMvc.perform(post(SecurityConstants.ROUTE_AUTH_LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(buildLoginBody())
            .with(csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.message").value("Identifiants invalides"));

    verify(userWebMapper, never()).toResponse(any(UserDetails.class));
  }

  @Test
  @Story("Connexion")
  @Severity(SeverityLevel.NORMAL)
  @Description("Corps de requête invalide — HTTP 400 Bad Request.")
  @DisplayName("POST /login — retourne 400 si l'email est invalide")
  void login_shouldReturn400_whenBodyIsInvalid() throws Exception {
    mockMvc.perform(post(SecurityConstants.ROUTE_AUTH_LOGIN)
            .contentType(MediaType.APPLICATION_JSON)
            .content(buildInvalidLoginBody())
            .with(csrf()))
        .andExpect(status().isBadRequest());

    verify(authenticationManager, never()).authenticate(any());
  }

  // ── POST /api/v1/auth/logout ──────────────────────────────────────────────

  @Test
  @Story("Déconnexion")
  @Severity(SeverityLevel.NORMAL)
  @Description("Déconnexion — session invalidée, HTTP 204 No Content retourné.")
  @DisplayName("POST /logout — retourne 204 et invalide la session")
  void logout_shouldReturn204() throws Exception {
    mockMvc.perform(post(SecurityConstants.ROUTE_AUTH_LOGOUT)
            .with(csrf()))
        .andExpect(status().isNoContent());
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  /**
   * Construit le corps JSON d'une requête d'inscription.
   */
  private String buildRegisterBody() throws Exception {
    return objectMapper.writeValueAsString(Map.of(
        "email", TEST_EMAIL,
        "password", TEST_PASSWORD,
        "firstName", TEST_FIRST,
        "lastName", TEST_LAST,
        "role", TEST_ROLE
    ));
  }

  /**
   * Corps JSON d'une requête d'inscription invalide (email malformé, mot de passe vide).
   */
  private String buildInvalidRegisterBody() throws Exception {
    return objectMapper.writeValueAsString(Map.of(
        "email", INVALID_EMAIL,
        "password", EMPTY_PASSWORD
    ));
  }

  /**
   * Construit le corps JSON d'une requête de connexion.
   */
  private String buildLoginBody() throws Exception {
    return objectMapper.writeValueAsString(Map.of(
        "email", TEST_EMAIL,
        "password", TEST_PASSWORD
    ));
  }

  /**
   * Corps JSON d'une requête de connexion invalide (email malformé, mot de passe vide).
   */
  private String buildInvalidLoginBody() throws Exception {
    return objectMapper.writeValueAsString(Map.of(
        "email", INVALID_EMAIL,
        "password", EMPTY_PASSWORD
    ));
  }
}