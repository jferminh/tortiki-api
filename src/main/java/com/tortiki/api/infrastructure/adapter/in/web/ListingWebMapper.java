package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.application.port.in.ListingCommand;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.infrastructure.adapter.in.web.dto.CreateListingRequest;
import com.tortiki.api.infrastructure.adapter.in.web.dto.ListingResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le modèle domaine {@link Listing} et les DTOs
 * de la couche REST {@code adapter/in/web/}.
 *
 * <p>Garantit que les DTOs HTTP ne remontent jamais dans les couches
 * {@code application} ou {@code domain}.</p>
 */
@Component
public class ListingWebMapper {

  /**
   * Convertit un {@link Listing} domaine en {@link ListingResponse} HTTP.
   *
   * @param listing l'annonce domaine à convertir
   * @return le DTO de réponse HTTP
   */
  public ListingResponse toResponse(Listing listing) {
    return new ListingResponse(
        listing.getId(),
        listing.getTitle(),
        listing.getDescription(),
        listing.getPrice(),
        listing.getPortions(),
        listing.getPickupSlot(),
        listing.getCity(),
        listing.getPostalCode(),
        listing.getPhotoUrl(),
        listing.getStatus(),
        listing.getCuisineType() != null ? listing.getCuisineType().getName() : null,
        listing.getSeller() != null ? listing.getSeller().getEmail() : null,
        listing.getCreatedAt()
    );
  }

  /**
   * Convertit un {@link CreateListingRequest} en {@link ListingCommand} domaine.
   *
   * <p>Le {@code sellerId} est résolu par le contrôleur depuis le contexte
   * Spring Security — il n'appartient pas au DTO d'entrée.</p>
   *
   * @param request le DTO d'entrée validé
   * @return la commande domaine
   */
  public ListingCommand toCommand(CreateListingRequest request) {
    return new ListingCommand(
        request.title(),
        request.description(),
        request.price(),
        request.portions(),
        request.pickupSlot(),
        request.city(),
        request.postalCode(),
        request.cuisineTypeId()
    );
  }
}