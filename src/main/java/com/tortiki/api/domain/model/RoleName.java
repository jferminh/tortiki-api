package com.tortiki.api.domain.model;

/**
 * Enumération des rôles métier de la plateforme Tortiki.
 *
 * <p>Ces constantes sont utilisées dans les règles d'autorisation RBAC
 * de Spring Security. Les valeurs correspondent aux préfixes attendus
 * par {@code hasRole()} : Spring ajoute automatiquement le préfixe
 * {@code ROLE_} lors de la vérification.</p>
 *
 * <ul>
 *   <li>{@code ADMIN} — administration de la plateforme</li>
 *   <li>{@code SELLER} — publication et gestion des annonces</li>
 *   <li>{@code BUYER} — recherche et expression d'intérêt</li>
 * </ul>
 */
public enum RoleName {

  /** Rôle administrateur : gestion du référentiel et modération. */
  ADMIN,

  /** Rôle vendeur : création et gestion des annonces de plats. */
  SELLER,

  /** Rôle acheteur : recherche de plats et expression d'intérêt. */
  BUYER
}