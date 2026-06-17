package com.tortiki.api.infrastructure.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository Spring Data JPA pour la table {@code users}.
 *
 * <p>Spring Data génère automatiquement les requêtes SQL
 * à partir des noms de méthodes déclarées ici.</p>
 */
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

  /**
   * Recherche un utilisateur actif par son adresse email.
   *
   * <p>Génère : {@code SELECT * FROM users WHERE email = ? AND enabled = true}</p>
   *
   * @param email adresse email
   * @return un {@link Optional} contenant l'entité si le compte est actif
   */
  Optional<UserJpaEntity> findByEmailAndEnabledTrue(String email);

  /**
   * Recherche un utilisateur par son adresse email (tous statuts).
   *
   * <p>Génère : {@code SELECT * FROM users WHERE email = ?}</p>
   *
   * @param email adresse email
   * @return un {@link Optional} contenant l'entité, ou vide si absente
   */
  Optional<UserJpaEntity> findByEmail(String email);

  /**
   * Vérifie si une adresse email est déjà enregistrée.
   *
   * <p>Génère : {@code SELECT COUNT(*) > 0 FROM users WHERE email = ?}</p>
   *
   * @param email adresse email à vérifier
   * @return {@code true} si l'email existe déjà en base
   */
  boolean existsByEmail(String email);
}