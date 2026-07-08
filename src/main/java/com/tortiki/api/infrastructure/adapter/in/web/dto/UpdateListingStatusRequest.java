package com.tortiki.api.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO de requête pour le changement de statut d'une annonce par un
 * administrateur.
 *
 * <p>{@code @Pattern} restreint {@code newStatus} aux valeurs exactes de
 * l'énumération {@code ListingStatus} — garantit qu'aucune valeur invalide
 * n'atteint la couche {@code application/service}, qui appelle
 * {@code ListingStatus.valueOf(newStatus)} sans validation supplémentaire.</p>
 *
 * @param newStatus nouveau statut souhaité : {@code ACTIVE}, {@code INACTIVE}
 *                  ou {@code DELETED}
 */
public record UpdateListingStatusRequest(
    @NotBlank(message = "Le statut est obligatoire")
    @Pattern(
        regexp = "ACTIVE|INACTIVE|DELETED",
        message = "Le statut doit être ACTIVE, INACTIVE ou DELETED")
    String newStatus
) {
}