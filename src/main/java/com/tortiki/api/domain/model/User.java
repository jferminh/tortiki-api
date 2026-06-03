package com.tortiki.api.domain.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Représente un utilisateur de la plateforme Tortiki.
 *
 * <p>POJO pur du domaine : aucune annotation Spring ou JPA.
 * Un utilisateur peut cumuler les rôles SELLER et BUYER simultanément.
 * La persistance est déléguée à {@code UserEntity} dans la couche
 * {@code infrastructure/adapter/out/persistence}.</p>
 *
 * <p>Le champ {@code passwordHash} contient le hash BCrypt (force 12)
 * du mot de passe. Le mot de passe en clair n'est jamais stocké,
 * conformément aux recommandations OWASP.</p>
 */
public class User {

  /** Identifiant technique de l'utilisateur. */
  private Long id;

  /** Adresse email unique, utilisée comme identifiant de connexion. */
  private String email;

  /** Hash BCrypt du mot de passe (force 12). */
  private String passwordHash;

  /** Prénom de l'utilisateur. */
  private String firstName;

  /** Nom de famille de l'utilisateur. */
  private String lastName;

  /** Indique si le compte est actif. Un compte désactivé ne peut pas se connecter. */
  private boolean enabled;

  /** Date et heure de création du compte. */
  private LocalDateTime createdAt;

  /** Date et heure de dernière modification du compte. */
  private LocalDateTime updatedAt;

  /** Ensemble des rôles attribués à cet utilisateur. */
  private Set<Role> roles = new HashSet<>();

  /** Constructeur par défaut requis pour les mappers domain ↔ entité JPA. */
  public User() {
    // Requis par les mappers manuels de la couche infrastructure/adapter/out/persistence
  }

  /**
   * Retourne l'identifiant technique de l'utilisateur.
   *
   * @return identifiant
   */
  public Long getId() {
    return id;
  }

  /**
   * Définit l'identifiant technique de l'utilisateur.
   *
   * @param id identifiant
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Retourne l'adresse email de l'utilisateur.
   *
   * @return email
   */
  public String getEmail() {
    return email;
  }

  /**
   * Définit l'adresse email de l'utilisateur.
   *
   * @param email adresse email
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Retourne le hash BCrypt du mot de passe.
   *
   * @return hash du mot de passe
   */
  public String getPasswordHash() {
    return passwordHash;
  }

  /**
   * Définit le hash BCrypt du mot de passe.
   *
   * @param passwordHash hash BCrypt
   */
  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  /**
   * Retourne le prénom de l'utilisateur.
   *
   * @return prénom
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Définit le prénom de l'utilisateur.
   *
   * @param firstName prénom
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * Retourne le nom de famille de l'utilisateur.
   *
   * @return nom de famille
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Définit le nom de famille de l'utilisateur.
   *
   * @param lastName nom de famille
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * Indique si le compte utilisateur est actif.
   *
   * @return {@code true} si le compte est activé
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Active ou désactive le compte utilisateur.
   *
   * @param enabled {@code true} pour activer le compte
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Retourne la date et heure de création du compte.
   *
   * @return date de création
   */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * Définit la date et heure de création du compte.
   *
   * @param createdAt date de création
   */
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * Retourne la date et heure de dernière modification du compte.
   *
   * @return date de modification
   */
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  /**
   * Définit la date et heure de dernière modification du compte.
   *
   * @param updatedAt date de modification
   */
  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  /**
   * Retourne l'ensemble des rôles attribués à l'utilisateur.
   *
   * @return ensemble de rôles
   */
  public Set<Role> getRoles() {
    return roles;
  }

  /**
   * Définit l'ensemble des rôles attribués à l'utilisateur.
   *
   * @param roles ensemble de rôles
   */
  public void setRoles(Set<Role> roles) {
    this.roles = roles;
  }

  /**
   * Ajoute un rôle à l'utilisateur.
   *
   * <p>Utilisé par {@code UserService} lors de l'inscription pour
   * attribuer le rôle demandé sans écraser les rôles existants.</p>
   *
   * @param role rôle à ajouter
   */
  public void addRole(Role role) {
    this.roles.add(role);
  }

  @Override
  public String toString() {
    return "User{id=" + id
        + ", email='" + email + "'"
        + ", enabled=" + enabled + "}";
  }
}