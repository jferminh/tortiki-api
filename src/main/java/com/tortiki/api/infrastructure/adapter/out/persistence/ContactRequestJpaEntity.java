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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

/**
 * Entité JPA représentant la table {@code contact_requests}.
 *
 * <p>Classe technique de la couche
 * {@code infrastructure/adapter/out/persistence/} —
 * ne doit jamais remonter dans le domaine ou la couche application.</p>
 *
 * <p>Contrainte d'unicité {@code UNIQUE(listing_id, buyer_id)} déclarée
 * en annotation et garantie en base via {@code V1__init_schema.sql}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "contact_requests",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_contact_requests_listing_buyer",
        columnNames = {"listing_id", "buyer_id"}
    )
)
public class ContactRequestJpaEntity {

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
  private UserJpaEntity buyer;

  /** Nombre de portions souhaitées. */
  @Column(nullable = false)
  private Integer portions;

  /** Statut courant de la demande — type ENUM PostgreSQL natif. */
  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  @Column(name = "status", nullable = false, columnDefinition = "contact_request_status")
  private ContactRequestStatus status;

  /** Message optionnel laissé par l'acheteur. */
  @Column(columnDefinition = "TEXT")
  private String message;

  /** Date de création — initialisée par {@link #prePersist()}, non modifiable. */
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /** Date de dernière mise à jour — gérée par {@link #preUpdate()}. */
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /**
   * Initialise {@code createdAt} et {@code updatedAt} à la création de l'entité.
   */
  @PrePersist
  void prePersist() {
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
    this.createdAt = now;
    this.updatedAt = now;
  }

  /**
   * Met à jour {@code updatedAt} à chaque modification de l'entité.
   */
  @PreUpdate
  void preUpdate() {
    this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
  }
}