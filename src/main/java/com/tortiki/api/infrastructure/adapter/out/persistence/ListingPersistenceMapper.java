package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.Allergen;
import com.tortiki.api.domain.model.CuisineType;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.User;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
 * <p>Délègue la conversion des associations {@code cuisineType} et
 * {@code allergens} en lecture ({@link #toDomain}) à
 * {@link CuisineTypePersistenceMapper} et {@link AllergenPersistenceMapper}
 * respectivement, pour éviter de dupliquer une logique déjà correcte et
 * complète ailleurs dans le projet. En écriture ({@link #toEntity}), ces
 * associations restent des références minimales ne portant que
 * l'identifiant technique : JPA n'a besoin que de la clé étrangère pour
 * persister la relation, jamais des autres colonnes de l'entité liée.</p>
 *
 * <p>Le vendeur ({@code seller}) fait exception à cette délégation : sa
 * conversion reste volontairement partielle et locale à cette classe
 * (email, prénom, nom uniquement), afin de ne jamais charger le hash de
 * mot de passe ni les rôles dans l'objet {@link User} imbriqué dans une
 * annonce — principe de minimisation des données RGPD.</p>
 *
 * <p>Appartient exclusivement à la couche
 * {@code infrastructure/adapter/out/persistence/}.</p>
 */
@Component
@RequiredArgsConstructor
public class ListingPersistenceMapper {

  private final CuisineTypePersistenceMapper cuisineTypeMapper;
  private final AllergenPersistenceMapper allergenMapper;

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
    entity.setCity(listing.getCity());
    entity.setPickupLat(listing.getPickupLat());
    entity.setPickupLng(listing.getPickupLng());
    entity.setPickupDatetime(listing.getPickupDatetime());
    entity.setStatus(listing.getStatus());
    entity.setCreatedAt(listing.getCreatedAt());
    entity.setUpdatedAt(listing.getUpdatedAt());
    entity.setSeller(mapSellerReference(listing.getSeller()));
    entity.setCuisineType(mapCuisineTypeReference(listing.getCuisineType()));
    entity.setAllergens(mapAllergenReferences(listing.getAllergens()));
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
    listing.setCity(entity.getCity());
    listing.setPickupLat(entity.getPickupLat());
    listing.setPickupLng(entity.getPickupLng());
    listing.setPickupDatetime(entity.getPickupDatetime());
    listing.setStatus(entity.getStatus());
    listing.setCreatedAt(entity.getCreatedAt());
    listing.setUpdatedAt(entity.getUpdatedAt());
    listing.setSeller(mapSellerSummary(entity.getSeller()));

    if (entity.getCuisineType() != null) {
      listing.setCuisineType(cuisineTypeMapper.toDomain(entity.getCuisineType()));
    }

    if (entity.getAllergens() != null) {
      List<Allergen> allergens = entity.getAllergens().stream()
              .map(allergenMapper::toDomain)
              .toList();
      listing.setAllergens(allergens);
    } else {
      listing.setAllergens(new ArrayList<>());
    }

    return listing;
  }

  /**
   * Construit une référence JPA minimale vers le vendeur, portant
   * uniquement son identifiant technique.
   *
   * <p>Seul l'identifiant est nécessaire pour qu'Hibernate persiste
   * la clé étrangère {@code seller_id} — aucune autre colonne de
   * {@link UserJpaEntity} n'est concernée par cette écriture.</p>
   *
   * @param seller vendeur du domaine, peut être {@code null}
   * @return référence JPA minimale, ou {@code null} si {@code seller} l'est
   */
  private UserJpaEntity mapSellerReference(User seller) {
    if (seller == null) {
      return null;
    }
    UserJpaEntity entity = new UserJpaEntity();
    entity.setId(seller.getId());
    return entity;
  }

  /**
   * Construit une référence JPA minimale vers l'origine culinaire, portant
   * uniquement son identifiant technique.
   *
   * @param cuisineType origine culinaire du domaine, peut être {@code null}
   * @return référence JPA minimale, ou {@code null} si {@code cuisineType} l'est
   */
  private CuisineTypeJpaEntity mapCuisineTypeReference(CuisineType cuisineType) {
    if (cuisineType == null) {
      return null;
    }
    CuisineTypeJpaEntity entity = new CuisineTypeJpaEntity();
    entity.setId(cuisineType.getId());
    return entity;
  }

  /**
   * Construit la liste des références JPA minimales vers les allergènes,
   * portant uniquement leur identifiant technique.
   *
   * <p>Seul l'identifiant est nécessaire pour qu'Hibernate persiste
   * les lignes de la table de jointure {@code listing_allergens}.</p>
   *
   * @param allergens allergènes du domaine, jamais {@code null} par contrat
   *     de {@link Listing#setAllergens}
   * @return liste de références JPA minimales, jamais {@code null}
   */
  private List<AllergenJpaEntity> mapAllergenReferences(List<Allergen> allergens) {
    return allergens.stream()
            .map(allergen -> {
              AllergenJpaEntity entity = new AllergenJpaEntity();
              entity.setId(allergen.getId());
              return entity;
            })
            .toList();
  }

  /**
   * Convertit l'entité JPA du vendeur en résumé domaine minimal.
   *
   * <p>Ne copie volontairement ni {@code passwordHash}, ni {@code roles},
   * ni {@code enabled}, ni les horodatages — principe de minimisation
   * des données RGPD : une annonce n'a besoin que de l'identité publique
   * de son vendeur, jamais de ses informations d'authentification.</p>
   *
   * @param entity entité JPA du vendeur, peut être {@code null}
   * @return résumé domaine minimal, ou {@code null} si {@code entity} l'est
   */
  private User mapSellerSummary(UserJpaEntity entity) {
    if (entity == null) {
      return null;
    }
    User seller = new User();
    seller.setId(entity.getId());
    seller.setEmail(entity.getEmail());
    seller.setFirstName(entity.getFirstName());
    seller.setLastName(entity.getLastName());
    return seller;
  }
}