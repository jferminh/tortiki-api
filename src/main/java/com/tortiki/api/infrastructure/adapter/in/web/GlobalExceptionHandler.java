package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.domain.exception.AllergenNotFoundException;
import com.tortiki.api.domain.exception.ContactRequestAlreadyExistsException;
import com.tortiki.api.domain.exception.ContactRequestNotFoundException;
import com.tortiki.api.domain.exception.CuisineTypeInUseException;
import com.tortiki.api.domain.exception.CuisineTypeNotFoundException;
import com.tortiki.api.domain.exception.InvalidStatusTransitionException;
import com.tortiki.api.domain.exception.ListingNotFoundException;
import com.tortiki.api.domain.exception.ReviewAlreadyExistsException;
import com.tortiki.api.domain.exception.ReviewNotAllowedException;
import com.tortiki.api.domain.exception.RoleNotFoundException;
import com.tortiki.api.domain.exception.SelfContactException;
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

  /** Libellé HTTP pour les ressources introuvables (404). */
  private static final String NOT_FOUND = "Not Found";

  /** Libellé HTTP pour les accès refusés (403). */
  private static final String FORBIDDEN = "Forbidden";

  /** Libellé HTTP pour les conflits de données (409). */
  private static final String CONFLICT = "Conflict";

  /** Libellé HTTP pour les erreurs de validation (400). */
  private static final String BAD_REQUEST = "Bad Request";

  /** Libellé HTTP pour les entités non traitables (422). */
  private static final String UNPROCESSABLE = "Unprocessable Entity";

  /** Libellé HTTP pour les erreurs serveur inattendues (500). */
  private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

  /** Libellé HTTP pour les erreurs de service externe (503). */
  private static final String SERVICE_UNAVAILABLE = "Service Unavailable";

  // ═══════════════════════════════════════════════════════
  // Utilisateur
  // ═══════════════════════════════════════════════════════

  /**
   * Gère les tentatives d'inscription avec un email déjà utilisé.
   *
   * @param ex exception levée par {@code RegisterUserUseCase}
   * @return réponse HTTP 409 Conflict
   */
  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
      final UserAlreadyExistsException ex) {
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
      final UserNotFoundException ex) {
    log.warn("Utilisateur introuvable : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.of(404, NOT_FOUND, ex.getMessage()));
  }

  /**
   * Gère les rôles introuvables en base lors de l'inscription.
   *
   * @param ex exception levée par {@code UserService}
   * @return réponse HTTP 422 Unprocessable Entity
   */
  @ExceptionHandler(RoleNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleRoleNotFound(
      final RoleNotFoundException ex) {
    log.error("Rôle introuvable — vérifier Flyway V1 : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(ErrorResponse.of(422, UNPROCESSABLE, ex.getMessage()));
  }

  // ═══════════════════════════════════════════════════════
  // Annonce
  // ═══════════════════════════════════════════════════════

  /**
   * Gère les recherches d'annonce infructueuses.
   *
   * @param ex exception levée par {@code ManageListingUseCase}
   * @return réponse HTTP 404 Not Found
   */
  @ExceptionHandler(ListingNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleListingNotFound(
      final ListingNotFoundException ex) {
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
      final CuisineTypeNotFoundException ex) {
    log.warn("Origine culinaire introuvable : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.of(404, NOT_FOUND, ex.getMessage()));
  }

  /**
   * Gère les tentatives de suppression d'une origine culinaire encore
   * référencée par des annonces actives.
   *
   * @param ex exception levée par {@code CuisineTypeService.delete}
   * @return réponse HTTP 409 Conflict
   */
  @ExceptionHandler(CuisineTypeInUseException.class)
  public ResponseEntity<ErrorResponse> handleCuisineTypeInUse(
      final CuisineTypeInUseException ex) {
    log.warn("Suppression refusée, origine culinaire en usage : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ErrorResponse.of(409, CONFLICT, ex.getMessage()));
  }

  /**
   * Gère les recherches d'allergène infructueuses.
   *
   * @param ex exception levée lors de la validation des allergènes d'une annonce
   * @return réponse HTTP 404 Not Found
   */
  @ExceptionHandler(AllergenNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAllergenNotFound(
      final AllergenNotFoundException ex) {
    log.warn("Allergène introuvable : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.of(404, NOT_FOUND, ex.getMessage()));
  }

  // ═══════════════════════════════════════════════════════
  // Demande de contact
  // ═══════════════════════════════════════════════════════

  /**
   * Gère les recherches de demande de contact infructueuses.
   *
   * @param ex exception levée par {@code ManageContactRequestService}
   * @return réponse HTTP 404 Not Found
   */
  @ExceptionHandler(ContactRequestNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleContactRequestNotFound(
      final ContactRequestNotFoundException ex) {
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
      final ContactRequestAlreadyExistsException ex) {
    log.warn("Demande de contact en doublon : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ErrorResponse.of(409, CONFLICT, ex.getMessage()));
  }

  /**
   * Gère les tentatives d'un vendeur de contacter sa propre annonce.
   *
   * @param ex exception levée par {@code ContactRequestService}
   * @return réponse HTTP 409 Conflict
   */
  @ExceptionHandler(SelfContactException.class)
  public ResponseEntity<ErrorResponse> handleSelfContact(
      final SelfContactException ex) {
    log.warn("Tentative de contact sur sa propre annonce : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ErrorResponse.of(409, CONFLICT, ex.getMessage()));
  }

  /**
   * Gère les transitions de statut interdites sur une demande de contact.
   *
   * <p>Les statuts {@code CONFIRMED} et {@code REFUSED} sont définitifs —
   * toute tentative de modification ultérieure lève cette exception.</p>
   *
   * @param ex exception levée par {@code ManageContactRequestService}
   * @return réponse HTTP 409 Conflict
   */
  @ExceptionHandler(InvalidStatusTransitionException.class)
  public ResponseEntity<ErrorResponse> handleInvalidStatusTransition(
      final InvalidStatusTransitionException ex) {
    log.warn("Transition de statut interdite : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ErrorResponse.of(409, CONFLICT, ex.getMessage()));
  }

  // ═══════════════════════════════════════════════════════
  // Évaluation (Review)
  // ═══════════════════════════════════════════════════════

  /**
   * Gère les tentatives de double évaluation sur une même annonce.
   *
   * <p>Un acheteur ne peut laisser qu'une seule évaluation par annonce.</p>
   *
   * @param ex exception levée par {@code SubmitReviewUseCase}
   * @return réponse HTTP 409 Conflict
   */
  @ExceptionHandler(ReviewAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleReviewAlreadyExists(
      final ReviewAlreadyExistsException ex) {
    log.warn("Évaluation en doublon : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ErrorResponse.of(409, CONFLICT, ex.getMessage()));
  }

  /**
   * Gère les tentatives de notation sans demande confirmée préalable.
   *
   * <p>Un acheteur ne peut évaluer une annonce que si sa demande
   * de contact est au statut {@code CONFIRMED}.</p>
   *
   * @param ex exception levée par {@code SubmitReviewUseCase}
   * @return réponse HTTP 403 Forbidden
   */
  @ExceptionHandler(ReviewNotAllowedException.class)
  public ResponseEntity<ErrorResponse> handleReviewNotAllowed(
      final ReviewNotAllowedException ex) {
    log.warn("Évaluation non autorisée : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(ErrorResponse.of(403, FORBIDDEN, ex.getMessage()));
  }

  // ═══════════════════════════════════════════════════════
  // Sécurité & autorisation
  // ═══════════════════════════════════════════════════════

  /**
   * Gère les tentatives d'action non autorisée sur une ressource.
   *
   * @param ex exception levée par les services métier
   * @return réponse HTTP 403 Forbidden
   */
  @ExceptionHandler(UnauthorizedActionException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorizedAction(
      final UnauthorizedActionException ex) {
    log.warn("Action non autorisée : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.FORBIDDEN)
        .body(ErrorResponse.of(403, FORBIDDEN, ex.getMessage()));
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
  public void handleAccessDenied(final AccessDeniedException ex)
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
  public void handleAuthorizationDenied(final AuthorizationDeniedException ex)
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
      final BadCredentialsException ex) {
    log.warn("Tentative de connexion échouée : {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(ErrorResponse.of(401, "Unauthorized", "Identifiants invalides"));
  }

  // ═══════════════════════════════════════════════════════
  // Validation & technique
  // ═══════════════════════════════════════════════════════

  /**
   * Gère les erreurs de validation Bean Validation ({@code @Valid}).
   *
   * @param ex exception levée par Spring Validation
   * @return réponse HTTP 400 Bad Request avec le détail des champs invalides
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      final MethodArgumentNotValidException ex) {
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
   * Gère les échecs de stockage de fichier (upload MinIO).
   *
   * <p>Retourne HTTP 503, car l'échec provient d'un service externe
   * (MinIO), pas d'une erreur de l'utilisateur ni du code métier.</p>
   *
   * @param ex exception levée par {@code MinioStorageAdapter}
   * @return réponse HTTP 503 Service Unavailable
   */
  @ExceptionHandler(StorageException.class)
  public ResponseEntity<ErrorResponse> handleStorage(final StorageException ex) {
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
  public ResponseEntity<ErrorResponse> handleGeneric(final Exception ex) {
    log.error("Erreur inattendue : {}", ex.getMessage(), ex);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of(500, INTERNAL_SERVER_ERROR,
            "Une erreur inattendue s'est produite"));
  }
}