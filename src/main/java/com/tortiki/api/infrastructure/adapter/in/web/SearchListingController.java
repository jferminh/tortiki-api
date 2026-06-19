package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.application.port.in.SearchCriteria;
import com.tortiki.api.application.port.in.SearchListingsUseCase;
import com.tortiki.api.config.SecurityConstants;
import com.tortiki.api.infrastructure.adapter.in.web.dto.ErrorResponse;
import com.tortiki.api.infrastructure.adapter.in.web.dto.SearchListingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST de recherche d'annonces Tortiki.
 *
 * <p>Point d'entrée HTTP pour la recherche filtrée d'annonces actives.
 * Traduit les paramètres de requête HTTP en {@link SearchCriteria}
 * et délègue au port primaire {@link SearchListingsUseCase}.</p>
 *
 * <p>Accessible publiquement — aucune authentification requise
 * pour consulter les annonces (configuré dans {@code SecurityConfig}).</p>
 */
@Slf4j
@RestController
@RequestMapping(SecurityConstants.ROUTE_LISTINGS_SEARCH)
@Tag(name = "Recherche", description = "Recherche d'annonces de plats cuisinés")
public class SearchListingController {

  /** Port primaire de recherche d'annonces. */
  private final SearchListingsUseCase searchListingsUseCase;

  /**
   * Construit le contrôleur avec le port primaire de recherche.
   *
   * @param searchListingsUseCase port primaire de recherche
   */
  public SearchListingController(SearchListingsUseCase searchListingsUseCase) {
    this.searchListingsUseCase = searchListingsUseCase;
  }

  /**
   * Recherche les annonces actives selon les critères fournis.
   *
   * <p>Tous les paramètres sont optionnels. Sans paramètre,
   * retourne toutes les annonces actives paginées.</p>
   *
   * @param query         mot-clé libre sur titre ou description
   * @param city          ville de retrait (géocodée par Nominatim)
   * @param cuisineTypeId filtre par origine culinaire
   * @param maxPrice      prix maximum en euros
   * @param radiusKm      rayon de recherche en km (défaut 10)
   * @param page          numéro de page (défaut 0)
   * @param size          taille de page (défaut 10, max 50)
   * @return liste d'annonces correspondantes
   */
  @GetMapping
  @Operation(
      summary = "Rechercher des annonces",
      description = "Recherche paginée d'annonces actives avec filtres optionnels.")
  @ApiResponse(responseCode = "200", description = "Résultats de recherche",
      content = @Content(schema = @Schema(implementation = SearchListingResponse.class)))
  @ApiResponse(responseCode = "400", description = "Paramètres invalides",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public ResponseEntity<List<SearchListingResponse>> search(
      @Parameter(description = "Mot-clé libre")
      @RequestParam(required = false) String query,

      @Parameter(description = "Ville de retrait")
      @RequestParam(required = false) String city,

      @Parameter(description = "Identifiant de l'origine culinaire")
      @RequestParam(required = false) Long cuisineTypeId,

      @Parameter(description = "Prix maximum en euros")
      @RequestParam(required = false) BigDecimal maxPrice,

      @Parameter(description = "Rayon de recherche en km (défaut : 10)")
      @RequestParam(required = false, defaultValue = "10.0") Double radiusKm,

      @Parameter(description = "Numéro de page (défaut : 0)")
      @RequestParam(required = false, defaultValue = "0") int page,

      @Parameter(description = "Taille de page (défaut : 10, max : 50)")
      @RequestParam(required = false, defaultValue = "10") int size) {

    SearchCriteria criteria = new SearchCriteria(
        query, city, cuisineTypeId, null, maxPrice,
        null, null, radiusKm, page, size);

    log.debug("Recherche HTTP — city='{}' query='{}' page={}", city, query, page);

    List<SearchListingResponse> results = searchListingsUseCase.search(criteria)
        .stream()
        .map(SearchListingResponse::from)
        .toList();

    return ResponseEntity.ok(results);
  }
}