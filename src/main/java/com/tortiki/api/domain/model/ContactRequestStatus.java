package com.tortiki.api.domain.model;

/**
 * Statut du cycle de vie d'une demande de contact sur Tortiki.
 *
 * <ul>
 *   <li>{@code PENDING} — demande envoyée, en attente de réponse du vendeur</li>
 *   <li>{@code CONFIRMED} — vendeur a confirmé le retrait</li>
 *   <li>{@code REFUSED} — vendeur a refusé la demande</li>
 * </ul>
 *
 * <p>Correspond au type ENUM PostgreSQL {@code contact_request_status}
 * défini dans la migration {@code V1__init_schema.sql}.</p>
 */
public enum ContactRequestStatus {

  /** Demande envoyée, en attente de réponse du vendeur. */
  PENDING,

  /** Vendeur a confirmé le retrait du plat. */
  CONFIRMED,

  /** Vendeur a refusé la demande. */
  REFUSED
}