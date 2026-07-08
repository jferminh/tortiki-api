package com.tortiki.api.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

/**
 * DTO de réponse représentant une annonce dans le panel d'administration.
 *
 * <p>Expose {@code sellerEmail} contrairement aux DTOs publics
 * ({@code ListingResponse}) — usage strictement réservé à l'affichage
 * administrateur, jamais exposé aux endpoints publics ou vendeur.</p>
 *
 * @param id           identifiant de l'annonce
 * @param title        titre du plat
 * @param sellerEmail  email du vendeur, usage interne admin uniquement
 * @param pickupAddress adresse de retrait
 * @param price        prix unitaire
 * @param status       statut actuel de l'annonce
 * @param photoUrl     URL de la photo, peut être {@code null}
 */
public record AdminListingResponse(
    Long id,
    String title,
    String sellerEmail,
    String pickupAddress,
    BigDecimal price,
    String status,
    String photoUrl
) {
}