package com.tortiki.api.config;

/**
 * Constantes de sécurité partagées dans toute l'application Tortiki.
 *
 * <p>Centralise les noms de rôles RBAC et les routes sensibles afin
 * d'éviter les chaînes magiques dispersées dans les contrôleurs,
 * services et tests de sécurité.</p>
 *
 * <p>Classe utilitaire non instanciable.</p>
 */
public final class SecurityConstants {

  /** Rôle administrateur de la plateforme. */
  public static final String ROLE_ADMIN = "ADMIN";

  /** Rôle vendeur — gestion des annonces et des demandes de contact. */
  public static final String ROLE_SELLER = "SELLER";

  /** Rôle acheteur — recherche et expression d'intérêt. */
  public static final String ROLE_BUYER = "BUYER";

  /** Route d'une annonce par identifiant numérique. */
  public static final String ROUTE_LISTING_BY_ID = "/api/listings/{id}";

  /**
   * Route de confirmation d'une demande de contact.
   *
   * <p>Le segment {@code *} matche un identifiant numérique simple.
   * Ne pas remplacer par {@code **} — l'id ne contient pas de slash.</p>
   */
  public static final String ROUTE_CONTACT_CONFIRM = "/api/contact-requests/*/confirm";

  /** Route de refus d'une demande de contact. */
  public static final String ROUTE_CONTACT_REFUSE = "/api/contact-requests/*/refuse";

  /** Constructeur privé — classe utilitaire non instantiable. */
  private SecurityConstants() {
    throw new UnsupportedOperationException(
        "SecurityConstants est une classe utilitaire non instantiable"
    );
  }
}