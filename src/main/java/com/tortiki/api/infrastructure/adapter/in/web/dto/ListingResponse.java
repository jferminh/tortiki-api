package com.tortiki.api.infrastructure.adapter.in.web.dto;

import com.tortiki.api.domain.model.ListingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de réponse HTTP pour une annonce de plat.
 *
 * <p>Record immuable — ne remonte jamais dans les couches
 * {@code application} ou {@code domain}.</p>
 *
 * @param id          identifiant technique de l'annonce
 * @param title       titre du plat
 * @param description description détaillée
 * @param price       prix unitaire en euros
 * @param portions    nombre de portions disponibles
 * @param pickupSlot  créneau de retrait
 * @param city        ville de retrait
 * @param postalCode  code postal de retrait
 * @param photoUrl    URL MinIO de la photo (null si non uploadée)
 * @param status      statut de l'annonce
 * @param cuisineTypeName nom de l'origine culinaire
 * @param sellerEmail email du vendeur
 * @param createdAt   date de création
 */
public record ListingResponse(
    Long id,
    String title,
    String description,
    BigDecimal price,
    Integer portions,
    String pickupSlot,
    String city,
    String postalCode,
    String photoUrl,
    ListingStatus status,
    String cuisineTypeName,
    String sellerEmail,
    LocalDateTime createdAt
) {
}