package com.tortiki.api.infrastructure.adapter.in.web.dto;

import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
import java.time.LocalDateTime;

/**
 * DTO de réponse représentant une demande de contact dans le tableau de bord vendeur.
 *
 * <p>Record immuable Java 21 — expose uniquement les champs nécessaires
 * à l'affichage côté vendeur. Le {@code buyerId} n'est pas exposé (RGPD).</p>
 *
 * @param id             identifiant de la demande
 * @param listingId      identifiant de l'annonce concernée
 * @param listingTitle   titre de l'annonce (confort affichage)
 * @param buyerFirstName prénom de l'acheteur
 * @param message        message laissé par l'acheteur
 * @param portions       nombre de portions demandées
 * @param status         statut actuel de la demande
 * @param createdAt      date de soumission
 */
public record ContactRequestSummaryResponse(
    Long id,
    Long listingId,
    String listingTitle,
    String buyerFirstName,
    String message,
    Integer portions,
    ContactRequestStatus status,
    LocalDateTime createdAt
) {

  /**
   * Construit le DTO depuis le POJO domaine {@link ContactRequest}.
   *
   * @param cr demande de contact domaine
   * @return DTO prêt à sérialiser
   */
  public static ContactRequestSummaryResponse from(final ContactRequest cr) {
    return new ContactRequestSummaryResponse(
        cr.getId(),
        cr.getListing() != null ? cr.getListing().getId() : null,
        cr.getListing() != null ? cr.getListing().getTitle() : null,
        cr.getBuyer() != null ? cr.getBuyer().getFirstName() : null,
        cr.getMessage(),
        cr.getPortions(),
        cr.getStatus(),
        cr.getCreatedAt()
    );
  }
}