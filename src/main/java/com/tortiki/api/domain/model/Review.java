package com.tortiki.api.domain.model;

import java.time.LocalDateTime;

/**
 * Représente une évaluation laissée par un acheteur sur une annonce.
 *
 * <p>POJO métier pur — zéro annotation Spring ou JPA.
 * La note doit être comprise entre 1 et 5 inclus.
 * Un acheteur ne peut laisser qu'une seule évaluation par annonce.</p>
 */
public class Review {

  /**
   * Identifiant technique de l'évaluation.
   */
  private final Long id;

  /**
   * Annonce évaluée.
   */
  private final Listing listing;

  /**
   * Utilisateur ayant laissé l'évaluation.
   */
  private final User reviewer;

  /**
   * Note de 1 à 5.
   */
  private final Integer rating;

  /**
   * Commentaire libre de l'acheteur (optionnel).
   */
  private final String comment;

  /**
   * Date et heure de création de l'évaluation.
   */
  private final LocalDateTime createdAt;

  /**
   * Constructeur complet utilisé par les factories et mappers.
   *
   * @param id        identifiant (null à la création)
   * @param listing   annonce évaluée
   * @param reviewer  utilisateur auteur de l'évaluation
   * @param rating    note entre 1 et 5
   * @param comment   commentaire optionnel
   * @param createdAt horodatage de création
   */
  public Review(
      Long id,
      Listing listing,
      User reviewer,
      Integer rating,
      String comment,
      LocalDateTime createdAt) {
    this.id = id;
    this.listing = listing;
    this.reviewer = reviewer;
    this.rating = rating;
    this.comment = comment;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public Listing getListing() {
    return listing;
  }

  public User getReviewer() {
    return reviewer;
  }

  public Integer getRating() {
    return rating;
  }

  public String getComment() {
    return comment;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }


}