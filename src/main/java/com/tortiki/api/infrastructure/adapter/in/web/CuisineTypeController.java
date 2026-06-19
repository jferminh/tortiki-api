package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.application.port.in.ManageCuisineTypeUseCase;
import com.tortiki.api.config.SecurityConstants;
import com.tortiki.api.domain.model.CuisineType;
import com.tortiki.api.infrastructure.adapter.in.web.dto.CreateCuisineTypeRequest;
import com.tortiki.api.infrastructure.adapter.in.web.dto.CuisineTypeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptateur primaire REST pour le référentiel des origines culinaires Tortiki.
 *
 * <p>Expose la consultation publique et la gestion admin des origines culinaires.
 * La création est réservée au rôle {@code ROLE_ADMIN} via {@code @PreAuthorize}.
 * Délègue la logique métier au port primaire {@link ManageCuisineTypeUseCase}.</p>
 */
@Slf4j
@RestController
@RequestMapping(SecurityConstants.ROUTE_CUISINE_TYPES)
@RequiredArgsConstructor
@Tag(name = "Origines culinaires", description = "Référentiel des origines culinaires")
public class CuisineTypeController {

  private final ManageCuisineTypeUseCase manageCuisineTypeUseCase;
  private final CuisineTypeWebMapper cuisineTypeWebMapper;

  /**
   * Retourne toutes les origines culinaires disponibles.
   *
   * <p>Endpoint public — aucune authentification requise.</p>
   *
   * @return la liste complète des origines culinaires
   */
  @GetMapping
  @Operation(summary = "Lister toutes les origines culinaires")
  @ApiResponse(responseCode = "200", description = "Liste retournée avec succès")
  public ResponseEntity<List<CuisineTypeResponse>> findAll() {
    log.debug("Requête GET /api/cuisine-types");
    List<CuisineTypeResponse> types = manageCuisineTypeUseCase.findAll()
        .stream()
        .map(cuisineTypeWebMapper::toResponse)
        .toList();
    return ResponseEntity.ok(types);
  }

  /**
   * Retourne le détail d'une origine culinaire par son identifiant.
   *
   * <p>Endpoint public. Retourne HTTP 404 si l'origine est introuvable.</p>
   *
   * @param id l'identifiant de l'origine culinaire
   * @return l'origine culinaire correspondante
   */
  @GetMapping("/{id}")
  @Operation(summary = "Détail d'une origine culinaire par identifiant")
  @ApiResponse(responseCode = "200", description = "Origine culinaire trouvée")
  @ApiResponse(responseCode = "404", description = "Origine culinaire introuvable")
  public ResponseEntity<CuisineTypeResponse> findById(@PathVariable Long id) {
    log.debug("Requête GET /api/cuisine-types/{}", id);
    CuisineType cuisineType = manageCuisineTypeUseCase.findById(id);
    return ResponseEntity.ok(cuisineTypeWebMapper.toResponse(cuisineType));
  }

  /**
   * Crée une nouvelle origine culinaire dans le référentiel.
   *
   * <p>Réservé au rôle {@code ROLE_ADMIN} — vérifié par {@code @PreAuthorize}.
   * Retourne HTTP 201 avec l'origine créée.</p>
   *
   * @param request le DTO de création validé
   * @return l'origine culinaire créée avec HTTP 201
   */
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Créer une origine culinaire (admin uniquement)")
  @ApiResponse(responseCode = "201", description = "Origine culinaire créée")
  @ApiResponse(responseCode = "400", description = "Données invalides")
  @ApiResponse(responseCode = "403", description = "Rôle ADMIN requis")
  public ResponseEntity<CuisineTypeResponse> create(
      @Valid @RequestBody CreateCuisineTypeRequest request) {

    log.debug("Requête POST /api/cuisine-types : {}", request.name());
    CuisineType created = manageCuisineTypeUseCase.create(
        request.name(), request.description()
    );
    log.info("Origine culinaire créée : id={} nom={}", created.getId(), created.getName());
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(cuisineTypeWebMapper.toResponse(created));
  }
}