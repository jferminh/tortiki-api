package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.Role;
import com.tortiki.api.domain.model.RoleName;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le modèle domaine {@link Role} et l'entité JPA {@link RoleEntity}.
 *
 * <p>Garantit que {@link Role} et {@link RoleEntity} ne se connaissent
 * pas mutuellement. Seul cet adaptateur les fait cohabiter.</p>
 */
@Component
public class RoleMapper {

  /**
   * Convertit une {@link RoleEntity} JPA en {@link Role} domaine.
   *
   * @param entity l'entité JPA à convertir
   * @return le POJO domaine correspondant
   */
  public Role toDomain(RoleEntity entity) {
    Role role = new Role();
    role.setId(entity.getId());
    role.setName(RoleName.valueOf(entity.getName()));
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
    entity.setName(role.getName().name());
    return entity;
  }
}