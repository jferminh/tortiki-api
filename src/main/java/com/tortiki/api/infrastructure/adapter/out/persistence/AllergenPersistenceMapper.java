package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.Allergen;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le POJO domaine {@link Allergen} et l'entité JPA
 * {@link AllergenJpaEntity}.
 *
 * <p>Appartient exclusivement à la couche
 * {@code infrastructure/adapter/out/persistence/}.</p>
 */
@Component
public class AllergenPersistenceMapper {

  /**
   * Convertit une entité JPA {@link AllergenJpaEntity} en POJO domaine.
   *
   * @param entity entité JPA source
   * @return POJO domaine {@link Allergen}
   */
  public Allergen toDomain(AllergenJpaEntity entity) {
    Allergen allergen = new Allergen();
    allergen.setId(entity.getId());
    allergen.setName(entity.getName());
    allergen.setEnabled(entity.isEnabled());
    return allergen;
  }

  /**
   * Convertit un POJO domaine {@link Allergen} en entité JPA.
   *
   * @param allergen POJO domaine source
   * @return entité JPA {@link AllergenJpaEntity}
   */
  public AllergenJpaEntity toEntity(Allergen allergen) {
    AllergenJpaEntity entity = new AllergenJpaEntity();
    entity.setId(allergen.getId());
    entity.setName(allergen.getName());
    entity.setEnabled(allergen.isEnabled());
    return entity;
  }
}