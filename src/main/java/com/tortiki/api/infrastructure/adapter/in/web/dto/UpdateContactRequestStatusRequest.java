package com.tortiki.api.infrastructure.adapter.in.web.dto;

import com.tortiki.api.domain.model.ContactRequestStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de requête pour la mise à jour du statut d'une demande de contact.
 *
 * <p>Record immuable Java 21 — un seul champ obligatoire.
 * La validation {@code @NotNull} est vérifiée par Spring avant d'atteindre le service.</p>
 *
 * @param newStatus nouveau statut souhaité par le vendeur
 */
public record UpdateContactRequestStatusRequest(
    @NotNull(message = "Le nouveau statut est obligatoire")
    ContactRequestStatus newStatus
) {}