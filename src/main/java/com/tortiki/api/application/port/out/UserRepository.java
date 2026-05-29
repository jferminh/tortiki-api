package com.tortiki.api.application.port.out;

import com.tortiki.api.domain.model.User;
import java.util.Optional;

/**
 * Port secondaire — contrat de persistance des utilisateurs.
 *
 * <p>Définit ce que le domaine exige de la couche de persistance,
 * sans aucune dépendance vers JPA ou PostgreSQL. L'implémentation
 * est assurée par {@code UserRepositoryAdapter} dans
 * {@code infrastructure/adapter/out/persistence/}.</p>
 *
 * <p>Ce port est une interface du domaine : si la base de données
 * change (ex. MongoDB), seul l'adaptateur change, jamais ce contrat.</p>
 */
public interface UserRepository {

  /**
   * Persiste un nouvel utilisateur ou met à jour un utilisateur existant.
   *
   * @param user utilisateur à persister
   * @return l'utilisateur persisté avec son identifiant généré
   */
  User save(User user);

  /**
   * Recherche un utilisateur par son adresse email.
   *
   * @param email adresse email de l'utilisateur
   * @return un {@link Optional} contenant l'utilisateur, ou vide si absent
   */
  Optional<User> findByEmail(String email);

  /**
   * Recherche un utilisateur par son identifiant technique.
   *
   * @param id identifiant technique de l'utilisateur
   * @return un {@link Optional} contenant l'utilisateur, ou vide si absent
   */
  Optional<User> findById(Long id);

  /**
   * Vérifie si une adresse email est déjà utilisée.
   *
   * @param email adresse email à vérifier
   * @return {@code true} si l'email existe déjà en base
   */
  boolean existsByEmail(String email);
}