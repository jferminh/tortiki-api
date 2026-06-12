package com.tortiki.api.domain.model;

/**
 * Représente une origine culinaire du référentiel Tortiki.
 *
 * <p>POJO pur du domaine : aucune annotation Spring ou JPA.
 * Exemples : Ukrainienne, Marocaine, Japonaise, Végétalienne.</p>
 *
 * <p>La gestion du référentiel (CRUD) est réservée au rôle
 * {@code ROLE_ADMIN}. Ce modèle est utilisé comme critère de
 * filtrage dans la recherche d'annonces.</p>
 *
 * <p>Une origine désactivée ({@code enabled = false}) n'est pas
 * proposée lors de la création d'annonce — règle métier administrable
 * sans migration de schéma.</p>
 */
public class CuisineType {

  /** Identifiant technique de l'origine culinaire. */
  private Long id;

  /** Nom de l'origine culinaire, unique en base. */
  private String name;

  /** Description optionnelle de l'origine culinaire. */
  private String description;

  /**
   * Indique si l'origine est active et proposable à la création d'annonce.
   * Valeur par défaut : {@code true}.
   */
  private boolean enabled = true;

  /**
   * Constructeur par défaut requis pour les mappers domain ↔ entité JPA.
   */
  public CuisineType() {
    // Requis par les mappers manuels de la couche infrastructure/adapter/out/persistence
  }

  /**
   * Constructeur complet.
   *
   * @param id          identifiant technique
   * @param name        nom de l'origine culinaire
   * @param description description optionnelle
   * @param enabled     indique si l'origine est active
   */
  public CuisineType(Long id, String name, String description, boolean enabled) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.enabled = enabled;
  }

  /**
   * Retourne l'identifiant technique.
   *
   * @return identifiant
   */
  public Long getId() {
    return id;
  }

  /**
   * Définit l'identifiant technique.
   *
   * @param id identifiant
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Retourne le nom de l'origine culinaire.
   *
   * @return nom
   */
  public String getName() {
    return name;
  }

  /**
   * Définit le nom de l'origine culinaire.
   *
   * @param name nom unique
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Retourne la description de l'origine culinaire.
   *
   * @return description ou {@code null}
   */
  public String getDescription() {
    return description;
  }

  /**
   * Définit la description de l'origine culinaire.
   *
   * @param description description optionnelle
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Indique si l'origine culinaire est active.
   *
   * @return {@code true} si active, {@code false} si désactivée par un admin
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Définit si l'origine culinaire est active.
   *
   * @param enabled {@code true} pour activer, {@code false} pour désactiver
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String toString() {
    return "CuisineType{id=" + id
        + ", name='" + name + "'"
        + ", enabled=" + enabled
        + "}";
  }
}