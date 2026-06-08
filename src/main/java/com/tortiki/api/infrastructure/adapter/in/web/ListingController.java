package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.application.port.in.FindUserUseCase;
import com.tortiki.api.application.port.in.ListingCommand;
import com.tortiki.api.application.port.in.ManageListingUseCase;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.infrastructure.adapter.in.web.dto.CreateListingRequest;
import com.tortiki.api.infrastructure.adapter.in.web.dto.ListingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptateur primaire REST pour la gestion des annonces de plats Tortiki.
 *
 * <p>Expose des endpoints publics de consultation et les endpoints
 * vendeur de création d'annonces.
 * Délègue la logique métier au port primaire {@link ManageListingUseCase}.</p>
 *
 * <p>L'identifiant du vendeur est résolu depuis le contexte Spring Security
 * via {@code @AuthenticationPrincipal} — jamais fourni par le client.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
@Tag(name = "Annonces", description = "Consultation et création des annonces de plats")
public class ListingController {

  private final ManageListingUseCase manageListingUseCase;
  private final ListingWebMapper listingWebMapper;
  private final FindUserUseCase findUserUseCase;

  /**
   * Retourne toutes les annonces actives de la plateforme.
   *
   * <p>Endpoint public — aucune authentification requise.</p>
   *
   * @return la liste des annonces actives
   */
  @GetMapping
  @Operation(summary = "Lister toutes les annonces actives")
  @ApiResponse(responseCode = "200", description = "Liste retournée avec succès")
  public ResponseEntity<List<ListingResponse>> findAll() {
    log.debug("Requête GET /api/listings");
    List<ListingResponse> listings = manageListingUseCase.findAll()
        .stream()
        .map(listingWebMapper::toResponse)
        .toList();
    return ResponseEntity.ok(listings);
  }

  /**
   * Retourne le détail d'une annonce par son identifiant.
   *
   * <p>Endpoint public — aucune authentification requise.
   * Retourne HTTP 404 si l'annonce est introuvable.</p>
   *
   * @param id l'identifiant de l'annonce
   * @return l'annonce correspondante
   */
  @GetMapping("/{id}")
  @Operation(summary = "Détail d'une annonce par identifiant")
  @ApiResponse(responseCode = "200", description = "Annonce trouvée")
  @ApiResponse(responseCode = "404", description = "Annonce introuvable")
  public ResponseEntity<ListingResponse> findById(@PathVariable Long id) {
    log.debug("Requête GET /api/listings/{}", id);
    Listing listing = manageListingUseCase.findById(id);
    return ResponseEntity.ok(listingWebMapper.toResponse(listing));
  }

  /**
   * Crée une nouvelle annonce pour le vendeur authentifié.
   *
   * <p>Réservé au rôle {@code ROLE_SELLER} — vérifié par Spring Security.
   * L'identifiant du vendeur est extrait du contexte d'authentification,
   * jamais fourni par le client.</p>
   *
   * @param request     le DTO de création validé
   * @param userDetails le principal authentifié injecté par Spring Security
   * @return l'annonce créée avec HTTP 201
   */
  @PostMapping
  @Operation(summary = "Créer une nouvelle annonce (vendeur authentifié)")
  @ApiResponse(responseCode = "201", description = "Annonce créée avec succès")
  @ApiResponse(responseCode = "400", description = "Données invalides")
  @ApiResponse(responseCode = "401", description = "Non authentifié")
  @ApiResponse(responseCode = "403", description = "Rôle SELLER requis")
  public ResponseEntity<ListingResponse> create(
      @Valid @RequestBody CreateListingRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {

    log.debug("Requête POST /api/listings par : {}", userDetails.getUsername());

    Long sellerId = findUserUseCase.findByEmail(userDetails.getUsername()).getId();
    ListingCommand command = listingWebMapper.toCommand(request);
    Listing created = manageListingUseCase.create(sellerId, command);

    log.info("Annonce créée (id={}) par : {}", created.getId(), userDetails.getUsername());
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(listingWebMapper.toResponse(created));
  }
}