package com.tortiki.api.infrastructure.adapter.in.web.dto;

import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
import java.time.LocalDateTime;

/**
 * DTO de réponse pour une demande de contact créée.
 *
 * <p>Expose uniquement les données nécessaires à la confirmation
 * côté acheteur — aucune donnée sensible du vendeur.</p>
 *
 * @param id        identifiant de la demande créée
 * @param listingId identifiant de l'annonce concernée
 * @param status    statut initial {@code PENDING}
 * @param message   message transmis au vendeur
 * @param portions  nombre de portions demandées
 * @param createdAt date et heure de création
 */
public record ContactRequestResponse(
    Long id,
    Long listingId,
    ContactRequestStatus status,
    String message,
    Integer portions,
    LocalDateTime createdAt
) {

  /**
   * Construit le DTO depuis un objet domaine {@link ContactRequest}.
   *
   * @param contactRequest demande de contact domaine
   * @return DTO de réponse correspondante
   */
  public static ContactRequestResponse from(ContactRequest contactRequest) {
    return new ContactRequestResponse(
        contactRequest.getId(),
        contactRequest.getListing().getId(),
        contactRequest.getStatus(),
        contactRequest.getMessage(),
        contactRequest.getPortions(),
        contactRequest.getCreatedAt()
    );
  }
}