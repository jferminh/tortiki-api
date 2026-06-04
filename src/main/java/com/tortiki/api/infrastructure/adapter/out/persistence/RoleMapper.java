package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.Role;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le modèle domaine {@link Role} et l'entité JPA {@link RoleEntity}.
 *
 * <p>Garantit que {@link Role} (domaine pur) et {@link RoleEntity} (JPA)
 * ne se connaissent jamais directement.</p>
 */
@Component
public class RoleMapper {

  /**
   * Convertit un {@link RoleEntity} JPA en {@link Role} domaine.
   *
   * @param entity l'entité JPA à convertir
   * @return le POJO domaine correspondant
   */
  public Role toDomain(RoleEntity entity) {
    Role role = new Role();
    role.setId(entity.getId());
    role.setName(entity.getName());
    return role;
  }

  /**
   * Convertit un {@link Role} domaine en {@link RoleEntity} JPA.
   *
   * @param role le POJO domaine à convertir
   * @return l'entité JPA correspondante
   */
  public RoleEntity toEntity(Role role) {
    RoleEntity entity = new RoleEntity();
    entity.setId(role.getId());
    entity.setName(role.getName());
    return entity;
  }
}