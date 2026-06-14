package com.tortiki.api.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de requête pour la soumission d'une demande de contact.
 *
 * <p>Record immuable Java 21 — reçu depuis le body HTTP JSON.
 * Validé par Bean Validation avant transmission au service applicatif.</p>
 *
 * @param listingId identifiant de l'annonce ciblée
 * @param message   message optionnel laissé par l'acheteur (500 caractères max)
 * @param portions  nombre de portions souhaitées (1 à 20)
 */
public record CreateContactRequestRequest(

    @NotNull(message = "L'identifiant de l'annonce est obligatoire")
    Long listingId,

    @Size(max = 500, message = "Le message ne peut pas dépasser 500 caractères")
    String message,

    @NotNull(message = "Le nombre de portions est obligatoire")
    @Min(value = 1, message = "Le nombre de portions minimum est 1")
    @Max(value = 20, message = "Le nombre de portions maximum est 20")
    Integer portions
) {}