package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.ListingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité JPA représentant une annonce de plat en base de données.
 *
 * <p>Appartient exclusivement à la couche
 * {@code infrastructure/adapter/out/persistence/}.
 * Le domaine ne connaît jamais cette classe — il utilise uniquement
 * le POJO {@code Listing}.</p>
 *
 * <p>La relation {@code ManyToMany} avec {@link AllergenJpaEntity} est
 * gérée via la table de jointure {@code listing_allergens}, conforme
 * au règlement INCO EU n°1169/2011.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "listings")
public class ListingJpaEntity {

  /** Identifiant technique généré par la séquence PostgreSQL. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Titre de l'annonce. */
  @Column(name = "title", nullable = false)
  private String title;

  /** Description détaillée du plat. */
  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  /** Prix unitaire en euros. */
  @Column(name = "price", nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  /** Nombre de portions disponibles. */
  @Column(name = "portions", nullable = false)
  private Integer portions;

  /** URL de la photo stockée dans MinIO. */
  @Column(name = "photo_url", length = 500)
  private String photoUrl;

  /** Adresse de retrait saisie par le vendeur. */
  @Column(name = "pickup_address", nullable = false)
  private String pickupAddress;

  /** Latitude géocodée via Nominatim. */
  @Column(name = "pickup_lat", precision = 10, scale = 7)
  private Double pickupLat;

  /** Longitude géocodée via Nominatim. */
  @Column(name = "pickup_lng", precision = 10, scale = 7)
  private Double pickupLng;

  /** Date et heure du créneau de retrait. */
  @Column(name = "pickup_datetime", nullable = false)
  private LocalDateTime pickupDatetime;

  /**
   * Statut de l'annonce mappé sur le type ENUM PostgreSQL {@code listing_status}.
   * Stocké comme chaîne de caractères via {@code EnumType.STRING}.
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private ListingStatus status;

  /** Date de création. */
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /** Date de dernière modification. */
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /**
   * Vendeur propriétaire de l'annonce.
   * {@code LAZY} : le vendeur n'est chargé que si explicitement accédé.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id", nullable = false)
  private UserEntity seller;

  /**
   * Origine culinaire associée à l'annonce.
   * {@code LAZY} : chargé uniquement si accédé.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cuisine_type_id", nullable = false)
  private CuisineTypeJpaEntity cuisineType;

  /**
   * Allergènes présents dans le plat.
   * Table de jointure {@code listing_allergens} — conforme INCO EU.
   * {@code LAZY} : chargé uniquement lors de l'accès explicite.
   */
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "listing_allergens",
      joinColumns = @JoinColumn(name = "listing_id"),
      inverseJoinColumns = @JoinColumn(name = "allergen_id")
  )
  private List<AllergenJpaEntity> allergens = new ArrayList<>();
}