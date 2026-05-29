package com.tortiki.api.application.port.out;

import com.tortiki.api.domain.model.Role;
import com.tortiki.api.domain.model.RoleName;
import java.util.Optional;

/**
 * Port secondaire — contrat de persistance des rôles.
 *
 * <p>Utilisé par {@code UserService} pour récupérer un rôle existant
 * depuis le référentiel avant de l'attribuer à un utilisateur.
 * Les rôles sont insérés en base par Flyway ({@code V1__init_schema.sql})
 * et ne sont jamais créés à la volée par le code applicatif.</p>
 */
public interface RoleRepository {

  /**
   * Recherche un rôle par son nom.
   *
   * @param name nom du rôle ({@code ADMIN}, {@code SELLER}, {@code BUYER})
   * @return un {@link Optional} contenant le rôle, ou vide si absent
   */
  Optional<Role> findByName(RoleName name);
}