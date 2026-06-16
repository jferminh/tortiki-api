package com.tortiki.api.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité JPA représentant un utilisateur de la plateforme Tortiki.
 *
 * <p>Correspond à la table {@code users} créée par Flyway {@code V1__init_schema.sql}.
 * Cette classe appartient à la couche {@code infrastructure/adapter/out/persistence/}
 * et ne doit jamais être exposée au-delà de cette couche (pas de retour dans les
 * contrôleurs REST, pas d'import dans {@code domain/}).</p>
 *
 * <p>Le mot de passe est stocké sous forme de hash BCrypt (force 12) —
 * jamais en clair, conformément aux recommandations OWASP.</p>
 *
 * <p>Les coordonnées géographiques ({@code latitude}, {@code longitude}) sont
 * renseignées via l'adaptateur Nominatim ({@code NominatimGeoAdapter})
 * lors de la création ou modification de l'adresse.</p>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

  /**
   * Identifiant technique auto-incrémenté (BIGSERIAL PostgreSQL).
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Adresse email — identifiant unique de connexion.
   * Indexé implicitement par la contrainte UNIQUE.
   */
  @Column(nullable = false, unique = true)
  private String email;

  /**
   * Hash BCrypt du mot de passe (force 12, ~250ms/hash).
   * Jamais le mot de passe en clair — jamais loggué.
   */
  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  /**
   * Prénom de l'utilisateur.
   */
  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  /**
   * Nom de famille de l'utilisateur.
   */
  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  /**
   * Numéro de téléphone — optionnel, partagé au client après confirmation.
   */
  @Column(name = "phone", length = 20)
  private String phone;

  /**
   * URL de l'avatar stocké dans MinIO — optionnel.
   */
  @Column(name = "avatar_url", length = 500)
  private String avatarUrl;

  /**
   * Ville de l'utilisateur (saisie libre ou issue de Nominatim).
   */
  @Column(name = "city", length = 100)
  private String city;

  /**
   * Latitude géographique pour les recherches de proximité (Nominatim).
   * Précision 7 décimales ≈ 1 cm — suffisant pour usage urbain.
   */
  @Column(name = "latitude", precision = 10, scale = 7)
  private Double latitude;

  /**
   * Longitude géographique pour les recherches de proximité (Nominatim).
   */
  @Column(name = "longitude", precision = 10, scale = 7)
  private Double longitude;

  /**
   * Indique si le compte est actif.
   * Un compte désactivé ne peut pas se connecter ni publier d'annonces.
   */
  @Column(nullable = false)
  @Builder.Default
  private boolean enabled = true;

  /**
   * Date et heure de création du compte — non modifiable après insertion.
   */
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /**
   * Date et heure de dernière modification du compte.
   */
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /**
   * Rôles associés à cet utilisateur.
   *
   * <p>{@code FetchType.EAGER} est intentionnel : Spring Security charge les
   * autorités à chaque requête authentifiée via {@code UserDetailsServiceImpl}.
   * Un chargement {@code LAZY} provoquerait une
   * {@code LazyInitializationException} hors contexte transactionnel.</p>
   *
   * <p>Un utilisateur peut cumuler plusieurs rôles (ex : ROLE_SELLER + ROLE_BUYER).</p>
   */
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  @Builder.Default
  private Set<RoleEntity> roles = new HashSet<>();

  /**
   * Initialise les horodatages avant la première persistance.
   * Complète le {@code DEFAULT NOW()} Flyway pour les insertions hors SQL natif.
   */
  @PrePersist
  protected void onCreate() {
    final LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
    this.createdAt = now;
    this.updatedAt = now;
  }

  /**
   * Met à jour l'horodatage de modification à chaque sauvegarde JPA.
   */
  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
  }
}