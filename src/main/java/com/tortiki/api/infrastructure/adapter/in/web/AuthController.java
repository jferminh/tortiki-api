package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.application.port.in.FindUserUseCase;
import com.tortiki.api.application.port.in.RegisterUserUseCase;
import com.tortiki.api.domain.exception.UserAlreadyExistsException;
import com.tortiki.api.domain.model.User;
import com.tortiki.api.infrastructure.adapter.in.web.dto.LoginRequest;
import com.tortiki.api.infrastructure.adapter.in.web.dto.RegisterRequest;
import com.tortiki.api.infrastructure.adapter.in.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptateur primaire REST pour l'authentification sur la plateforme Tortiki.
 *
 * <p>Exposer les endpoints publics d'inscription et de connexion.
 * Délègue la logique métier aux ports primaires
 * {@link RegisterUserUseCase} et {@link FindUserUseCase}.</p>
 *
 * <p>La gestion de session est stateful (pas de JWT en v1) :
 * Spring Security stocke le contexte d'authentification en session HTTP.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Inscription et connexion des utilisateurs")
public class AuthController {

  private final RegisterUserUseCase registerUserUseCase;
  private final FindUserUseCase findUserUseCase;
  private final AuthenticationManager authenticationManager;
  private final UserWebMapper userWebMapper;

  /**
   * Inscrit un nouvel utilisateur sur la plateforme.
   *
   * <p>Retourne HTTP 201 avec le profil créé en cas de succès.
   * Retourne HTTP 409 si l'adresse email est déjà utilisée.</p>
   *
   * @param request les données d'inscription validées
   * @return le profil utilisateur créé
   */
  @PostMapping("/register")
  @Operation(summary = "Inscription d'un nouvel utilisateur")
  @ApiResponse(responseCode = "201", description = "Compte créé avec succès")
  @ApiResponse(responseCode = "400", description = "Données invalides")
  @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
  public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
    log.debug("Requête d'inscription reçue pour : {}", request.email());

    try {
      User created = registerUserUseCase.register(
          request.email(),
          request.password(),
          request.firstName(),
          request.lastName(),
          request.role()
      );
      return ResponseEntity
          .status(HttpStatus.CREATED)
          .body(userWebMapper.toResponse(created));

    } catch (UserAlreadyExistsException ex) {
      log.warn("Tentative d'inscription avec un email existant : {}", request.email());
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
  }

  /**
   * Authentifie un utilisateur et crée une session HTTP.
   *
   * <p>Spring Security vérifie les credentials via {@link AuthenticationManager}.
   * En cas de succès, le contexte d'authentification est stocké en session
   * pour les requêtes suivantes.</p>
   *
   * <p>Retourne HTTP 200 avec le profil utilisateur en cas de succès.
   * Retourne HTTP 401 si les credentials sont invalides.</p>
   *
   * @param request        les credentials de connexion validée
   * @param servletRequest la requête HTTP pour la gestion de session
   * @return le profil utilisateur authentifié
   */
  @PostMapping("/login")
  @Operation(summary = "Connexion d'un utilisateur existant")
  @ApiResponse(responseCode = "200", description = "Connexion réussie")
  @ApiResponse(responseCode = "400", description = "Données invalides")
  @ApiResponse(responseCode = "401", description = "Credentials invalides")
  public ResponseEntity<UserResponse> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest servletRequest) {

    log.debug("Requête de connexion reçue pour : {}", request.email());

    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.email(), request.password())
      );

      SecurityContext context = SecurityContextHolder.createEmptyContext();
      context.setAuthentication(authentication);
      SecurityContextHolder.setContext(context);

      HttpSession session = servletRequest.getSession(true);
      session.setAttribute(
          HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
          context
      );

      User user = findUserUseCase.findByEmail(request.email());
      log.info("Connexion réussie pour : {}", request.email());
      return ResponseEntity.ok(userWebMapper.toResponse(user));

    } catch (BadCredentialsException ex) {
      log.warn("Tentative de connexion échouée pour : {}", request.email());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  /**
   * Déconnecte l'utilisateur en invalidant sa session HTTP.
   *
   * <p>Retourne HTTP 204 No Content après invalidation de la session.</p>
   *
   * @param servletRequest la requête HTTP contenant la session à invalider
   * @return réponse vide avec statut 204
   */
  @PostMapping("/logout")
  @Operation(summary = "Déconnexion de l'utilisateur")
  @ApiResponse(responseCode = "204", description = "Déconnexion réussie")
  public ResponseEntity<Void> logout(HttpServletRequest servletRequest) {
    HttpSession session = servletRequest.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    SecurityContextHolder.clearContext();
    log.info("Session invalidée");
    return ResponseEntity.noContent().build();
  }
}