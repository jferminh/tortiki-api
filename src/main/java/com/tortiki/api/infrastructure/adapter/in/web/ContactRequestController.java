package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.application.port.in.ManageContactRequestUseCase;
import com.tortiki.api.application.port.in.SubmitContactRequestUseCase;
import com.tortiki.api.config.SecurityConstants;
import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
import com.tortiki.api.infrastructure.adapter.in.web.dto.ContactRequestResponse;
import com.tortiki.api.infrastructure.adapter.in.web.dto.ContactRequestSummaryResponse;
import com.tortiki.api.infrastructure.adapter.in.web.dto.CreateContactRequestRequest;
import com.tortiki.api.infrastructure.adapter.in.web.dto.UpdateContactRequestStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour la gestion des demandes de contact.
 *
 * <p>Expose trois cas d'usage : soumission par l'acheteur ({@code
 * ROLE_BUYER}), consultation du tableau de bord et confirmation/refus
 * par le vendeur ({@code ROLE_SELLER}).</p>
 */
@Slf4j
@RestController
@RequestMapping(SecurityConstants.ROUTE_CONTACT_REQUESTS)
@RequiredArgsConstructor
@Tag(
    name = "Contact Requests",
    description = "Soumission et gestion des demandes d'intérêt pour une annonce"
)
public class ContactRequestController {

  /** Port primaire du cas d'usage de soumission d'une demande de contact. */
  private final SubmitContactRequestUseCase submitContactRequestUseCase;

  /** Port primaire du cas d'usage de gestion des demandes côté vendeur. */
  private final ManageContactRequestUseCase manageContactRequestUseCase;

  /**
   * Soumet une demande de contact pour une annonce.
   *
   * @param request   corps de la requête JSON validé par Bean Validation
   * @param principal utilisateur authentifié injecté par Spring Security
   * @return la demande créée avec statut HTTP {@code 201 Created}
   */
  @Operation(
      summary = "Soumettre une demande de contact",
      description = "Permet à un acheteur authentifié d'exprimer son intérêt "
          + "pour une annonce. Une seule demande par annonce est autorisée.",
      security = @SecurityRequirement(name = "cookieAuth")
  )
  @ApiResponse(responseCode = "201", description = "Demande créée avec succès")
  @ApiResponse(responseCode = "400", description = "Données invalides ou règle métier violée")
  @ApiResponse(responseCode = "401", description = "Authentification requise")
  @ApiResponse(responseCode = "403", description = "Rôle ROLE_BUYER requis")
  @ApiResponse(responseCode = "404", description = "Annonce introuvable")
  @PostMapping
  @PreAuthorize("hasRole('BUYER')")
  public ResponseEntity<ContactRequestResponse> submit(
      @Valid @RequestBody final CreateContactRequestRequest request,
      final Principal principal
  ) {
    log.info("Soumission demande de contact — annonce {} par {}",
        request.listingId(), principal.getName());

    SubmitContactRequestUseCase.Command command =
        new SubmitContactRequestUseCase.Command(
            request.listingId(),
            principal.getName(),
            request.message(),
            request.portions()
        );

    ContactRequest contactRequest = submitContactRequestUseCase.submit(command);

    log.info("Demande de contact {} créée — annonce {} acheteur {}",
        contactRequest.getId(),
        contactRequest.getListing().getId(),
        contactRequest.getBuyer().getId()
    );

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ContactRequestResponse.from(contactRequest));
  }

  /**
   * Récupère les demandes de contact reçues par le vendeur connecté.
   *
   * <p>L'email du vendeur est résolu depuis la session Spring Security —
   * aucun identifiant n'est jamais fourni ni accepté depuis le client.</p>
   *
   * @param principal vendeur authentifié injecté par Spring Security
   * @return liste des demandes reçues, triées par date décroissante
   */
  @Operation(
      summary = "Consulter le tableau de bord des demandes reçues",
      description = "Retourne les demandes de contact reçues sur les "
          + "annonces du vendeur connecté.",
      security = @SecurityRequirement(name = "cookieAuth")
  )
  @ApiResponse(responseCode = "200", description = "Liste des demandes reçues")
  @ApiResponse(responseCode = "401", description = "Authentification requise")
  @ApiResponse(responseCode = "403", description = "Rôle ROLE_SELLER requis")
  @GetMapping
  @PreAuthorize("hasRole('SELLER')")
  public ResponseEntity<List<ContactRequestSummaryResponse>> getDashboard(
      final Principal principal
  ) {
    log.info("Consultation dashboard demandes — vendeur {}", principal.getName());

    List<ContactRequest> requests = manageContactRequestUseCase.findBySeller(principal.getName());

    List<ContactRequestSummaryResponse> response = requests.stream()
        .map(ContactRequestSummaryResponse::from)
        .toList();

    log.info("Dashboard vendeur {} — {} demande(s) retournée(s)",
        principal.getName(), response.size());

    return ResponseEntity.ok(response);
  }

  /**
   * Confirme ou refuse une demande de contact.
   *
   * <p>Seul le vendeur propriétaire de l'annonce concernée peut modifier
   * le statut. Les statuts finaux {@code CONFIRMED} et {@code REFUSED}
   * sont irréversibles — toute nouvelle transition lève une exception
   * métier traduite en {@code 409 Conflict} par le
   * {@code GlobalExceptionHandler}.</p>
   *
   * @param id        identifiant de la demande à mettre à jour
   * @param request   corps de la requête contenant le nouveau statut
   * @param principal vendeur authentifié injecté par Spring Security
   * @return la demande mise à jour
   */
  @Operation(
      summary = "Confirmer ou refuser une demande de contact",
      description = "Transition autorisée uniquement depuis le statut "
          + "PENDING vers CONFIRMED ou REFUSED.",
      security = @SecurityRequirement(name = "cookieAuth")
  )
  @ApiResponse(responseCode = "200", description = "Statut mis à jour avec succès")
  @ApiResponse(responseCode = "401", description = "Authentification requise")
  @ApiResponse(responseCode = "403", description = "Rôle ROLE_SELLER requis")
  @ApiResponse(responseCode = "404", description = "Demande introuvable ou hors périmètre vendeur")
  @ApiResponse(responseCode = "409", description = "Transition de statut invalide")
  @PatchMapping("/{id}/status")
  @PreAuthorize("hasRole('SELLER')")
  public ResponseEntity<ContactRequestSummaryResponse> updateStatus(
      @Parameter(description = "Identifiant de la demande de contact")
      @PathVariable final Long id,
      @Valid @RequestBody final UpdateContactRequestStatusRequest request,
      final Principal principal
  ) {
    log.info("Mise à jour statut demande {} — nouveau statut {} par {}",
        id, request.newStatus(), principal.getName());

    ManageContactRequestUseCase.UpdateStatusCommand command =
        new ManageContactRequestUseCase.UpdateStatusCommand(
            id,
            principal.getName(),
            request.newStatus()
        );

    ContactRequest updated = manageContactRequestUseCase.updateStatus(command);

    log.info("Demande {} statut mis à jour — {}", updated.getId(), updated.getStatus());

    return ResponseEntity.ok(ContactRequestSummaryResponse.from(updated));
  }
}