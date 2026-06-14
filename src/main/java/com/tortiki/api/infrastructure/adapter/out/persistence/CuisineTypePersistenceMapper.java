package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.CuisineType;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le POJO domaine {@link CuisineType} et l'entité JPA
 * {@link CuisineTypeJpaEntity}.
 *
 * <p>Appartient exclusivement à la couche
 * {@code infrastructure/adapter/out/persistence/}.</p>
 */
@Component
public class CuisineTypePersistenceMapper {

  /**
   * Convertit une entité JPA en POJO domaine.
   *
   * @param entity entité JPA source
   * @return POJO domaine {@link CuisineType}
   */
  public CuisineType toDomain(CuisineTypeJpaEntity entity) {
    CuisineType ct = new CuisineType();
    ct.setId(entity.getId());
    ct.setName(entity.getName());
    ct.setDescription(entity.getDescription());
    ct.setEnabled(entity.isEnabled());
    return ct;
  }

  /**
   * Convertit un POJO domaine en entité JPA.
   *
   * @param ct POJO domaine source
   * @return entité JPA {@link CuisineTypeJpaEntity}
   */
  public CuisineTypeJpaEntity toEntity(CuisineType ct) {
    CuisineTypeJpaEntity entity = new CuisineTypeJpaEntity();
    entity.setId(ct.getId());
    entity.setName(ct.getName());
    entity.setDescription(ct.getDescription());
    entity.setEnabled(ct.isEnabled());
    return entity;
  }
}