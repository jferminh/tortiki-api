package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.domain.exception.AllergenNotFoundException;
import com.tortiki.api.domain.exception.ContactRequestAlreadyExistsException;
import com.tortiki.api.domain.exception.ContactRequestNotFoundException;
import com.tortiki.api.domain.exception.CuisineTypeNotFoundException;
import com.tortiki.api.domain.exception.ListingNotFoundException;
import com.tortiki.api.domain.exception.RoleNotFoundException;
import com.tortiki.api.domain.exception.StorageException;
import com.tortiki.api.domain.exception.UnauthorizedActionException;
import com.tortiki.api.domain.exception.UserAlreadyExistsException;
import com.tortiki.api.domain.exception.UserNotFoundException;
import com.tortiki.api.infrastructure.adapter.in.web.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Gestionnaire global des exceptions de l'API Tortiki.
 *
 * <p>Traduit les exceptions métier du domaine en réponses HTTP JSON
 * structurées. Placé dans la couche {@code infrastructure/adapter/in/web/}
 * car il appartient à l'adaptateur entrant REST — le domaine n'en
 * connaît pas l'existence.</p>
 *
 * <p>Toutes les erreurs retournent un {@link ErrorResponse} uniforme
 * avec le code HTTP, le libellé et le message descriptif.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Libellé HTTP pour les ressources introuvables (404).
   */
  private static final String NOT_FOUND = "Not Found";

  /**
   * Libellé HTTP pour les accès refusés (403).
   */
  private static final String FORBIDDEN = "Forbidden";

  /**
   * Libellé HTTP pour les conflits de données (409).
   */
  private static final String CONFLICT = "Conflict";

  /**
   * Libellé HTTP pour les erreurs de validation (400).
   */
  private static final String BAD_REQUEST = "Bad Request";

  /**
   * Libellé HTTP pour les entités non traitables (422).
   */
  private static final String UNPROCESSABLE = "Unprocessable Entity";

  /**
   * Libellé HTTP pour les erreurs serveur inattendues (500).
   */
  private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

  /**
   * Libellé HTTP pour les erreurs de service externe (503).
   */
  private static final String SERVICE_UNAVAILABLE = "Service Unavailable";


  /**
   * Gère les tentatives d'inscription avec un email déjà utilisé.
   *
   * @param ex exception levée par {@code RegisterUserUseCase}
   * @return réponse HTTP 409 Conflict
   */
  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
      UserAlreadyExistsException ex) {
    log.warn("Tentative d'inscription avec un email existant : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ErrorResponse.of(409, CONFLICT, ex.getMessage()));
  }

  /**
   * Gère les recherches d'utilisateur infructueuses.
   *
   * @param ex exception levée par {@code FindUserUseCase}
   * @return réponse HTTP 404 Not Found
   */
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFound(
      UserNotFoundException ex) {
    log.warn("Utilisateur introuvable : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.of(404, NOT_FOUND, ex.getMessage()));
  }

  /**
   * Gère les recherches d'annonce infructueuses.
   *
   * @param ex exception levée par {@code ManageListingUseCase}
   * @return réponse HTTP 404 Not Found
   */
  @ExceptionHandler(ListingNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleListingNotFound(
      ListingNotFoundException ex) {
    log.warn("Annonce introuvable : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.of(404, NOT_FOUND, ex.getMessage()));
  }

  /**
   * Gère les recherches d'origine culinaire infructueuses.
   *
   * @param ex exception levée par {@code ManageCuisineTypeUseCase}
   * @return réponse HTTP 404 Not Found
   */
  @ExceptionHandler(CuisineTypeNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleCuisineTypeNotFound(
      CuisineTypeNotFoundException ex) {
    log.warn("Origine culinaire introuvable : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.of(404, NOT_FOUND, ex.getMessage()));
  }

  /**
   * Gère les recherches d'allergène infructueuses.
   *
   * @param ex exception levée lors de la validation des allergènes d'une annonce
   * @return réponse HTTP 404 Not Found
   */
  @ExceptionHandler(AllergenNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAllergenNotFound(
      AllergenNotFoundException ex) {
    log.warn("Allergène introuvable : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.of(404, NOT_FOUND, ex.getMessage()));
  }

  /**
   * Gère les recherches de demande de contact infructueuses.
   *
   * @param ex exception levée par {@code ContactRequestService}
   * @return réponse HTTP 404 Not Found
   */
  @ExceptionHandler(ContactRequestNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleContactRequestNotFound(
      ContactRequestNotFoundException ex) {
    log.warn("Demande de contact introuvable : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.of(404, NOT_FOUND, ex.getMessage()));
  }

  /**
   * Gère les tentatives de double demande sur une même annonce.
   *
   * @param ex exception levée par {@code ContactRequestService}
   * @return réponse HTTP 409 Conflict
   */
  @ExceptionHandler(ContactRequestAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleContactRequestAlreadyExists(
      ContactRequestAlreadyExistsException ex) {
    log.warn("Demande de contact en doublon : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ErrorResponse.of(409, CONFLICT, ex.getMessage()));
  }

  /**
   * Gère les rôles introuvables en base lors de l'inscription.
   *
   * @param ex exception levée par {@code UserService}
   * @return réponse HTTP 422 Unprocessable Entity
   */
  @ExceptionHandler(RoleNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleRoleNotFound(
      RoleNotFoundException ex) {
    log.error("Rôle introuvable — vérifier Flyway V1 : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(ErrorResponse.of(422, UNPROCESSABLE, ex.getMessage()));
  }

  /**
   * Gère les tentatives d'action non autorisée sur une ressource.
   *
   * @param ex exception levée par les services métier
   * @return réponse HTTP 403 Forbidden
   */
  @ExceptionHandler(UnauthorizedActionException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorizedAction(
      UnauthorizedActionException ex) {
    log.warn("Action non autorisée : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(ErrorResponse.of(403, FORBIDDEN, ex.getMessage()));
  }

  /**
   * Gère les erreurs de validation Bean Validation ({@code @Valid}).
   *
   * @param ex exception levée par Spring Validation
   * @return réponse HTTP 400 Bad Request avec le détail des champs invalides
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      MethodArgumentNotValidException ex) {

    String message = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(fe -> fe.getField() + " : " + fe.getDefaultMessage())
        .collect(java.util.stream.Collectors.joining(" | "));
    log.warn("Erreur de validation : {}", message);
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.of(400, BAD_REQUEST, message));
  }

  /**
   * Délègue {@link AccessDeniedException} à Spring Security.
   *
   * <p>Cette exception doit remonter à l'{@code ExceptionTranslationFilter}
   * pour être traduite en HTTP 403 par l'{@code accessDeniedHandler}.
   * L'attraper ici la transformerait en HTTP 500.</p>
   *
   * @param ex l'exception d'accès refusé Spring Security legacy
   * @throws AccessDeniedException relancée vers Spring Security
   */
  @ExceptionHandler(AccessDeniedException.class)
  public void handleAccessDenied(AccessDeniedException ex)
      throws AccessDeniedException {
    throw ex;
  }

  /**
   * Délègue {@link AuthorizationDeniedException} à Spring Security.
   *
   * <p>Levée par {@code @PreAuthorize} depuis Spring Security 6.
   * Doit remonter à l'{@code ExceptionTranslationFilter}
   * pour être traduite en HTTP 403.</p>
   *
   * @param ex l'exception d'autorisation refusée Spring Security 6
   * @throws AuthorizationDeniedException relancée vers Spring Security
   */
  @ExceptionHandler(AuthorizationDeniedException.class)
  public void handleAuthorizationDenied(AuthorizationDeniedException ex)
      throws AuthorizationDeniedException {
    throw ex;
  }

  /**
   * Gère les tentatives de connexion avec des credentials invalides.
   *
   * @param ex l'exception de credentials invalides
   * @return réponse HTTP 401 Unauthorized
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentials(
      BadCredentialsException ex) {
    log.warn("Tentative de connexion échouée : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(ErrorResponse.of(401, "Unauthorized", "Identifiants invalides"));
  }

  /**
   * Gère les échecs de stockage de fichier (upload MinIO).
   *
   * <p>Retourne HTTP 503, car l'échec provient d'un service externe
   * (MinIO), pas d'une erreur de l'utilisateur ni du code métier.</p>
   *
   * @param ex exception levée par {@code MinioStorageAdapter}
   * @return réponse HTTP 503 Service Unavailable
   */
  @ExceptionHandler(StorageException.class)
  public ResponseEntity<ErrorResponse> handleStorage(StorageException ex) {
    log.error("Échec du stockage fichier : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(ErrorResponse.of(503, SERVICE_UNAVAILABLE,
            "Le service de stockage est temporairement indisponible"));
  }

  /**
   * Gère toutes les exceptions non prévues explicitement.
   *
   * @param ex exception inattendue
   * @return réponse HTTP 500 Internal Server Error
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    log.error("Erreur inattendue : {}", ex.getMessage(), ex);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of(500, INTERNAL_SERVER_ERROR,
            "Une erreur inattendue s'est produite"));
  }
}