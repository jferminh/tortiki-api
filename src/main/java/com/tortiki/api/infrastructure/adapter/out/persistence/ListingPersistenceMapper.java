package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.Allergen;
import com.tortiki.api.domain.model.CuisineType;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.User;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le POJO domaine {@link Listing} et l'entité JPA
 * {@link ListingJpaEntity}.
 *
 * <p>Garantit que {@link ListingJpaEntity} ne remonte jamais dans
 * les couches {@code application} ou {@code domain}.
 * Toutes les conversions sont manuelles — pas de MapStruct en v1
 * pour garder la lisibilité maximale au dossier CDA.</p>
 *
 * <p>Appartient exclusivement à la couche
 * {@code infrastructure/adapter/out/persistence/}.</p>
 */
@Component
public class ListingPersistenceMapper {

  /**
   * Convertit un POJO domaine {@link Listing} en entité JPA.
   *
   * <p>Si {@code listing.getId()} est non nul, l'entité existante
   * sera mise à jour par JPA ({@code merge}). Sinon, une nouvelle
   * entité sera insérée ({@code persist}).</p>
   *
   * @param listing le POJO domaine à convertir
   * @return l'entité JPA correspondante
   */
  public ListingJpaEntity toEntity(Listing listing) {
    ListingJpaEntity entity = new ListingJpaEntity();
    entity.setId(listing.getId());
    entity.setTitle(listing.getTitle());
    entity.setDescription(listing.getDescription());
    entity.setPrice(listing.getPrice());
    entity.setPortions(listing.getPortions());
    entity.setPhotoUrl(listing.getPhotoUrl());
    entity.setPickupAddress(listing.getPickupAddress());
    entity.setPickupLat(listing.getPickupLat());
    entity.setPickupLng(listing.getPickupLng());
    entity.setPickupDatetime(listing.getPickupDatetime());
    entity.setStatus(listing.getStatus());
    entity.setCreatedAt(listing.getCreatedAt());
    entity.setUpdatedAt(listing.getUpdatedAt());

    if (listing.getSeller() != null) {
      UserJpaEntity sellerEntity = new UserJpaEntity();
      sellerEntity.setId(listing.getSeller().getId());
      entity.setSeller(sellerEntity);
    }

    if (listing.getCuisineType() != null) {
      CuisineTypeJpaEntity ctEntity = new CuisineTypeJpaEntity();
      ctEntity.setId(listing.getCuisineType().getId());
      entity.setCuisineType(ctEntity);
    }

    if (listing.getAllergens() != null) {
      List<AllergenJpaEntity> allergenEntities = listing.getAllergens()
          .stream()
          .map(allergen -> {
            AllergenJpaEntity ae = new AllergenJpaEntity();
            ae.setId(allergen.getId());
            ae.setName(allergen.getName());
            return ae;
          })
          .toList();
      entity.setAllergens(allergenEntities);
    }

    return entity;
  }

  /**
   * Convertit une entité JPA {@link ListingJpaEntity} en POJO domaine.
   *
   * <p>Les relations {@code LAZY} ({@code seller}, {@code cuisineType},
   * {@code allergens}) sont mappées uniquement si non nulles —
   * Hibernate les charge avant appel de ce mapper grâce aux
   * {@code @Transactional} du service.</p>
   *
   * @param entity l'entité JPA à convertir
   * @return le POJO domaine correspondant
   */
  public Listing toDomain(ListingJpaEntity entity) {
    Listing listing = new Listing();
    listing.setId(entity.getId());
    listing.setTitle(entity.getTitle());
    listing.setDescription(entity.getDescription());
    listing.setPrice(entity.getPrice());
    listing.setPortions(entity.getPortions());
    listing.setPhotoUrl(entity.getPhotoUrl());
    listing.setPickupAddress(entity.getPickupAddress());
    listing.setPickupLat(entity.getPickupLat());
    listing.setPickupLng(entity.getPickupLng());
    listing.setPickupDatetime(entity.getPickupDatetime());
    listing.setStatus(entity.getStatus());
    listing.setCreatedAt(entity.getCreatedAt());
    listing.setUpdatedAt(entity.getUpdatedAt());

    if (entity.getSeller() != null) {
      User seller = new User();
      seller.setId(entity.getSeller().getId());
      seller.setEmail(entity.getSeller().getEmail());
      seller.setFirstName(entity.getSeller().getFirstName());
      seller.setLastName(entity.getSeller().getLastName());
      listing.setSeller(seller);
    }

    if (entity.getCuisineType() != null) {
      CuisineType ct = new CuisineType();
      ct.setId(entity.getCuisineType().getId());
      ct.setName(entity.getCuisineType().getName());
      listing.setCuisineType(ct);
    }

    if (entity.getAllergens() != null) {
      List<Allergen> allergens = entity.getAllergens()
          .stream()
          .map(ae -> {
            Allergen allergen = new Allergen();
            allergen.setId(ae.getId());
            allergen.setName(ae.getName());
            return allergen;
          })
          .toList();
      listing.setAllergens(allergens);
    } else {
      listing.setAllergens(new ArrayList<>());
    }

    return listing;
  }
}