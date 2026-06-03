package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.Role;
import com.tortiki.api.domain.model.RoleName;
import com.tortiki.api.domain.model.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le modèle domaine {@link User} et l'entité JPA {@link UserEntity}.
 *
 * <p>Ce composant garantit que ni {@link User} ni {@link UserEntity} ne se
 * connaissent mutuellement : seul l'adaptateur de persistence les fait
 * cohabiter.</p>
 */
@Component
public class UserMapper {

  /**
   * Convertit un {@link UserEntity} JPA en {@link User} domaine.
   *
   * @param entity l'entité JPA à convertir
   * @return le POJO domaine correspondant
   */
  public User toDomain(UserEntity entity) {
    Set<Role> roles = entity.getRoles().stream()
        .map(re -> {
          Role role = new Role();
          role.setId(re.getId());
          role.setName(RoleName.valueOf(re.getName()));
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
    user.setUpdatedAt(entity.getUpdatedAt()); // ← nécessite Option A
    return user;
  }

  /**
   * Convertit un {@link User} domaine en {@link UserEntity} JPA.
   *
   * <p>Les rôles sont reconstruits en {@link RoleEntity} sans {@code @Lazy} :
   * on passe directement par le nom issu de {@link RoleName}.</p>
   *
   * @param user le POJO domaine à convertir
   * @return l'entité JPA correspondante
   */
  public UserEntity toEntity(User user) {
    UserEntity entity = new UserEntity();
    entity.setId(user.getId());
    entity.setEmail(user.getEmail());
    entity.setPasswordHash(user.getPasswordHash());
    entity.setFirstName(user.getFirstName());
    entity.setLastName(user.getLastName());
    entity.setEnabled(user.isEnabled());

    Set<RoleEntity> roleEntities = user.getRoles().stream()
        .map(role -> {
          RoleEntity re = new RoleEntity();
          re.setId(role.getId());
          re.setName(role.getName().name());
          return re;
        })
        .collect(Collectors.toSet());

    entity.setRoles(roleEntities);
    return entity;
  }
}