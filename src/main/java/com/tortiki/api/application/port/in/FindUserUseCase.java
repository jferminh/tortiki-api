package com.tortiki.api.application.port.in;

import com.tortiki.api.domain.model.User;

/**
 * Port primaire — cas d'usage : recherche d'un utilisateur.
 *
 * <p>Définit le contrat d'entrée pour la consultation d'un profil
 * utilisateur. Utilisé par {@code UserDetailsServiceImpl} pour
 * l'authentification Spring Security et par les contrôleurs REST.</p>
 */
public interface FindUserUseCase {

  /**
   * Recherche un utilisateur par son adresse email.
   *
   * @param email adresse email de l'utilisateur
   * @return l'utilisateur correspondant
   * @throws com.tortiki.api.domain.exception.UserNotFoundException si aucun utilisateur
   *      actif n'existe pour cet email
   */
  User findByEmail(String email);

  /**
   * Recherche un utilisateur par son identifiant technique.
   *
   * @param id identifiant technique de l'utilisateur
   * @return l'utilisateur correspondant
   * @throws com.tortiki.api.domain.exception.UserNotFoundException si aucun utilisateur
   *      n'existe pour cet identifiant
   */
  User findById(Long id);
}