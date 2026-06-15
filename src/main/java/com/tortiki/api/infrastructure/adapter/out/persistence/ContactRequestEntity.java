package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.ContactRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité JPA représentant la table {@code contact_requests}.
 *
 * <p>Classe technique de la couche {@code infrastructure/adapter/out/persistence/} —
 * ne doit jamais remonter dans le domaine ou la couche application.</p>
 *
 * <p>Contrainte d'unicité {@code UNIQUE(listing_id, buyer_id)} gérée
 * en base via {@code V1__init_schema.sql}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "contact_requests")
public class ContactRequestEntity {

  /** Identifiant technique auto-généré. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Annonce concernée par la demande. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "listing_id", nullable = false)
  private ListingJpaEntity listing;

  /** Acheteur ayant soumis la demande. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "buyer_id", nullable = false)
  private UserEntity buyer;

  /** Nombre de portions souhaitées. */
  @Column(nullable = false)
  private Integer portions;

  /** Statut courant de la demande. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ContactRequestStatus status;

  /** Message optionnel laissé par l'acheteur. */
  @Column(columnDefinition = "TEXT")
  private String message;

  /** Date de création — initialisée par {@link #prePersist()}. */
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /** Date de dernière mise à jour — gérée par {@link #preUpdate()}. */
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /**
   * Initialise les dates à la création de l'entité.
   */
  @PrePersist
  void prePersist() {
    LocalDateTime now = LocalDateTime.now(java.time.Clock.systemUTC());
    this.createdAt = now;
    this.updatedAt = now;
  }

  /**
   * Met à jour la date de modification à chaque persistance.
   */
  @PreUpdate
  void preUpdate() {
    this.updatedAt = LocalDateTime.now(java.time.Clock.systemUTC());
  }
}