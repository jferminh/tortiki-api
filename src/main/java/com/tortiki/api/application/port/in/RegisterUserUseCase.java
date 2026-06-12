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
   * @param command données d'inscription de l'utilisateur
   * @return l'utilisateur créé avec son identifiant technique
   * @throws com.tortiki.api.domain.exception.UserAlreadyExistsException
   *         si l'adresse email est déjà utilisée
   */
  User register(RegisterUserUseCase.Command command);

  /**
   * Commande d'entrée pour l'inscription d'un nouvel utilisateur.
   *
   * <p>Record immuable Java 21 — garantit qu'aucune donnée n'est modifiée
   * entre le contrôleur et le service. Évite l'inversion accidentelle
   * des paramètres {@code firstName} / {@code lastName}.</p>
   *
   * @param email     adresse email unique (identifiant de connexion)
   * @param password  mot de passe en clair (haché avant persistance BCrypt force 12)
   * @param firstName prénom de l'utilisateur
   * @param lastName  nom de famille de l'utilisateur
   * @param role      rôle initial attribué ({@code SELLER} ou {@code BUYER})
   */
  record Command(
      String email,
      String password,
      String firstName,
      String lastName,
      RoleName role
  ) {}
}