package com.tortiki.api.config;

/**
 * Constantes de sécurité partagées entre {@link SecurityConfig}
 * et les controllers via {@code @PreAuthorize}.
 *
 * <p>Centralise toutes les routes utilisées dans la configuration
 * de sécurité afin d'éviter la duplication de littéraux
 * et de garantir la cohérence entre {@link SecurityConfig}
 * et les {@code @RequestMapping} des controllers.</p>
 */
public final class SecurityConstants {

  // ── Rôles — sans préfixe ROLE_ (ajouté automatiquement par Spring) ────────

  /** Rôle administrateur de la plateforme. */
  public static final String ROLE_ADMIN  = "ADMIN";

  /** Rôle vendeur — publication et gestion des annonces. */
  public static final String ROLE_SELLER = "SELLER";

  /** Rôle acheteur — recherche, contact et notation. */
  public static final String ROLE_BUYER  = "BUYER";

  // ── Préfixe API ────────────────────────────────────────────────────────────

  /** Préfixe commun à tous les endpoints REST de Tortiki API. */
  public static final String API_V1 = "/api/v1";

  // ── Auth ───────────────────────────────────────────────────────────────────

  /** Route d'inscription d'un nouvel utilisateur. */
  public static final String ROUTE_AUTH_REGISTER = API_V1 + "/auth/register";

  /** Route de connexion. */
  public static final String ROUTE_AUTH_LOGIN    = API_V1 + "/auth/login";

  /** Route de déconnexion. */
  public static final String ROUTE_AUTH_LOGOUT   = API_V1 + "/auth/logout";

  // ── Annonces ───────────────────────────────────────────────────────────────

  /** Route de la collection d'annonces. */
  public static final String ROUTE_LISTINGS         = API_V1 + "/listings";

  /** Route d'une annonce par identifiant. */
  public static final String ROUTE_LISTING_BY_ID    = API_V1 + "/listings/{id}";

  /** Route de recherche géolocalisée des annonces. */
  public static final String ROUTE_LISTINGS_SEARCH  = API_V1 + "/listings/search";

  // ── Origines culinaires ────────────────────────────────────────────────────

  /** Route de la collection des origines culinaires. */
  public static final String ROUTE_CUISINE_TYPES     = API_V1 + "/cuisine-types";

  /** Route générique pour une origine culinaire (lecture, modification, suppression). */
  public static final String ROUTE_CUISINE_TYPES_ALL = API_V1 + "/cuisine-types/**";

  // ── Allergènes ─────────────────────────────────────────────────────────────

  /** Route de la collection des allergènes. */
  public static final String ROUTE_ALLERGENS     = API_V1 + "/allergens";

  /** Route générique pour un allergène. */
  public static final String ROUTE_ALLERGENS_ALL = API_V1 + "/allergens/**";

  // ── Demandes de contact ────────────────────────────────────────────────────

  /** Route de soumission d'une demande de contact. */
  public static final String ROUTE_CONTACT_REQUESTS = API_V1 + "/contact-requests";

  /** Route de consultation des demandes de l'acheteur connecté. */
  public static final String ROUTE_CONTACT_MY       = API_V1 + "/contact-requests/my";

  /** Route de consultation des demandes reçues par le vendeur. */
  public static final String ROUTE_CONTACT_SELLER   = API_V1 + "/contact-requests/seller";

  /** Route de confirmation d'une demande de contact. */
  public static final String ROUTE_CONTACT_CONFIRM  = API_V1 + "/contact-requests/{id}/confirm";

  /** Route de refus d'une demande de contact. */
  public static final String ROUTE_CONTACT_REFUSE   = API_V1 + "/contact-requests/{id}/refuse";

  // ── Notations ──────────────────────────────────────────────────────────────

  /** Route de soumission d'une notation (Sprint 3). */
  public static final String ROUTE_REVIEWS = API_V1 + "/reviews";

  // ── Administration ─────────────────────────────────────────────────────────

  /** Route générique de l'espace d'administration. */
  public static final String ROUTE_ADMIN_ALL          = API_V1 + "/admin/**";

  /** Route de modération des annonces signalées (Sprint 3). */
  public static final String ROUTE_ADMIN_LISTINGS_ALL = API_V1 + "/admin/listings/**";

  private SecurityConstants() {
    // Classe utilitaire — instanciation interdite
  }
}