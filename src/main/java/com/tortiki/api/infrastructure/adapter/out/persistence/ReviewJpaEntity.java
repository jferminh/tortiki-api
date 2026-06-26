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
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité JPA représentant une évaluation en base de données.
 *
 * <p>Séparée du POJO domaine {@code Review} — aucune annotation JPA
 * ne doit apparaître dans {@code domain/model/}.
 * La contrainte d'unicité est portée par {@code contact_request_id UNIQUE}
 * définie dans V1 — garantit 1 avis par transaction confirmée.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reviews")
public class ReviewJpaEntity {

  /** Identifiant technique généré par la séquence PostgreSQL. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Demande de contact confirmée à l'origine de l'évaluation.
   * Contrainte UNIQUE en base — garantit 1 avis par demande.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contact_request_id", nullable = false, unique = true)
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
  @Column(nullable = false)
  private LocalDateTime createdAt;
}