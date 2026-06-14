package com.tortiki.api.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une annonce de plat fait maison publiée par un vendeur.
 *
 * <p>POJO pur du domaine : aucune annotation Spring ou JPA.
 * Une annonce contient les informations métier nécessaires au parcours
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
public class Listing {

  /**
   * Identifiant technique.
   */
  private Long id;

  /**
   * Titre affiché dans les résultats de recherche.
   */
  private String title;

  /**
   * Description détaillée du plat.
   */
  private String description;

  /**
   * Prix unitaire en euros.
   */
  private BigDecimal price;

  /**
   * Nombre de portions disponibles.
   */
  private Integer portions;

  /**
   * URL de la photo stockée dans MinIO.
   */
  private String photoUrl;

  /**
   * Adresse de retrait saisie par le vendeur.
   */
  private String pickupAddress;

  /**
   * Latitude géocodée via Nominatim.
   */
  private Double pickupLat;

  /**
   * Longitude géocodée via Nominatim.
   */
  private Double pickupLng;

  /**
   * Date et heure du créneau de retrait.
   * Remplace {@code pickupSlot} (String libre) aligné sur
   * {@code pickup_datetime TIMESTAMP} en base.
   */
  private LocalDateTime pickupDatetime;

  /**
   * Statut de l'annonce.
   */
  private ListingStatus status;

  /**
   * Date de création.
   */
  private LocalDateTime createdAt;

  /**
   * Date de dernière modification.
   */
  private LocalDateTime updatedAt;

  /**
   * Vendeur propriétaire de l'annonce.
   */
  private User seller;

  /**
   * Origine culinaire associée.
   */
  private CuisineType cuisineType;

  /**
   * Allergènes présents dans le plat.
   * Conformément au règlement INCO EU n°1169/2011.
   */
  private List<Allergen> allergens = new ArrayList<>();

  /**
   * Constructeur par défaut requis pour les mappers domain ↔ entité JPA.
   */
  public Listing() {
    // Requis par les mappers manuels de la couche infrastructure/adapter/out/persistence
  }

  /**
   * Retourne l'identifiant technique de l'annonce.
   *
   * @return identifiant
   */
  public Long getId() {
    return id;
  }

  /**
   * Définit l'identifiant technique de l'annonce.
   *
   * @param id identifiant
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Retourne le titre de l'annonce.
   *
   * @return titre
   */
  public String getTitle() {
    return title;
  }

  /**
   * Définit le titre de l'annonce.
   *
   * @param title titre
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Retourne la description du plat.
   *
   * @return description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Définit la description du plat.
   *
   * @param description description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Retourne le prix unitaire en euros.
   *
   * @return prix
   */
  public BigDecimal getPrice() {
    return price;
  }

  /**
   * Définit le prix unitaire en euros.
   *
   * @param price prix
   */
  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  /**
   * Retourne le nombre de portions disponibles.
   *
   * @return nombre de portions
   */
  public Integer getPortions() {
    return portions;
  }

  /**
   * Définit le nombre de portions disponibles.
   *
   * @param portions nombre de portions
   */
  public void setPortions(Integer portions) {
    this.portions = portions;
  }

  /**
   * Retourne l'URL de la photo stockée dans MinIO.
   *
   * @return URL de la photo ou {@code null}
   */
  public String getPhotoUrl() {
    return photoUrl;
  }

  /**
   * Définit l'URL de la photo stockée dans MinIO.
   *
   * @param photoUrl URL de la photo
   */
  public void setPhotoUrl(String photoUrl) {
    this.photoUrl = photoUrl;
  }

  /**
   * Retourne l'adresse de retrait saisie par le vendeur.
   *
   * @return adresse de retrait
   */
  public String getPickupAddress() {
    return pickupAddress;
  }

  /**
   * Définit l'adresse de retrait.
   *
   * @param pickupAddress adresse de retrait
   */
  public void setPickupAddress(String pickupAddress) {
    this.pickupAddress = pickupAddress;
  }

  /**
   * Retourne la latitude géocodée.
   *
   * @return latitude géocodée
   */
  public Double getPickupLat() {
    return pickupLat;
  }

  /**
   * Définit la latitude géocodée via Nominatim.
   *
   * @param pickupLat latitude en degrés décimaux
   */
  public void setPickupLat(Double pickupLat) {
    this.pickupLat = pickupLat;
  }

  /**
   * Retourne la longitude.
   *
   * @return longitude géocodée
   */
  public Double getPickupLng() {
    return pickupLng;
  }

  /**
   * Définit la longitude géocodée via Nominatim.
   *
   * @param pickupLng longitude en degrés décimaux
   */
  public void setPickupLng(Double pickupLng) {
    this.pickupLng = pickupLng;
  }

  /**
   * Retourne la date et heure du créneau de retrait.
   *
   * @return date et heure du créneau de retrait
   */
  public LocalDateTime getPickupDatetime() {
    return pickupDatetime;
  }

  /**
   * Définit la date et heure de retrait.
   *
   * @param pickupDatetime date et heure de retrait
   */
  public void setPickupDatetime(LocalDateTime pickupDatetime) {
    this.pickupDatetime = pickupDatetime;
  }

  /**
   * Retourne le statut de l'annonce.
   *
   * @return statut
   */
  public ListingStatus getStatus() {
    return status;
  }

  /**
   * Définit le statut de l'annonce.
   *
   * @param status statut
   */
  public void setStatus(ListingStatus status) {
    this.status = status;
  }

  /**
   * Retourne la date et heure de création de l'annonce.
   *
   * @return date de création
   */
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  /**
   * Définit la date et heure de création de l'annonce.
   *
   * @param createdAt date de création
   */
  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * Retourne la date de la dernière modification.
   *
   * @return date de dernière modification */
  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  /**
   * Définit la date de la modification.
   *
   * @param updatedAt date de modification */
  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  /**
   * Retourne le vendeur propriétaire de l'annonce.
   *
   * @return vendeur
   */
  public User getSeller() {
    return seller;
  }

  /**
   * Définit le vendeur propriétaire de l'annonce.
   *
   * @param seller vendeur
   */
  public void setSeller(User seller) {
    this.seller = seller;
  }

  /**
   * Retourne l'origine culinaire associée à l'annonce.
   *
   * @return origine culinaire
   */
  public CuisineType getCuisineType() {
    return cuisineType;
  }

  /**
   * Définit l'origine culinaire associée à l'annonce.
   *
   * @param cuisineType origine culinaire
   */
  public void setCuisineType(CuisineType cuisineType) {
    this.cuisineType = cuisineType;
  }

  /**
   * Retourne la liste des allergènes associés à cette annonce.
   *
   * @return liste d'allergènes, jamais {@code null}
   */
  public List<Allergen> getAllergens() {
    return allergens;
  }

  /**
   * Définit la liste des allergènes associés à cette annonce.
   *
   * @param allergens liste d'allergènes
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