package com.tortiki.api.infrastructure.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository Spring Data JPA pour l'entité {@link RoleEntity}.
 *
 * <p>Étend {@link JpaRepository} pour bénéficier des opérations CRUD
 * standard. Les rôles sont insérés par Flyway V1 et ne sont jamais
 * créés à la volée par le code applicatif.</p>
 *
 * <p>Appartient à la couche {@code infrastructure} : ne doit jamais
 * être injecté hors de {@code adapter/out/persistence/}.</p>
 */
@Repository
public interface RoleJpaRepository extends JpaRepository<RoleEntity, Long> {

  /**
   * Recherche un rôle par son nom exact tel que stocké en base.
   *
   * <p>Utiliser {@code roleName.name()} pour convertir un
   * {@link com.tortiki.api.domain.model.RoleName}
   * en {@code String} avant d'appeler cette méthode.</p>
   *
   * <p>Valeurs attendues : {@code ADMIN}, {@code SELLER}, {@code BUYER}.</p>
   *
   * @param name le nom du rôle tel que stocké en base de données
   * @return un {@link Optional} contenant l'entité si elle existe
   */
  Optional<RoleEntity> findByName(String name);
}