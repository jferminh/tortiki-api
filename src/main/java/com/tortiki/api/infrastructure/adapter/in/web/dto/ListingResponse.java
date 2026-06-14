package com.tortiki.api.infrastructure.adapter.in.web.dto;

import com.tortiki.api.domain.model.ListingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de réponse représentant une annonce de plat Tortiki.
 *
 * <p>Record immuable Java 21 — exposé par les endpoints REST publics
 * et vendeur. Ne contient aucun objet domaine ni entité JPA.</p>
 *
 * <p>Données RGPD : seul l'email du vendeur est exposé en v1.
 * Les coordonnées téléphoniques ne sont transmises qu'après
 * {@code CONFIRMED} (Sprint 3).</p>
 *
 * @param id              identifiant technique de l'annonce
 * @param title           titre de l'annonce
 * @param description     description détaillée du plat
 * @param price           prix unitaire en euros
 * @param portions        nombre de portions disponibles
 * @param pickupAddress   adresse de retrait
 * @param pickupDatetime  date et heure du créneau de retrait
 * @param photoUrl        URL publique de la photo MinIO ({@code null} si absente)
 * @param status          statut de l'annonce
 * @param cuisineTypeName nom de l'origine culinaire
 * @param sellerEmail     email du vendeur (identifiant public)
 * @param allergenNames   noms des allergènes présents (liste vide si aucun)
 * @param createdAt       date de création
 */
@Schema(description = "Représentation d'une annonce de plat")
public record ListingResponse(

    @Schema(description = "Identifiant technique", example = "42")
    Long id,

    @Schema(description = "Titre de l'annonce", example = "Bortsch ukrainien maison")
    String title,

    @Schema(description = "Description détaillée du plat")
    String description,

    @Schema(description = "Prix unitaire en euros", example = "8.50")
    BigDecimal price,

    @Schema(description = "Nombre de portions disponibles", example = "4")
    Integer portions,

    @Schema(
        description = "Adresse de retrait",
        example = "12 rue de la Paix, 54000 Nancy"
    )
    String pickupAddress,

    @Schema(
        description = "Date et heure du créneau de retrait",
        example = "2026-06-21T14:00:00"
    )
    LocalDateTime pickupDatetime,

    @Schema(
        description = "URL publique de la photo (null si aucune photo)",
        example = "http://localhost:9000/tortiki-photos/uuid-listing-42.jpg"
    )
    String photoUrl,

    @Schema(description = "Statut de l'annonce", example = "ACTIVE")
    ListingStatus status,

    @Schema(description = "Origine culinaire", example = "Ukrainienne")
    String cuisineTypeName,

    @Schema(description = "Email du vendeur", example = "sofia@example.com")
    String sellerEmail,

    @Schema(
        description = "Allergènes présents (règlement INCO EU n°1169/2011)",
        example = "[\"Gluten\", \"Lait\"]"
    )
    List<String> allergenNames,

    @Schema(description = "Date de création", example = "2026-06-12T10:00:00")
    LocalDateTime createdAt

) {}