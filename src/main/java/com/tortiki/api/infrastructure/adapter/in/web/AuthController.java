package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.application.port.in.RegisterUserUseCase;
import com.tortiki.api.domain.model.User;
import com.tortiki.api.infrastructure.adapter.in.web.dto.LoginRequest;
import com.tortiki.api.infrastructure.adapter.in.web.dto.RegisterRequest;
import com.tortiki.api.infrastructure.adapter.in.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptateur primaire REST pour l'authentification sur la plateforme Tortiki.
 *
 * <p>Expose des endpoints publics d'inscription, de connexion et de déconnexion.
 * Délègue la logique métier au port primaire {@link RegisterUserUseCase}.</p>
 *
 * <p>La gestion de session est stateful (pas de JWT en v1) :
 * Spring Security stocke le contexte d'authentification en session HTTP.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Inscription et connexion des utilisateurs")
public class AuthController {

  private final RegisterUserUseCase registerUserUseCase;
  private final AuthenticationManager authenticationManager;
  private final UserWebMapper userWebMapper;

  /**
   * Inscrit un nouvel utilisateur sur la plateforme.
   *
   * <p>Retourne HTTP 201 avec le profil créé en cas de succès.
   * HTTP 409 si l'email est déjà utilisé — géré par
   * {@link GlobalExceptionHandler#handleUserAlreadyExists}.</p>
   *
   * @param request les données d'inscription validées
   * @return le profil utilisateur créé avec HTTP 201
   */
  @PostMapping("/register")
  @Operation(summary = "Inscription d'un nouvel utilisateur")
  @ApiResponse(responseCode = "201", description = "Compte créé avec succès")
  @ApiResponse(responseCode = "400", description = "Données invalides")
  @ApiResponse(responseCode = "409", description = "Email déjà utilisé")
  public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
    log.debug("Requête d'inscription reçue pour : {}", request.email());
    User created = registerUserUseCase.register(
        request.email(),
        request.password(),
        request.firstName(),
        request.lastName(),
        request.role()
    );
    log.info("Utilisateur inscrit : id={} email={}", created.getId(), created.getEmail());
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(userWebMapper.toResponse(created));
  }

  /**
   * Authentifie un utilisateur et crée une session HTTP stateful.
   *
   * <p>HTTP 401 si les credentials sont invalides — géré par
   * {@link GlobalExceptionHandler} via {@code BadCredentialsException}.</p>
   *
   * @param request        les credentials de connexion validée
   * @param servletRequest la requête HTTP pour la gestion de session
   * @return le profil utilisateur authentifié avec HTTP 200
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
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email(), request.password())
    );

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);

    servletRequest.getSession(true).setAttribute(
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
        context
    );

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    log.info("Connexion réussie pour : {}", request.email());
    return ResponseEntity.ok(userWebMapper.toResponse(userDetails));
  }

  /**
   * Déconnecte l'utilisateur en invalidant sa session HTTP.
   *
   * <p>Délègue à {@link SecurityContextLogoutHandler} pour invalider
   * la session et nettoyer le contexte Spring Security.
   * Retourne HTTP 204 No Content après déconnexion.</p>
   *
   * @param servletRequest  la requête HTTP contenant la session à invalider
   * @param servletResponse la réponse HTTP
   * @return réponse vide avec statut 204
   */
  @PostMapping("/logout")
  @Operation(summary = "Déconnexion de l'utilisateur")
  @ApiResponse(responseCode = "204", description = "Déconnexion réussie")
  public ResponseEntity<Void> logout(
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = (authentication != null) ? authentication.getName() : "anonyme";

    new SecurityContextLogoutHandler().logout(servletRequest, servletResponse, authentication);

    log.info("Déconnexion de : {}", email);
    return ResponseEntity.noContent().build();
  }
}