package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.application.port.in.ManageAllergenUseCase;
import com.tortiki.api.infrastructure.adapter.in.web.dto.AllergenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptateur primaire REST pour la consultation des allergènes Tortiki.
 *
 * <p>Délègue la logique métier à {@link ManageAllergenUseCase}. Référentiel
 * réglementaire INCO EU n°1169/2011 — endpoints publics, aucune restriction
 * de rôle. La gestion (création/modification) reste réservée à
 * {@code ROLE_ADMIN} et sera ajoutée dans une itération ultérieure.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/allergens")
@RequiredArgsConstructor
@Tag(name = "Allergènes", description = "Consultation du référentiel des allergènes")
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
}