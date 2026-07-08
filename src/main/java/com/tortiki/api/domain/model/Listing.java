package com.tortiki.api.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Représente une annonce de plat fait maison publiée par un vendeur.
 *
 * <p>POJO pur du domaine : aucune annotation Spring ou JPA. {@link Getter}
 * et {@link Setter} de Lombok sont des processeurs d'annotation à la
 * compilation — ils disparaissent du bytecode généré et ne créent aucune
 * dépendance runtime à un framework d'infrastructure.</p>
 *
 * <p>Une annonce contient les informations métier nécessaires au parcours
 * Click &amp; Collect : description du plat, prix, portions disponibles,
 * créneau de retrait et photo.</p>
 *
 * <p>Contraintes métier :</p>
 * <ul>
 *   <li>Un seul créneau de retrait par annonce en v1.</li>
 *   <li>Une seule photo par annonce en v1.</li>
 *   <li>Le vendeur est obligatoirement un {@link User} avec le rôle
 *       {@code ROLE_SELLER}.</li>
 *   <li>Les allergènes sont obligatoires conformément au règlement INCO EU
 *       n°1169/2011.</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
public class Listing {

  /** Identifiant technique. */
  private Long id;

  /** Titre affiché dans les résultats de recherche. */
  private String title;

  /** Description détaillée du plat. */
  private String description;

  /** Prix unitaire en euros. */
  private BigDecimal price;

  /** Nombre de portions disponibles. */
  private Integer portions;

  /** URL de la photo stockée dans MinIO. */
  private String photoUrl;

  /** Adresse de retrait saisie par le vendeur. */
  private String pickupAddress;

  /** Latitude géocodée via Nominatim. */
  private Double pickupLat;

  /** Longitude géocodée via Nominatim. */
  private Double pickupLng;

  /** Date et heure du créneau de retrait. */
  private LocalDateTime pickupDatetime;

  /** Statut de l'annonce. */
  private ListingStatus status;

  /** Date de création. */
  private LocalDateTime createdAt;

  /** Date de dernière modification. */
  private LocalDateTime updatedAt;

  /** Vendeur propriétaire de l'annonce. */
  private User seller;

  /** Origine culinaire associée. */
  private CuisineType cuisineType;

  /**
   * Allergènes présents dans le plat.
   * Conformément au règlement INCO EU n°1169/2011.
   */
  private List<Allergen> allergens = new ArrayList<>();

  /**
   * Définit la liste des allergènes, en garantissant qu'elle n'est jamais
   * {@code null} même si {@code null} est passé explicitement.
   *
   * <p>Remplace le setter généré par Lombok pour ce champ précis —
   * Lombok respecte l'ordre de déclaration des méthodes et n'écrase
   * jamais un setter manuel déjà présent dans la classe.</p>
   *
   * @param allergens liste d'allergènes, peut être {@code null}
   */
  public void setAllergens(List<Allergen> allergens) {
    this.allergens = allergens != null ? allergens : new ArrayList<>();
  }

  @Override
  public String toString() {
    return "Listing{id=" + id
        + ", title='" + title + "'"
        + ", status=" + status
        + ", seller=" + (seller != null ? seller.getEmail() : "null")
        + "}";
  }
}