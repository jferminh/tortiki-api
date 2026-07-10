package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.application.port.in.FindReviewsUseCase;
import com.tortiki.api.application.port.in.SubmitReviewUseCase;
import com.tortiki.api.config.SecurityConstants;
import com.tortiki.api.domain.model.Review;
import com.tortiki.api.infrastructure.adapter.in.web.dto.ReviewResponse;
import com.tortiki.api.infrastructure.adapter.in.web.dto.SubmitReviewRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour la soumission des évaluations.
 *
 * <p>Réservé aux acheteurs ayant une demande de contact {@code CONFIRMED}.</p>
 */
@Slf4j
@RestController
@RequestMapping(SecurityConstants.ROUTE_REVIEWS)
@RequiredArgsConstructor
@Tag(name = "Évaluations", description = "Soumission d'une évaluation par un acheteur")
public class ReviewController {

  private final SubmitReviewUseCase submitReviewUseCase;
  private final FindReviewsUseCase findReviewsUseCase;

  /**
   * Soumet une évaluation pour une annonce.
   *
   * @param request   DTO contenant listingId, rating et comment
   * @param principal principal Spring Security — email de l'acheteur connecté
   * @return évaluation créée avec statut 201
   */
  @PostMapping
  @PreAuthorize("hasRole('BUYER')")
  @Operation(
      summary = "Soumettre une évaluation",
      description = "Un acheteur avec une demande CONFIRMED peut noter une annonce de 1 à 5."
  )
  @ApiResponse(responseCode = "201", description = "Évaluation créée avec succès")
  @ApiResponse(responseCode = "400", description = "Données invalides")
  @ApiResponse(responseCode = "401", description = "Non authentifié")
  @ApiResponse(responseCode = "403",
      description = "Accès réservé aux acheteurs ou demande non confirmée")
  @ApiResponse(responseCode = "409", description = "Évaluation déjà soumise")
  public ResponseEntity<ReviewResponse> submitReview(
      @Valid @RequestBody final SubmitReviewRequest request,
      final Principal principal) {

    final String reviewerEmail = principal.getName();
    log.debug("POST /api/v1/reviews — annonce={} acheteur={}",
        request.listingId(), reviewerEmail);  // ← debug, pas info

    final SubmitReviewUseCase.Command command = new SubmitReviewUseCase.Command(
        request.listingId(),
        reviewerEmail,
        request.rating(),
        request.comment()
    );

    final Review review = submitReviewUseCase.submit(command);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ReviewResponse.from(review));
  }

  /**
   * Retourne toutes les évaluations d'une annonce.
   *
   * <p>Endpoint public — aucune authentification requise. Alimente
   * la section avis de la fiche plat consultée par tout visiteur.</p>
   *
   * @param listingId identifiant de l'annonce évaluée
   * @return liste des évaluations, vide si aucune n'existe encore
   */
  @GetMapping
  @Operation(
      summary = "Lister les évaluations d'une annonce",
      description = "Endpoint public — retourne les avis triés par date décroissante."
  )
  @ApiResponse(responseCode = "200", description = "Liste retournée avec succès")
  public ResponseEntity<List<ReviewResponse>> findByListingId(
      @RequestParam final Long listingId) {

    log.debug("GET /api/v1/reviews?listingId={}", listingId);

    final List<ReviewResponse> response = findReviewsUseCase.findByListingId(listingId)
        .stream()
        .map(ReviewResponse::from)
        .toList();

    return ResponseEntity.ok(response);
  }
}