package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.application.port.in.ManageAllergenUseCase;
import com.tortiki.api.config.SecurityConstants;
import com.tortiki.api.infrastructure.adapter.in.web.dto.AllergenResponse;
import com.tortiki.api.infrastructure.adapter.in.web.dto.CreateAllergenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptateur primaire REST pour la gestion des allergènes Tortiki.
 *
 * <p>Délègue la logique métier à {@link ManageAllergenUseCase}. Référentiel
 * réglementaire INCO EU n°1169/2011 — la consultation reste publique,
 * la création et la désactivation sont réservées à {@code ROLE_ADMIN}.</p>
 */
@Slf4j
@RestController
@RequestMapping(SecurityConstants.ROUTE_ALLERGENS)
@RequiredArgsConstructor
@Tag(name = "Allergènes", description = "Gestion du référentiel des allergènes")
public class AllergenController {

  private final ManageAllergenUseCase manageAllergenUseCase;
  private final AllergenWebMapper allergenWebMapper;

  /**
   * Retourne tous les allergènes du référentiel. Endpoint public.
   *
   * @return liste complète des allergènes
   */
  @GetMapping
  @Operation(summary = "Lister tous les allergènes")
  @ApiResponse(responseCode = "200", description = "Liste retournée avec succès")
  public ResponseEntity<List<AllergenResponse>> findAll() {
    log.debug("Requête GET /api/v1/allergens");
    List<AllergenResponse> response = manageAllergenUseCase.findAll()
        .stream()
        .map(allergenWebMapper::toResponse)
        .toList();
    return ResponseEntity.ok(response);
  }

  /**
   * Retourne un allergène par identifiant. Endpoint public.
   *
   * @param id identifiant de l'allergène
   * @return l'allergène correspondant
   */
  @GetMapping("/{id}")
  @Operation(summary = "Détail d'un allergène")
  @ApiResponse(responseCode = "200", description = "Allergène trouvé")
  @ApiResponse(responseCode = "404", description = "Allergène introuvable")
  public ResponseEntity<AllergenResponse> findById(@PathVariable Long id) {
    log.debug("Requête GET /api/v1/allergens/{}", id);
    return ResponseEntity.ok(allergenWebMapper.toResponse(manageAllergenUseCase.findById(id)));
  }

  /**
   * Crée un nouvel allergène dans le référentiel. Réservé à {@code ROLE_ADMIN}.
   *
   * @param request corps de la requête JSON validé par Bean Validation
   * @return l'allergène créé avec statut HTTP {@code 201 Created}
   */
  @PostMapping
  @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
  @Operation(
      summary = "Créer un allergène",
      description = "Réservé à ROLE_ADMIN. L'allergène est créé activé par défaut.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponse(responseCode = "201", description = "Allergène créé avec succès")
  @ApiResponse(responseCode = "400", description = "Données invalides")
  @ApiResponse(responseCode = "401", description = "Authentification requise")
  @ApiResponse(responseCode = "403", description = "Rôle ROLE_ADMIN requis")
  public ResponseEntity<AllergenResponse> create(
      @Valid @RequestBody CreateAllergenRequest request) {
    log.debug("Requête POST /api/v1/allergens nom={}", request.name());
    var created = manageAllergenUseCase.create(request.name());
    return ResponseEntity.status(HttpStatus.CREATED).body(allergenWebMapper.toResponse(created));
  }

  /**
   * Désactive un allergène du référentiel. Réservé à {@code ROLE_ADMIN}.
   *
   * <p>Ne supprime jamais physiquement la ligne — bascule
   * {@code enabled} à {@code false} pour préserver l'intégrité
   * référentielle de {@code listing_allergens}.</p>
   *
   * @param id identifiant de l'allergène à désactiver
   * @return {@code 204 No Content}
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('" + SecurityConstants.ROLE_ADMIN + "')")
  @Operation(
      summary = "Désactiver un allergène",
      description = "Réservé à ROLE_ADMIN. Désactivation logique, jamais de suppression physique.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponse(responseCode = "204", description = "Allergène désactivé avec succès")
  @ApiResponse(responseCode = "401", description = "Authentification requise")
  @ApiResponse(responseCode = "403", description = "Rôle ROLE_ADMIN requis")
  @ApiResponse(responseCode = "404", description = "Allergène introuvable")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    log.debug("Requête DELETE /api/v1/allergens/{}", id);
    manageAllergenUseCase.delete(id);
    return ResponseEntity.noContent().build();
  }
}