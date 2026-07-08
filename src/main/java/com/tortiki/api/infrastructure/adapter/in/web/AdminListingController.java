package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.application.port.in.ManageAdminListingsUseCase;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.infrastructure.adapter.in.web.dto.AdminListingResponse;
import com.tortiki.api.infrastructure.adapter.in.web.dto.UpdateListingStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * Adaptateur primaire REST pour la modération des annonces par un
 * administrateur.
 *
 * <p>Toutes les routes de ce contrôleur sont réservées au rôle
 * {@code ROLE_ADMIN}, vérifié via {@code @PreAuthorize}. Dlgue la logique
 * mtier au port primaire {@link ManageAdminListingsUseCase}.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/listings")
@RequiredArgsConstructor
@Tag(name = "Administration - Annonces",
    description = "Modération des annonces réservée aux administrateurs")
public class AdminListingController {

  private final ManageAdminListingsUseCase manageAdminListingsUseCase;
  private final AdminListingWebMapper adminListingWebMapper;

  /**
   * Retourne toutes les annonces de la plateforme, tous vendeurs et tous
   * statuts confondus.
   *
   * @return la liste complète des annonces au format admin
   */
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Lister toutes les annonces (modération admin)")
  @ApiResponse(responseCode = "200", description = "Liste retournée avec succès")
  @ApiResponse(responseCode = "403", description = "Rôle ADMIN requis")
  public ResponseEntity<List<AdminListingResponse>> findAll() {
    log.debug("Requête GET /api/v1/admin/listings");
    List<AdminListingResponse> responses = manageAdminListingsUseCase.findAll()
        .stream()
        .map(adminListingWebMapper::toResponse)
        .toList();
    return ResponseEntity.ok(responses);
  }

  /**
   * Modifie le statut d'une annonce (activation, désactivation, suppression
   * logique).
   *
   * @param id      identifiant de l'annonce à modifier
   * @param request DTO contenant le nouveau statut, validé
   * @return l'annonce avec son statut mis à jour
   */
  @PatchMapping("/{id}/status")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Modifier le statut d'une annonce (modération admin)")
  @ApiResponse(responseCode = "200", description = "Statut mis à jour")
  @ApiResponse(responseCode = "400", description = "Statut invalide")
  @ApiResponse(responseCode = "403", description = "Rôle ADMIN requis")
  @ApiResponse(responseCode = "404", description = "Annonce introuvable")
  public ResponseEntity<AdminListingResponse> updateStatus(
      @PathVariable final Long id,
      @Valid @RequestBody final UpdateListingStatusRequest request) {
    log.debug("Requête PATCH /api/v1/admin/listings/{}/status -> {}", id, request.newStatus());
    Listing updated = manageAdminListingsUseCase.updateStatus(id, request.newStatus());
    return ResponseEntity.ok(adminListingWebMapper.toResponse(updated));
  }
}