package com.tortiki.api.infrastructure.adapter.in.web.dto;

import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
import java.time.LocalDateTime;

/**
 * DTO de réponse représentant une demande de contact dans l'historique acheteur.
 *
 * <p>Record immuable Java 21 — expose uniquement les champs nécessaires
 * à l'affichage côté acheteur (Théo). Le {@code sellerId} n'est pas exposé
 * directement, seul le prénom est transmis (RGPD, symétrique au traitement
 * appliqué sur {@code ContactRequestSummaryResponse} côté vendeur).</p>
 *
 * @param id              identifiant de la demande
 * @param listingId       identifiant de l'annonce concernée
 * @param listingTitle    titre de l'annonce (confort affichage)
 * @param listingPhotoUrl URL de la photo de l'annonce, {@code null} si absente
 * @param sellerFirstName prénom du vendeur contacté
 * @param message         message laissé par l'acheteur
 * @param portions        nombre de portions demandées
 * @param status          statut actuel de la demande
 * @param createdAt       date de soumission
 */
public record ContactRequestBuyerSummaryResponse(
    Long id,
    Long listingId,
    String listingTitle,
    String listingPhotoUrl,
    String sellerFirstName,
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
  public static ContactRequestBuyerSummaryResponse from(final ContactRequest cr) {
    return new ContactRequestBuyerSummaryResponse(
        cr.getId(),
        cr.getListing() != null ? cr.getListing().getId() : null,
        cr.getListing() != null ? cr.getListing().getTitle() : null,
        cr.getListing() != null ? cr.getListing().getPhotoUrl() : null,
        cr.getListing() != null && cr.getListing().getSeller() != null
            ? cr.getListing().getSeller().getFirstName()
            : null,
        cr.getMessage(),
        cr.getPortions(),
        cr.getStatus(),
        cr.getCreatedAt()
    );
  }
}