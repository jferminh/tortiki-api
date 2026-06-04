package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.application.port.out.RoleRepository;
import com.tortiki.api.domain.model.Role;
import com.tortiki.api.domain.model.RoleName;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Adaptateur secondaire — implémentation JPA du port {@link RoleRepository}.
 *
 * <p>Fait le pont entre le contrat du domaine ({@link RoleRepository})
 * et la technologie de persistance (Spring Data JPA + PostgreSQL).
 * {@code UserService} dépend de l'interface — jamais de cette classe.</p>
 */
@Repository
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {

  /**
   * Repository Spring Data JPA délégué.
   */
  private final RoleJpaRepository roleJpaRepository;

  /**
   * Mapper domaine ↔ entité JPA.
   */
  private final RoleMapper roleMapper;

  /**
   * {@inheritDoc}
   *
   * <p>Délègue à {@link RoleJpaRepository#findByName(RoleName)}
   * et convertit l'entité en POJO domaine via {@link RoleMapper}.</p>
   */
  @Override
  public Optional<Role> findByName(RoleName name) {
    return roleJpaRepository.findByName(name)
        .map(roleMapper::toDomain);
  }
}