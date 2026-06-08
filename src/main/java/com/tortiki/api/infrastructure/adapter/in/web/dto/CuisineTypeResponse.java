package com.tortiki.api.infrastructure.adapter.in.web.dto;

/**
 * DTO de réponse HTTP pour une origine culinaire.
 *
 * <p>Record immuable — ne remonte jamais dans les couches
 * {@code application} ou {@code domain}.</p>
 *
 * @param id          identifiant technique
 * @param name        nom de l'origine culinaire
 * @param description description optionnelle
 */
public record CuisineTypeResponse(
    Long id,
    String name,
    String description
) {
}