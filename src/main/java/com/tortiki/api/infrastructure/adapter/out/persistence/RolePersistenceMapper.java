package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.Role;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le modèle domaine {@link Role} et l'entité JPA {@link RoleJpaEntity}.
 *
 * <p>Garantit que {@link Role} (domaine pur) et {@link RoleJpaEntity} (JPA)
 * ne se connaissent jamais directement.</p>
 */
@Component
public class RolePersistenceMapper {

  /**
   * Convertit un {@link RoleJpaEntity} JPA en {@link Role} domaine.
   *
   * @param entity l'entité JPA à convertir
   * @return le POJO domaine correspondant
   */
  public Role toDomain(RoleJpaEntity entity) {
    Role role = new Role();
    role.setId(entity.getId());
    role.setName(entity.getName());
    return role;
  }

  /**
   * Convertit un {@link Role} domaine en {@link RoleJpaEntity} JPA.
   *
   * @param role le POJO domaine à convertir
   * @return l'entité JPA correspondante
   */
  public RoleJpaEntity toEntity(Role role) {
    RoleJpaEntity entity = new RoleJpaEntity();
    entity.setId(role.getId());
    entity.setName(role.getName());
    return entity;
  }
}