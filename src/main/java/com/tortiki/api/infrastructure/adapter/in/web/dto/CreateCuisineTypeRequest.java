package com.tortiki.api.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO d'entrée pour la création d'une origine culinaire.
 *
 * <p>Réservé au rôle {@code ROLE_ADMIN}.
 * Record immuable validé par {@code @Valid} dans
 * {@link com.tortiki.api.infrastructure.adapter.in.web.CuisineTypeController}.</p>
 *
 * @param name        nom unique de l'origine culinaire
 * @param description description optionnelle (max 255 caractères)
 */
public record CreateCuisineTypeRequest(
    @NotBlank(message = "Le nom de l'origine culinaire est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    String name,

    @Size(max = 255, message = "La description ne peut pas dépasser 255 caractères")
    String description
) {
}