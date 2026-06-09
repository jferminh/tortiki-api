package com.tortiki.api.domain.model;

import java.time.LocalDateTime;

/**
 * Représente une demande d'intérêt d'un acheteur pour une annonce de plat.
 *
 * <p>POJO pur du domaine — aucune annotation Spring ou JPA.
 * Un acheteur ne peut soumettre qu'une seule demande par annonce
 * (contrainte {@code UNIQUE(listing_id, buyer_id)} en base).</p>
 *
 * <p>Le cycle de vie est géré par le vendeur via
 * {@code SubmitContactRequestUseCase} et {@code ManageContactRequestUseCase}
 * (Sprint 3).</p>
 */
public class ContactRequest {

  /** Identifiant technique de la demande. */
  private Long id;

  /** Annonce concernée par la demande. */
  private Listing listing;

  /** Acheteur ayant soumis la demande. */
  private User buyer;

  /** Statut courant de la demande. */
  private ContactRequestStatus status;

  /** Message optionnel laissé par l'acheteur au vendeur. */
  private String message;

  /** Nombre de portions souhaitées par l'acheteur. */
  private Integer portions;

  /** Date et heure de création de la demande. */
  private LocalDateTime createdAt;

  /** Date et heure de dernière mise à jour de la demande. */
  private LocalDateTime updatedAt;

  /** Constructeur par défaut requis pour les mappers domain ↔ entité JPA. */
  public ContactRequest() {
  }

  /**
   * Constructeur complet.
   *
   * @param id        identifiant technique
   * @param listing   annonce concernée
   * @param buyer     acheteur ayant soumis la demande
   * @param status    statut courant
   * @param message   message optionnel
   * @param portions  nombre de portions souhaitées
   * @param createdAt date de création
   * @param updatedAt date de mise à jour
   */
  public ContactRequest(Long id, Listing listing, User buyer,
                        ContactRequestStatus status, String message, Integer portions,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
    this.id = id;
    this.listing = listing;
    this.buyer = buyer;
    this.status = status;
    this.message = message;
    this.portions = portions;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
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
   * Retourne l'annonce concernée par la demande.
   *
   * @return annonce
   */
  public Listing getListing() {
    return listing;
  }

  /**
   * Définit l'annonce concernée par la demande.
   *
   * @param listing annonce
   */
  public void setListing(Listing listing) {
    this.listing = listing;
  }

  /**
   * Retourne l'acheteur ayant soumis la demande.
   *
   * @return acheteur
   */
  public User getBuyer() {
    return buyer;
  }

  /**
   * Définit l'acheteur ayant soumis la demande.
   *
   * @param buyer acheteur
   */
  public void setBuyer(User buyer) {
    this.buyer = buyer;
  }

  /**
   * Retourne le statut courant de la demande.
   *
   * @return statut
   */
  public ContactRequestStatus getStatus() {
    return status;
  }

  /**
   * Définit le statut courant de la demande.
   *
   * @param status statut
   */
  public void setStatus(ContactRequestStatus status) {
    this.status = status;
  }

  /**
   * Retourne le message optionnel laissé par l'acheteur.
   *
   * @return message ou {@code null}
   */
  public String getMessage() {
    return message;
  }

  /**
   * Définit le message optionnel laissé par l'acheteur.
   *
   * @param message message
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Retourne le nombre de portions souhaitées.
   *
   * @return nombre de portions
   */
  public Integer getPortions() {
    return portions;
  }

  /**
   * Définit le nombre de portions souhaitées.
   *
   * @param portions nombre de portions
   */
  public void setPortions(Integer portions) {
    this.portions = portions;
  }

  /**
   * Retourne la date et heure de création de la demande.
   *
   * @return date de création
   */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * Définit la date et heure de création de la demande.
   *
   * @param createdAt date de création
   */
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * Retourne la date et heure de dernière mise à jour.
   *
   * @return date de mise à jour
   */
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  /**
   * Définit la date et heure de dernière mise à jour.
   *
   * @param updatedAt date de mise à jour
   */
  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public String toString() {
    return "ContactRequest{id=" + id
        + ", status=" + status
        + ", buyer=" + (buyer != null ? buyer.getEmail() : null)
        + ", listing=" + (listing != null ? listing.getId() : null)
        + "}";
  }
}