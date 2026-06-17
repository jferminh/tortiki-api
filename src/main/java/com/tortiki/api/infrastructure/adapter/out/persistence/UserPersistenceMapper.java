package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.Role;
import com.tortiki.api.domain.model.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le modèle domaine {@link User} et l'entité JPA {@link UserJpaEntity}.
 *
 * <p>Ce composant garantit que ni {@link User} ni {@link UserJpaEntity} ne se
 * connaissent mutuellement : seul l'adaptateur de persistance les fait
 * cohabiter.</p>
 *
 * <p>Depuis le refactoring de {@link RoleJpaEntity}, le champ {@code name}
 * est typé {@link com.tortiki.api.domain.model.RoleName} — aucune conversion
 * {@code valueOf} / {@code .name()} n'est nécessaire dans ce mapper.</p>
 */
@Component
public class UserPersistenceMapper {

  /**
   * Convertit un {@link UserJpaEntity} JPA en {@link User} domaine.
   *
   * @param entity l'entité JPA à convertir
   * @return le POJO domaine correspondant
   */
  public User toDomain(UserJpaEntity entity) {
    Set<Role> roles = entity.getRoles().stream()
        .map(re -> {
          Role role = new Role();
          role.setId(re.getId());
          role.setName(re.getName()); // ✅ RoleName → RoleName, pas de conversion
          return role;
        })
        .collect(Collectors.toSet());

    User user = new User();
    user.setId(entity.getId());
    user.setEmail(entity.getEmail());
    user.setPasswordHash(entity.getPasswordHash());
    user.setFirstName(entity.getFirstName());
    user.setLastName(entity.getLastName());
    user.setEnabled(entity.isEnabled());
    user.setRoles(roles);
    user.setCreatedAt(entity.getCreatedAt());
    user.setUpdatedAt(entity.getUpdatedAt());
    return user;
  }

  /**
   * Convertit un {@link User} domaine en {@link UserJpaEntity} JPA.
   *
   * <p>Les rôles sont reconstruits en {@link RoleJpaEntity} à partir
   * de leur {@link com.tortiki.api.domain.model.RoleName} — le champ
   * {@code name} de {@link RoleJpaEntity} est directement compatible.</p>
   *
   * @param user le POJO domaine à convertir
   * @return l'entité JPA correspondante
   */
  public UserJpaEntity toEntity(User user) {
    UserJpaEntity entity = new UserJpaEntity();
    entity.setId(user.getId());
    entity.setEmail(user.getEmail());
    entity.setPasswordHash(user.getPasswordHash());
    entity.setFirstName(user.getFirstName());
    entity.setLastName(user.getLastName());
    entity.setEnabled(user.isEnabled());

    Set<RoleJpaEntity> roleEntities = user.getRoles().stream()
        .map(role -> {
          RoleJpaEntity re = new RoleJpaEntity();
          re.setId(role.getId());
          re.setName(role.getName()); // ✅ RoleName → RoleName, pas de .name()
          return re;
        })
        .collect(Collectors.toSet());

    entity.setRoles(roleEntities);
    return entity;
  }
}