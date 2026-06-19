package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.application.port.in.SubmitContactRequestUseCase;
import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.infrastructure.adapter.in.web.dto.ContactRequestResponse;
import com.tortiki.api.infrastructure.adapter.in.web.dto.CreateContactRequestRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour la gestion des demandes de contact.
 *
 * <p>Expose l'endpoint {@code POST /api/contact-requests} permettant
 * à un acheteur authentifié d'exprimer son intérêt pour une annonce.</p>
 *
 * <p>Accès restreint au rôle {@code ROLE_BUYER} via Spring Security.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/contact-requests")
@RequiredArgsConstructor
@Tag(
    name = "Contact Requests",
    description = "Soumission de demandes d'intérêt pour une annonce"
)
public class ContactRequestController {

  /** Port primaire du cas d'usage de soumission d'une demande de contact. */
  private final SubmitContactRequestUseCase submitContactRequestUseCase;

  /**
   * Soumet une demande de contact pour une annonce.
   *
   * <p>Réservé aux acheteurs authentifiés ({@code ROLE_BUYER}).
   * L'email de l'acheteur est résolu depuis la session Spring Security
   * et transmis au service — jamais fourni par le client.</p>
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
      @Valid @RequestBody CreateContactRequestRequest request,
      Principal principal
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
}