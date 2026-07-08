package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.infrastructure.adapter.in.web.dto.AdminListingResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le POJO domaine {@link Listing} et le DTO
 * {@link AdminListingResponse} de la couche REST admin.
 *
 * <p>Garantit que le domaine ne remonte jamais tel quel dans une réponse
 * HTTP. Expose volontairement {@code sellerEmail} — usage strictement
 * réservé à l'affichage administrateur (minimisation RGPD ailleurs).</p>
 */
@Component
public class AdminListingWebMapper {

  /**
   * Convertit une annonce domaine en DTO de réponse pour le panel admin.
   *
   * @param listing l'annonce à convertir
   * @return le DTO de réponse correspondant
   */
  public AdminListingResponse toResponse(final Listing listing) {
    return new AdminListingResponse(
        listing.getId(),
        listing.getTitle(),
        listing.getSeller() != null ? listing.getSeller().getEmail() : null,
        listing.getPickupAddress(),
        listing.getPrice(),
        listing.getStatus().name(),
        listing.getPhotoUrl());
  }
}