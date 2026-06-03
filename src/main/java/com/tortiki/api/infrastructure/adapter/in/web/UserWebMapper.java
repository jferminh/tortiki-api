package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.domain.model.Role;
import com.tortiki.api.domain.model.RoleName;
import com.tortiki.api.domain.model.User;
import com.tortiki.api.infrastructure.adapter.in.web.dto.UserResponse;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le modèle domaine {@link User} et les DTOs
 * de la couche REST {@code adapter/in/web/}.
 *
 * <p>Garantit que les DTOs HTTP ne remontent jamais dans la couche
 * {@code application} ou {@code domain}.</p>
 */
@Component
public class UserWebMapper {

  /**
   * Convertit un {@link User} domaine en {@link UserResponse} HTTP.
   *
   * <p>Le {@code passwordHash} est volontairement exclu de la réponse :
   * principe du moindre privilège OWASP.</p>
   *
   * @param user le POJO domaine à convertir
   * @return le DTO de réponse HTTP
   */
  public UserResponse toResponse(User user) {
    Set<RoleName> roles = user.getRoles().stream()
        .map(Role::getName)
        .collect(Collectors.toSet());

    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        roles
    );
  }
}