package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.application.port.in.ManageContactRequestUseCase;
import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.infrastructure.adapter.in.web.dto.ContactRequestSummaryResponse;
import com.tortiki.api.infrastructure.adapter.in.web.dto.UpdateContactRequestStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST du tableau de bord vendeur.
 *
 * <p>Exposer les endpoints de gestion des demandes de contact reçues
 * par le vendeur authentifié.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/seller/dashboard")
@RequiredArgsConstructor
@Tag(name = "Tableau de bord vendeur", description = "Gestion des demandes reçues")
public class SellerDashboardController {

  private final ManageContactRequestUseCase manageContactRequestUseCase;

  /**
   * Retourne les demandes de contact reçues par le vendeur authentifié.
   *
   * @param principal principal Spring Security — email du vendeur connecté
   * @return liste des demandes reçues, triées par date décroissante
   */
  @GetMapping("/contact-requests")
  @PreAuthorize("hasRole('SELLER')")
  @Operation(
      summary = "Tableau de bord vendeur",
      description = "Retourne toutes les demandes de contact reçues par le vendeur authentifié."
  )
  @ApiResponse(responseCode = "200", description = "Liste retournée avec succès")
  @ApiResponse(responseCode = "401", description = "Non authentifié")
  @ApiResponse(responseCode = "403", description = "Accès réservé aux vendeurs")
  public ResponseEntity<List<ContactRequestSummaryResponse>> getDashboard(
      final Principal principal) {
    String sellerEmail = principal.getName();
    log.info("Dashboard vendeur {} : consultation des demandes", sellerEmail);

    List<ContactRequestSummaryResponse> response =
        manageContactRequestUseCase.findBySeller(sellerEmail)
            .stream()
            .map(ContactRequestSummaryResponse::from)
            .toList();

    log.info("Dashboard vendeur {} : {} demande(s) retournée(s)",
        sellerEmail, response.size());

    return ResponseEntity.ok(response);
  }

  /**
   * Met à jour le statut d'une demande de contact reçue par le vendeur.
   *
   * <p>Seul le vendeur propriétaire de l'annonce concernée peut confirmer ou refuser.
   * Les statuts {@code CONFIRMED} et {@code REFUSED} sont définitifs.</p>
   *
   * @param id        identifiant de la demande à mettre à jour
   * @param request   DTO contenant le nouveau statut
   * @param principal principal Spring Security — email du vendeur connecté
   * @return la demande mise à jour
   */
  @PatchMapping("/contact-requests/{id}/status")
  @PreAuthorize("hasRole('SELLER')")
  @Operation(
      summary = "Confirmer ou refuser une demande",
      description = "Met à jour le statut d'une demande de contact. "
          + "Seul PENDING → CONFIRMED ou PENDING → REFUSED est autorisé."
  )
  @ApiResponse(responseCode = "200", description = "Statut mis à jour avec succès")
  @ApiResponse(responseCode = "400", description = "Statut invalide ou transition interdite")
  @ApiResponse(responseCode = "401", description = "Non authentifié")
  @ApiResponse(responseCode = "403", description = "Accès réservé aux vendeurs")
  @ApiResponse(responseCode = "404", description = "Demande introuvable ou hors périmètre")
  public ResponseEntity<ContactRequestSummaryResponse> updateStatus(
      @PathVariable final Long id,
      @Valid @RequestBody final UpdateContactRequestStatusRequest request,
      final Principal principal) {
    String sellerEmail = principal.getName();
    log.info("Vendeur {} : mise à jour statut demande #{} → {}",
        sellerEmail, id, request.newStatus());

    ManageContactRequestUseCase.UpdateStatusCommand command =
        new ManageContactRequestUseCase.UpdateStatusCommand(id, sellerEmail, request.newStatus());
    ContactRequest updated = manageContactRequestUseCase.updateStatus(command);

    log.info("Demande #{} : statut mis à jour → {}", id, updated.getStatus());
    return ResponseEntity.ok(ContactRequestSummaryResponse.from(updated));
  }
}