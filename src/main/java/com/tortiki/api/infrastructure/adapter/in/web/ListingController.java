package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.application.port.in.FindUserUseCase;
import com.tortiki.api.application.port.in.ManageListingUseCase;
import com.tortiki.api.config.SecurityConstants;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Adaptateur primaire REST pour la gestion des annonces Tortiki.
 *
 * <p>Délègue la logique métier à {@link ManageListingUseCase}.
 * L'identifiant vendeur est toujours résolu depuis Spring Security —
 * jamais fourni par le client.</p>
 */
@Slf4j
@RestController
@RequestMapping(SecurityConstants.ROUTE_LISTINGS)
@RequiredArgsConstructor
@Tag(name = "Annonces", description = "Consultation et gestion des annonces de plats")
public class ListingController {

  private final ManageListingUseCase manageListingUseCase;
  private final ListingWebMapper listingWebMapper;
  private final FindUserUseCase findUserUseCase;

  /**
   * Retourne toutes les annonces actives. Endpoint public.
   *
   * @return liste des annonces actives
   */
  @GetMapping
  @Operation(summary = "Lister toutes les annonces actives")
  @ApiResponse(responseCode = "200", description = "Liste retournée avec succès")
  public ResponseEntity<List<ListingResponse>> findAll() {
    List<ListingResponse> response = manageListingUseCase.findAll()
        .stream()
        .map(listingWebMapper::toResponse)
        .toList();
    return ResponseEntity.ok(response);
  }

  /**
   * Retourne une annonce par identifiant. Endpoint public.
   *
   * @param id identifiant de l'annonce
   * @return l'annonce correspondante
   */
  @GetMapping("/{id}")
  @Operation(summary = "Détail d'une annonce")
  @ApiResponse(responseCode = "200", description = "Annonce trouvée")
  @ApiResponse(responseCode = "404", description = "Annonce introuvable")
  public ResponseEntity<ListingResponse> findById(@PathVariable Long id) {
    return ResponseEntity.ok(listingWebMapper.toResponse(manageListingUseCase.findById(id)));
  }

  /**
   * Retourne les villes distinctes ayant au moins une annonce active.
   *
   * <p>Endpoint public — alimente l'autocomplétion du champ de recherche
   * par ville côté frontend.</p>
   *
   * @return liste triée des villes distinctes, vide si aucune
   */
  @GetMapping("/cities")
  @Operation(summary = "Lister les villes distinctes avec annonces actives")
  @ApiResponse(responseCode = "200", description = "Liste retournée avec succès")
  public ResponseEntity<List<String>> findDistinctActiveCities() {
    return ResponseEntity.ok(manageListingUseCase.findDistinctActiveCities());
  }

  /**
   * Crée une annonce pour le vendeur authentifié.
   *
   * @param request     DTO de création validé
   * @param userDetails principal Spring Security
   * @return l'annonce créée avec HTTP 201
   */
  @PostMapping
  @Operation(summary = "Créer une annonce (ROLE_SELLER)")
  @ApiResponse(responseCode = "201", description = "Annonce créée")
  @ApiResponse(responseCode = "400", description = "Données invalides")
  @ApiResponse(responseCode = "403", description = "Rôle SELLER requis")
  public ResponseEntity<ListingResponse> create(
      @Valid @RequestBody CreateListingRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {

    Long sellerId = findUserUseCase.findByEmail(userDetails.getUsername()).getId();
    ManageListingUseCase.Command command = listingWebMapper.toCommand(request);
    Listing created = manageListingUseCase.create(sellerId, command);

    log.info("Annonce créée id={} par {}", created.getId(), userDetails.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED).body(listingWebMapper.toResponse(created));
  }

  /**
   * Met à jour une annonce du vendeur authentifié.
   *
   * @param id          identifiant de l'annonce
   * @param request     DTO de modification validé
   * @param userDetails principal Spring Security
   * @return l'annonce mise à jour
   */
  @PutMapping("/{id}")
  @Operation(summary = "Modifier une annonce (ROLE_SELLER propriétaire)")
  @ApiResponse(responseCode = "200", description = "Annonce mise à jour")
  @ApiResponse(responseCode = "403", description = "Non propriétaire ou rôle insuffisant")
  @ApiResponse(responseCode = "404", description = "Annonce introuvable")
  public ResponseEntity<ListingResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody CreateListingRequest request,
      @AuthenticationPrincipal UserDetails userDetails) {

    Long sellerId = findUserUseCase.findByEmail(userDetails.getUsername()).getId();
    ManageListingUseCase.Command command = listingWebMapper.toCommand(request);
    Listing updated = manageListingUseCase.update(id, sellerId, command);

    return ResponseEntity.ok(listingWebMapper.toResponse(updated));
  }

  /**
   * Upload ou remplace la photo d'une annonce.
   *
   * @param id          identifiant de l'annonce
   * @param photo       fichier image multipart
   * @param userDetails principal Spring Security
   * @return l'annonce mise à jour avec la nouvelle URL photo
   */
  @PutMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Uploader la photo d'une annonce (ROLE_SELLER propriétaire)")
  @ApiResponse(responseCode = "200", description = "Photo mise à jour")
  @ApiResponse(responseCode = "400", description = "Fichier invalide")
  public ResponseEntity<ListingResponse> updatePhoto(
      @PathVariable Long id,
      @RequestPart("photo") MultipartFile photo,
      @AuthenticationPrincipal UserDetails userDetails) throws java.io.IOException {

    Long sellerId = findUserUseCase.findByEmail(userDetails.getUsername()).getId();
    ManageListingUseCase.PhotoCommand photoCommand = new ManageListingUseCase.PhotoCommand(
        photo.getBytes(),
        photo.getContentType(),
        photo.getOriginalFilename()
    );
    Listing updated = manageListingUseCase.updatePhoto(id, sellerId, photoCommand);
    return ResponseEntity.ok(listingWebMapper.toResponse(updated));
  }

  /**
   * Supprime logiquement une annonce (statut INACTIVE).
   *
   * @param id          identifiant de l'annonce
   * @param userDetails principal Spring Security
   * @return HTTP 204 sans contenu
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Supprimer une annonce (suppression logique, ROLE_SELLER)")
  @ApiResponse(responseCode = "204", description = "Annonce désactivée")
  @ApiResponse(responseCode = "403", description = "Non propriétaire")
  @ApiResponse(responseCode = "404", description = "Annonce introuvable")
  public ResponseEntity<Void> delete(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {

    Long sellerId = findUserUseCase.findByEmail(userDetails.getUsername()).getId();
    manageListingUseCase.delete(id, sellerId);
    return ResponseEntity.noContent().build();
  }
}