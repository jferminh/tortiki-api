package com.tortiki.api.domain.model;

/**
 * Statut du cycle de vie d'une annonce Tortiki.
 *
 * <ul>
 *   <li>{@code ACTIVE} — annonce visible et disponible à la recherche</li>
 *   <li>{@code INACTIVE} — annonce désactivée par le vendeur</li>
 *   <li>{@code MODERATED} — annonce suspendue par un administrateur</li>
 * </ul>
 */
public enum ListingStatus {

  /** Annonce visible et disponible à la recherche. */
  ACTIVE,

  /** Annonce désactivée par le vendeur. */
  INACTIVE,

  /** Annonce suspendue suite à un signalement par un administrateur. */
  MODERATED
}