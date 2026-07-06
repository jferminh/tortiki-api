package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.application.port.in.FindUserUseCase;
import com.tortiki.api.application.port.in.ManageListingUseCase;
import com.tortiki.api.config.SecurityConstants;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.infrastructure.adapter.in.web.dto.ListingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptateur primaire REST pour le tableau de bord vendeur.
 *
 * <p>Distinct de {@link ListingController} : cet endpoint n'expose que les
 * annonces du vendeur authentifié, tous statuts confondus, contrairement
 * aux routes publiques de {@code /api/v1/listings} limitées aux annonces
 * actives.</p>
 *
 * <p>L'identifiant vendeur est toujours résolu depuis Spring Security via
 * {@link FindUserUseCase} — jamais fourni par le client en paramètre de
 * requête, ce qui préviendrait une faille d'IDOR (OWASP A01 — Broken
 * Access Control) permettant à un vendeur de consulter les annonces
 * d'un autre en modifiant l'URL.</p>
 */
@Slf4j
@RestController
@RequestMapping(SecurityConstants.ROUTE_SELLER_LISTINGS)
@RequiredArgsConstructor
@Tag(name = "Tableau de bord vendeur", description = "Gestion des annonces du vendeur connecté")
public class SellerListingController {

  private final ManageListingUseCase manageListingUseCase;
  private final ListingWebMapper listingWebMapper;
  private final FindUserUseCase findUserUseCase;

  /**
   * Retourne toutes les annonces du vendeur authentifié, tous statuts confondus.
   *
   * @param userDetails principal Spring Security du vendeur connecté
   * @return liste des annonces du vendeur, triées par date de création décroissante
   */
  @GetMapping
  @Operation(summary = "Lister toutes mes annonces (ROLE_SELLER)")
  @ApiResponse(responseCode = "200", description = "Liste retournée avec succès")
  @ApiResponse(responseCode = "403", description = "Rôle SELLER requis")
  public ResponseEntity<List<ListingResponse>> findAllForSeller(
      @AuthenticationPrincipal UserDetails userDetails) {

    Long sellerId = findUserUseCase.findByEmail(userDetails.getUsername()).getId();
    List<Listing> listings = manageListingUseCase.findAllForSeller(sellerId);

    log.debug("Tableau de bord vendeur id={} : {} annonce(s)", sellerId, listings.size());

    List<ListingResponse> response = listings.stream()
        .map(listingWebMapper::toResponse)
        .toList();
    return ResponseEntity.ok(response);
  }
}