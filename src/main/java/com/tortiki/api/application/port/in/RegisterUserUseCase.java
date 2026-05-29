package com.tortiki.api.application.port.in;

import com.tortiki.api.domain.model.RoleName;
import com.tortiki.api.domain.model.User;

/**
 * Port primaire — cas d'usage : inscription d'un nouvel utilisateur.
 *
 * <p>Définit le contrat d'entrée pour la création d'un compte sur la
 * plateforme Tortiki. L'implémentation est assurée par
 * {@code UserService} dans la couche {@code application/service/}.</p>
 *
 * <p>Ce port est appelé par l'adaptateur entrant
 * {@code AuthController} dans {@code infrastructure/adapter/in/web/}.</p>
 */
public interface RegisterUserUseCase {

  /**
   * Inscrit un nouvel utilisateur sur la plateforme.
   *
   * <p>Le mot de passe est haché par BCrypt (force 12) avant persistance.
   * Un compte est créé avec le rôle spécifié et activé immédiatement.</p>
   *
   * @param email     adresse email unique de l'utilisateur
   * @param password  mot de passe en clair (haché avant persistance)
   * @param firstName prénom de l'utilisateur
   * @param lastName  nom de famille de l'utilisateur
   * @param role      rôle initial attribué ({@code SELLER} ou {@code BUYER})
   * @return l'utilisateur créé avec son identifiant technique
   * @throws com.tortiki.api.domain.exception.UserAlreadyExistsException si l'email est déjà utilisé
   */
  User register(String email, String password, String firstName,
                String lastName, RoleName role);
}