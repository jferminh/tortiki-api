package com.tortiki.api.infrastructure.adapter.in.web.dto;

import com.tortiki.api.domain.model.Listing;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de réponse pour une annonce dans les résultats de recherche.
 *
 * <p>Expose uniquement les données nécessaires à l'affichage
 * d'une liste d'annonces — pas les données sensibles du vendeur
 * (téléphone réservé après CONFIRMED, conformément au RGPD).</p>
 *
 * @param id             identifiant unique de l'annonce
 * @param title          titre de l'annonce
 * @param description    description courte du plat
 * @param price          prix unitaire en euros
 * @param portions       nombre de portions disponibles
 * @param pickupAddress  adresse complète de retrait
 * @param pickupDatetime date et heure du créneau de retrait
 * @param photoUrl       URL photo principale MinIO ({@code null} si absente)
 * @param cuisineType    libellé de l'origine culinaire
 * @param sellerName     prénom et nom du vendeur (affichage public)
 */
@Schema(description = "Annonce dans les résultats de recherche")
public record SearchListingResponse(

    @Schema(description = "Identifiant unique", example = "42")
    Long id,

    @Schema(description = "Titre de l'annonce", example = "Bortsch ukrainien maison")
    String title,

    @Schema(description = "Description courte du plat")
    String description,

    @Schema(description = "Prix unitaire en euros", example = "8.50")
    BigDecimal price,

    @Schema(description = "Nombre de portions disponibles", example = "4")
    Integer portions,

    @Schema(
        description = "Adresse complète de retrait",
        example = "12 rue de la Paix, 54000 Nancy"
    )
    String pickupAddress,

    @Schema(
        description = "Date et heure du créneau de retrait",
        example = "2026-06-21T14:00:00"
    )
    LocalDateTime pickupDatetime,

    @Schema(description = "URL photo principale MinIO")
    String photoUrl,

    @Schema(description = "Origine culinaire", example = "Ukrainienne")
    String cuisineType,

    @Schema(description = "Prénom et nom du vendeur", example = "Sofia Kovalenko")
    String sellerName

) {

  /**
   * Construit un DTO de recherche depuis un objet domaine {@link Listing}.
   *
   * <p>Méthode factory statique — évite d'exposer le domaine
   * dans les couches supérieures.</p>
   *
   * @param listing annonce domaine à mapper
   * @return DTO de réponse correspondante
   */
  public static SearchListingResponse from(Listing listing) {
    return new SearchListingResponse(
        listing.getId(),
        listing.getTitle(),
        listing.getDescription(),
        listing.getPrice(),
        listing.getPortions(),
        listing.getPickupAddress(),
        listing.getPickupDatetime(),
        listing.getPhotoUrl(),
        listing.getCuisineType() != null
            ? listing.getCuisineType().getName()
            : null,
        listing.getSeller() != null
            ? listing.getSeller().getFirstName()
              + " " + listing.getSeller().getLastName()
            : null
    );
  }
}