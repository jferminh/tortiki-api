package com.tortiki.api.domain.model;

/**
 * Représente un allergène réglementaire reconnu par la plateforme Tortiki.
 *
 * <p>POJO pur du domaine — aucune annotation Spring ou JPA.
 * Les 14 allergènes majeurs sont définis par la réglementation européenne INCO
 * et insérés en base via la migration Flyway {@code V1__init_schema.sql}.</p>
 *
 * <p>Un allergène est associé à une {@link Listing} via une relation
 * {@code ManyToMany} gérée dans la couche
 * {@code infrastructure/adapter/out/persistence/}.</p>
 *
 * <p>Le champ {@code enabled} permet à {@code ROLE_ADMIN} de désactiver
 * un allergène sans le supprimer physiquement, évitant ainsi de casser
 * l'intégrité référentielle des annonces existantes qui y font référence.</p>
 */
public class Allergen {

  /** Identifiant technique de l'allergène. */
  private Long id;

  /** Nom officiel de l'allergène (ex. Gluten, Lactose, Arachides). */
  private String name;

  /** Indique si l'allergène est actif et proposé dans les formulaires. */
  private boolean enabled;

  /** Constructeur par défaut requis pour les mappers domain ↔ entité JPA. */
  public Allergen() {
  }

  /**
   * Constructeur complet.
   *
   * @param id      identifiant technique
   * @param name    nom officiel de l'allergène
   * @param enabled statut d'activation de l'allergène
   */
  public Allergen(Long id, String name, boolean enabled) {
    this.id = id;
    this.name = name;
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
   * Retourne le nom officiel de l'allergène.
   *
   * @return nom
   */
  public String getName() {
    return name;
  }

  /**
   * Définit le nom officiel de l'allergène.
   *
   * @param name nom
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Indique si l'allergène est actif.
   *
   * @return {@code true} si l'allergène est activé
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Active ou désactive l'allergène.
   *
   * @param enabled nouveau statut d'activation
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String toString() {
    return "Allergen{id=" + id + ", name=" + name + ", enabled=" + enabled + "}";
  }
}