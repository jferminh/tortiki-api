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
 */
public class Allergen {

  /** Identifiant technique de l'allergène. */
  private Long id;

  /** Nom officiel de l'allergène (ex. Gluten, Lactose, Arachides). */
  private String name;

  /** Constructeur par défaut requis pour les mappers domain ↔ entité JPA. */
  public Allergen() {
  }

  /**
   * Constructeur complet.
   *
   * @param id   identifiant technique
   * @param name nom officiel de l'allergène
   */
  public Allergen(Long id, String name) {
    this.id = id;
    this.name = name;
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

  @Override
  public String toString() {
    return "Allergen{id=" + id + ", name=" + name + "}";
  }
}