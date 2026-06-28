package com.tortiki.api.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité JPA représentant une évaluation en base de données.
 *
 * <p>Séparée du POJO domaine {@code Review} — aucune annotation JPA
 * ne doit apparaître dans {@code domain/model/}.
 * La contrainte d'unicité {@code uq_review_contact_request} garantit
 * qu'une demande confirmée ne peut générer qu'une seule évaluation.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "reviews",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_review_contact_request",
        columnNames = "contact_request_id"
    )
)
public class ReviewJpaEntity {

  /** Identifiant technique généré par la séquence PostgreSQL. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Demande de contact confirmée à l'origine de l'évaluation.
   * Contrainte UNIQUE portée par {@code uq_review_contact_request}.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contact_request_id", nullable = false)
  private ContactRequestJpaEntity contactRequest;

  /** Acheteur auteur de l'évaluation. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewer_id", nullable = false)
  private UserJpaEntity reviewer;

  /** Vendeur évalué — dénormalisé pour simplifier les agrégats de notes. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id", nullable = false)
  private UserJpaEntity seller;

  /** Annonce évaluée. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "listing_id", nullable = false)
  private ListingJpaEntity listing;

  /** Note de 1 à 5. */
  @Column(nullable = false)
  private Integer rating;

  /** Commentaire libre — optionnel. */
  @Column(columnDefinition = "TEXT")
  private String comment;

  /** Date et heure de création. */
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}