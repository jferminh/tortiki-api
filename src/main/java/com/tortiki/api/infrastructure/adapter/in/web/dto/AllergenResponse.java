package com.tortiki.api.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO de sortie représentant un allergène du référentiel réglementaire.
 *
 * <p>Record immuable Java 21 exposé par endpoint public
 * {@code GET /api/v1/allergens}. Ne contient aucun objet domaine
 * ni entité JPA.</p>
 *
 * <p>Référentiel conforme au règlement INCO EU n°1169/2011 —
 * consultation publique, gestion réservée à {@code ROLE_ADMIN}.</p>
 *
 * @param id identifiant technique de l'allergène
 * @param name nom de l'allergène (ex. "Gluten", "Lait")
 */
@Schema(description = "Représentation d'un allergène réglementaire")
public record AllergenResponse(

    @Schema(description = "Identifiant technique", example = "3")
    Long id,

    @Schema(description = "Nom de l'allergène", example = "Gluten")
    String name

) {}