package com.tortiki.api.infrastructure.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository JPA pour l'accès aux données des utilisateurs.
 *
 * <p>Étend {@link JpaRepository} pour bénéficier des opérations CRUD standard.
 * Les requêtes dérivées Spring Data évitent les injections SQL grâce
 * aux requêtes paramétrées générées automatiquement.</p>
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

  /**
   * Recherche un utilisateur actif par son adresse email.
   *
   * @param email l'adresse email à rechercher
   * @return un {@link Optional} contenant l'entité si trouvée
   */
  Optional<UserEntity> findByEmailAndEnabledTrue(String email);

  /**
   * Vérifie l'existence d'un compte pour une adresse email donnée.
   *
   * @param email l'adresse email à vérifier
   * @return {@code true} si un compte existe déjà pour cet email
   */
  boolean existsByEmail(String email);
}