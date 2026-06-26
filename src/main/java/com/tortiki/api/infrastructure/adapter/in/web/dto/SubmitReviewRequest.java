package com.tortiki.api.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de requête pour la soumission d'une évaluation.
 *
 * @param listingId identifiant de l'annonce évaluée
 * @param rating    note de un à cinq
 * @param comment   commentaire libre (optionnel)
 */
public record SubmitReviewRequest(
    @NotNull(message = "L'identifiant de l'annonce est obligatoire")
    Long listingId,
    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note minimale est 1")
    @Max(value = 5, message = "La note maximale est 5")
    Integer rating,
    String comment
) {}