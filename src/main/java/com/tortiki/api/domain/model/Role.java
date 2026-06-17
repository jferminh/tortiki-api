package com.tortiki.api.domain.model;

import java.util.Objects;

/**
 * Représente un rôle métier attribué à un utilisateur de la plateforme.
 *
 * <p>POJO pur du domaine : aucune annotation Spring ou JPA.
 * La persistance est déléguée à {@code RoleJpaEntity} dans la couche
 * {@code infrastructure/adapter/out/persistence}.</p>
 */
public class Role {

  /** Identifiant technique du rôle. */
  private Long id;

  /** Nom du rôle (ADMIN, SELLER, BUYER). */
  private RoleName name;

  /** Constructeur par défaut requis pour les mappers. */
  public Role() {
  }

  /**
   * Constructeur complet.
   *
   * @param id   identifiant technique
   * @param name nom du rôle
   */
  public Role(Long id, RoleName name) {
    this.id = id;
    this.name = name;
  }

  /**
   * Retourne l'identifiant technique du rôle.
   *
   * @return identifiant
   */
  public Long getId() {
    return id;
  }

  /**
   * Définit l'identifiant technique du rôle.
   *
   * @param id identifiant
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Retourne le nom du rôle.
   *
   * @return nom du rôle
   */
  public RoleName getName() {
    return name;
  }

  /**
   * Définit le nom du rôle.
   *
   * @param name nom du rôle
   */
  public void setName(RoleName name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Role role)) {
      return false;
    }
    return name == role.name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return "Role{id=" + id + ", name=" + name + "}";
  }
}