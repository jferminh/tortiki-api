package com.tortiki.api.infrastructure.adapter.in.web.dto;

import com.tortiki.api.domain.model.Listing;
import java.math.BigDecimal;

/**
 * DTO de réponse pour une annonce dans les résultats de recherche.
 *
 * <p>Expose uniquement les données nécessaires à l'affichage
 * d'une liste d'annonces — pas les données sensibles du vendeur.</p>
 *
 * @param id            identifiant unique de l'annonce
 * @param title         titre de l'annonce
 * @param description   description courte
 * @param price         prix en euros
 * @param city          ville de retrait
 * @param postalCode    code postal
 * @param portions      nombre de portions disponibles
 * @param photoUrl      URL photo principale (MinIO)
 * @param cuisineType   libellé de l'origine culinaire
 * @param sellerName    prénom + nom du vendeur
 */
public record SearchListingResponse(
    Long id,
    String title,
    String description,
    BigDecimal price,
    String city,
    String postalCode,
    Integer portions,
    String photoUrl,
    String cuisineType,
    String sellerName
) {

  /**
   * Construit un DTO de réponse depuis un objet domaine {@link Listing}.
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
        listing.getCity(),
        listing.getPostalCode(),
        listing.getPortions(),
        listing.getPhotoUrl(),
        listing.getCuisineType() != null ? listing.getCuisineType().getName() : null,
        listing.getSeller() != null
            ? listing.getSeller().getFirstName() + " " + listing.getSeller().getLastName()
            : null);
  }
}