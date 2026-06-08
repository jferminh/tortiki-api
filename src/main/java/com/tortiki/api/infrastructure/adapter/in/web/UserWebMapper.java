package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.domain.model.Role;
import com.tortiki.api.domain.model.RoleName;
import com.tortiki.api.domain.model.User;
import com.tortiki.api.infrastructure.adapter.in.web.dto.UserResponse;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
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

  /**
   * Construit un {@link UserResponse} depuis un {@link UserDetails} Spring Security.
   *
   * <p>Utilisé après l'authentification pour éviter un second appel en base :
   * le {@code UserDetails} est déjà chargé par {@code UserDetailsServiceImpl}
   * lors de l'appel à {@code AuthenticationManager.authenticate()}.</p>
   *
   * <p>L'identifiant ({@code id}) et les noms ({@code firstName}, {@code lastName})
   * ne sont pas portés par {@code UserDetails} — ils sont retournés comme {@code null}
   * dans cette surcharge légère. Pour une réponse complète, privilégier
   * {@link #toResponse(User)}.</p>
   *
   * @param userDetails le principal Spring Security authentifié
   * @return le DTO de réponse HTTP minimal
   */
  public UserResponse toResponse(UserDetails userDetails) {
    Set<RoleName> roles = userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .map(authority -> authority.replace("ROLE_", ""))
        .map(RoleName::valueOf)
        .collect(Collectors.toSet());

    return new UserResponse(
        null,
        userDetails.getUsername(),
        null,
        null,
        roles
    );
  }
}