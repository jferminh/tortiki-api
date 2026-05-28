package com.tortiki.api.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Représente une annonce de plat fait maison publiée par un vendeur.
 *
 * <p>POJO pur du domaine : aucune annotation Spring ou JPA.
 * Une annonce contient les informations métier nécessaires au parcours
 * Click &amp; Collect : description du plat, prix, portions disponibles,
 * créneau de retrait unique (v1) et photo.</p>
 *
 * <p>Contraintes métier :</p>
 * <ul>
 *   <li>Un seul créneau de retrait par annonce en v1</li>
 *   <li>Une seule photo par annonce en v1</li>
 *   <li>Le vendeur est obligatoirement un {@link User} avec le rôle
 *       {@code ROLE_SELLER}</li>
 * </ul>
 */
public class Listing {

  /**
   * Identifiant technique de l'annonce.
   */
  private Long id;

  /**
   * Titre de l'annonce, affiché dans les résultats de recherche.
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
   * Créneau de retrait unique proposé par le vendeur.
   * Format libre en v1 (ex. : "Samedi 14h-16h, Nancy centre").
   */
  private String pickupSlot;

  /**
   * URL de la photo stockée dans MinIO.
   */
  private String photoUrl;

  /**
   * Ville de retrait, utilisée pour la recherche géographique.
   */
  private String city;

  /**
   * Code postal de retrait, utilisé pour la recherche géographique.
   */
  private String postalCode;

  /**
   * Statut de l'annonce (ACTIVE, INACTIVE, MODERATED).
   */
  private ListingStatus status;

  /**
   * Date et heure de création de l'annonce.
   */
  private LocalDateTime createdAt;

  /**
   * Vendeur propriétaire de l'annonce.
   */
  private User seller;

  /**
   * Origine culinaire associée à l'annonce.
   */
  private CuisineType cuisineType;

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
   * Retourne le créneau de retrait proposé par le vendeur.
   *
   * @return créneau de retrait
   */
  public String getPickupSlot() {
    return pickupSlot;
  }

  /**
   * Définit le créneau de retrait proposé par le vendeur.
   *
   * @param pickupSlot créneau de retrait
   */
  public void setPickupSlot(String pickupSlot) {
    this.pickupSlot = pickupSlot;
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
   * Retourne la ville de retrait.
   *
   * @return ville
   */
  public String getCity() {
    return city;
  }

  /**
   * Définit la ville de retrait.
   *
   * @param city ville
   */
  public void setCity(String city) {
    this.city = city;
  }

  /**
   * Retourne le code postal de retrait.
   *
   * @return code postal
   */
  public String getPostalCode() {
    return postalCode;
  }

  /**
   * Définit le code postal de retrait.
   *
   * @param postalCode code postal
   */
  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
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

  @Override
  public String toString() {
    return "Listing{id=" + id
        + ", title='" + title + "'"
        + ", status=" + status
        + ", seller=" + (seller != null ? seller.getEmail() : "null")
        + "}";
  }
}