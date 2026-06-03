package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.application.port.out.RoleRepository;
import com.tortiki.api.domain.model.Role;
import com.tortiki.api.domain.model.RoleName;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adaptateur secondaire de persistence pour les rôles.
 *
 * <p>Implémente le port {@link RoleRepository} défini dans la couche
 * {@code application}. Délègue à {@link RoleJpaRepository} et traduit
 * les entités via {@link RoleMapper}.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {

  private final RoleJpaRepository roleJpaRepository;
  private final RoleMapper roleMapper;

  /**
   * {@inheritDoc}
   *
   * <p>Convertit le {@link RoleName} en {@code String} via {@code .name()}
   * pour correspondre à la valeur stockée en base de données.</p>
   */
  @Override
  public Optional<Role> findByName(RoleName name) {
    log.debug("Recherche du rôle : {}", name);
    return roleJpaRepository.findByName(name.name())
        .map(roleMapper::toDomain);
  }
}