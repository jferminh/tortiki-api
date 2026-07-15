package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.User;

/**
 * Mapper entre le POJO domaine {@link ContactRequest}
 * et l'entité JPA {@link ContactRequestJpaEntity}.
 *
 * <p>Classe utilitaire statique — pas de bean Spring.
 * Seuls les champs nécessaires au cas d'usage sont mappés
 * (pas de chargement complet des relations).</p>
 */
final class ContactRequestPersistenceMapper {

  /** Constructeur privé — classe utilitaire non instanciable. */
  private ContactRequestPersistenceMapper() {}

  /**
   * Convertit un POJO domaine en entité JPA.
   *
   * <p>Les relations {@code listing} et {@code buyer} sont mappées
   * via des entités légères (id uniquement) pour éviter le chargement
   * complet des graphes d'objets.</p>
   *
   * <p>{@code createdAt} et {@code updatedAt} sont intentionnellement
   * omis — initialisés automatiquement par {@code @PrePersist}
   * et {@code @PreUpdate} de {@link ContactRequestJpaEntity}.</p>
   *
   * @param domain POJO domaine à convertir
   * @return entité JPA correspondante
   */
  static ContactRequestJpaEntity toEntity(final ContactRequest domain) {
    ContactRequestJpaEntity entity = new ContactRequestJpaEntity();

    ListingJpaEntity listingEntity = new ListingJpaEntity();
    listingEntity.setId(domain.getListing().getId());
    entity.setListing(listingEntity);

    UserJpaEntity buyerEntity = new UserJpaEntity();
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
  static ContactRequest toDomain(final ContactRequestJpaEntity entity) {
    Listing listing = null;
    if (entity.getListing() != null) {
      listing = new Listing();
      listing.setId(entity.getListing().getId());
      listing.setTitle(entity.getListing().getTitle());
    }

    User buyer = null;
    if (entity.getBuyer() != null) {
      buyer = new User();
      buyer.setId(entity.getBuyer().getId());
      buyer.setFirstName(entity.getBuyer().getFirstName());
    }

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