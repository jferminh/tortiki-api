package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.application.port.in.ManageListingUseCase;
import com.tortiki.api.domain.model.Allergen;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.infrastructure.adapter.in.web.dto.CreateListingRequest;
import com.tortiki.api.infrastructure.adapter.in.web.dto.ListingResponse;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le modèle domaine {@link Listing} et les DTOs REST.
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
    List<String> allergenNames = listing.getAllergens()
        .stream()
        .map(Allergen::getName)
        .toList();

    return new ListingResponse(
        listing.getId(),
        listing.getTitle(),
        listing.getDescription(),
        listing.getPrice(),
        listing.getPortions(),
        listing.getPickupAddress(),
        listing.getPickupDatetime(),
        listing.getPhotoUrl(),
        listing.getStatus(),
        listing.getCuisineType() != null ? listing.getCuisineType().getName() : null,
        listing.getSeller() != null ? listing.getSeller().getEmail() : null,
        allergenNames,
        listing.getCreatedAt()
    );
  }

  /**
   * Convertit un {@link CreateListingRequest} en {@link ManageListingUseCase.Command}.
   *
   * <p>Le {@code sellerId} est résolu par le contrôleur depuis Spring Security —
   * il n'appartient pas au DTO d'entrée.</p>
   *
   * @param request le DTO d'entrée validé
   * @return la commande domaine
   */
  public ManageListingUseCase.Command toCommand(CreateListingRequest request) {
    return new ManageListingUseCase.Command(
        request.title(),
        request.description(),
        request.price(),
        request.portions(),
        request.pickupAddress(),
        request.pickupDatetime(),
        request.cuisineTypeId(),
        request.allergenIds()
    );
  }
}