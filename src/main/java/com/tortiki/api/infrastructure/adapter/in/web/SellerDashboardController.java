package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.application.port.in.ManageContactRequestUseCase;
import com.tortiki.api.infrastructure.adapter.in.web.dto.ContactRequestSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
}