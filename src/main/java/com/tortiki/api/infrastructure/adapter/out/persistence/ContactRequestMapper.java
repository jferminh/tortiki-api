package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.User;

/**
 * Mapper entre le POJO domaine {@link ContactRequest}
 * et l'entité JPA {@link ContactRequestEntity}.
 *
 * <p>Classe utilitaire statique — pas de bean Spring.
 * Seuls les champs nécessaires au cas d'usage sont mappés
 * (pas de chargement complet des relations).</p>
 */
final class ContactRequestMapper {

  /** Constructeur privé — classe utilitaire non instanciable. */
  private ContactRequestMapper() {}

  /**
   * Convertit un POJO domaine en entité JPA.
   *
   * <p>Les relations {@code listing} et {@code buyer} sont mappées
   * via des entités légères (id uniquement) pour éviter le chargement
   * complet des graphes d'objets.</p>
   *
   * @param domain POJO domaine à convertir
   * @return entité JPA correspondante
   */
  static ContactRequestEntity toEntity(ContactRequest domain) {
    ContactRequestEntity entity = new ContactRequestEntity();

    ListingJpaEntity listingEntity = new ListingJpaEntity();
    listingEntity.setId(domain.getListing().getId());
    entity.setListing(listingEntity);

    UserEntity buyerEntity = new UserEntity();
    buyerEntity.setId(domain.getBuyer().getId());
    entity.setBuyer(buyerEntity);

    entity.setPortions(domain.getPortions());
    entity.setStatus(domain.getStatus());
    entity.setMessage(domain.getMessage());
    return entity;
  }

  /**
   * Convertit une entité JPA en POJO domaine.
   *
   * @param entity entité JPA à convertir
   * @return POJO domaine correspondant
   */
  static ContactRequest toDomain(ContactRequestEntity entity) {
    Listing listing = new Listing();
    listing.setId(entity.getListing().getId());

    User buyer = new User();
    buyer.setId(entity.getBuyer().getId());

    ContactRequest domain = new ContactRequest();
    domain.setId(entity.getId());
    domain.setListing(listing);
    domain.setBuyer(buyer);
    domain.setPortions(entity.getPortions());
    domain.setStatus(entity.getStatus());
    domain.setMessage(entity.getMessage());
    domain.setCreatedAt(entity.getCreatedAt());
    domain.setUpdatedAt(entity.getUpdatedAt());
    return domain;
  }
}