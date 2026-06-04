package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository Spring Data JPA pour la table {@code roles}.
 *
 * <p>Génère automatiquement la requête
 * {@code SELECT * FROM roles WHERE name = ?} à partir du nom de méthode.</p>
 */
public interface RoleJpaRepository extends JpaRepository<RoleEntity, Long> {

  /**
   * Recherche une entité rôle par son nom.
   *
   * @param name le nom du rôle (enum {@link RoleName})
   * @return un {@link Optional} contenant l'entité, ou vide si absente
   */
  Optional<RoleEntity> findByName(RoleName name);
}