package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.application.port.in.FindBuyerContactRequestsUseCase;
import com.tortiki.api.config.SecurityConstants;
import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.infrastructure.adapter.in.web.dto.ContactRequestBuyerSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour la consultation de l'historique des demandes de contact
 * d'un acheteur.
 *
 * <p>Expose endpoint {@link SecurityConstants#ROUTE_CONTACT_MY} permettant
 * à un acheteur authentifié de consulter ses propres demandes, tous statuts
 * confondus. Symétrique côté acheteur de {@code SellerDashboardController}
 * (Issue 43-44).</p>
 *
 * <p>Accès restreint au rôle {@link SecurityConstants#ROLE_BUYER}
 * via Spring Security.</p>
 */
@Slf4j
@RestController
@RequestMapping(SecurityConstants.ROUTE_CONTACT_MY)
@RequiredArgsConstructor
@Tag(name = "Buyer Contact Requests",
    description = "Historique des demandes de contact soumises par l'acheteur")
public class BuyerContactRequestController {

  private final FindBuyerContactRequestsUseCase findBuyerContactRequestsUseCase;

  /**
   * Récupère l'historique des demandes de contact de l'acheteur connecté.
   *
   * <p>L'email de l'acheteur est résolu depuis la session Spring Security —
   * jamais fourni par le client, conformément au pattern déjà validé sur
   * {@code ContactRequestController.submit}.</p>
   *
   * @param principal utilisateur authentifié injecté par Spring Security
   * @return la liste des demandes de l'acheteur, avec statut HTTP 200
   */
  @Operation(
      summary = "Consulter l'historique de ses demandes de contact",
      description = "Permet à un acheteur authentifié de consulter toutes ses "
          + "demandes de contact, quel que soit leur statut.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponse(responseCode = "200", description = "Historique récupéré avec succès")
  @ApiResponse(responseCode = "401", description = "Authentification requise")
  @ApiResponse(responseCode = "403", description = "Rôle ROLE_BUYER requis")
  @GetMapping
  @PreAuthorize("hasRole('" + SecurityConstants.ROLE_BUYER + "')")
  public ResponseEntity<List<ContactRequestBuyerSummaryResponse>> findMyRequests(
      Principal principal) {
    log.info("Consultation historique demandes de contact par un acheteur");

    List<ContactRequest> requests = findBuyerContactRequestsUseCase
        .findByBuyer(principal.getName());

    List<ContactRequestBuyerSummaryResponse> response = requests.stream()
        .map(ContactRequestBuyerSummaryResponse::from)
        .toList();

    return ResponseEntity.ok(response);
  }
}