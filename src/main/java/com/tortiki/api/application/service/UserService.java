package com.tortiki.api.application.service;

import com.tortiki.api.application.port.in.FindUserUseCase;
import com.tortiki.api.application.port.in.RegisterUserUseCase;
import com.tortiki.api.application.port.out.RoleRepository;
import com.tortiki.api.application.port.out.UserRepository;
import com.tortiki.api.domain.exception.UserAlreadyExistsException;
import com.tortiki.api.domain.exception.UserNotFoundException;
import com.tortiki.api.domain.model.Role;
import com.tortiki.api.domain.model.RoleName;
import com.tortiki.api.domain.model.User;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier gérant l'inscription et la recherche des utilisateurs.
 *
 * <p>Implémente les ports primaires {@link RegisterUserUseCase} et
 * {@link FindUserUseCase}. Dépend uniquement des ports secondaires
 * {@link UserRepository} et {@link RoleRepository} — aucune dépendance
 * directe vers JPA ou la base de données.</p>
 *
 * <p>Le mot de passe est haché par BCrypt (force 12) avant persistance,
 * conformément aux recommandations OWASP. Le mot de passe en clair
 * n'est jamais stocké ni journalisé.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements RegisterUserUseCase, FindUserUseCase {

  /**
   * Port secondaire de persistance des utilisateurs.
   */
  private final UserRepository userRepository;

  /**
   * Port secondaire de persistance des rôles.
   */
  private final RoleRepository roleRepository;

  /**
   * Encodeur BCrypt fourni par la configuration Spring Security.
   */
  private final PasswordEncoder passwordEncoder;

  /**
   * {@inheritDoc}
   *
   * <p>Vérifie l'unicité de l'email, hache le mot de passe,
   * attribue le rôle demandé et persiste le compte.</p>
   */
  @Override
  @Transactional
  public User register(String email, String password, String firstName,
                       String lastName, RoleName role) {
    log.debug("Tentative d'inscription pour l'email : {}", email);

    if (userRepository.existsByEmail(email)) {
      throw new UserAlreadyExistsException(
          "Un compte existe déjà pour l'adresse email : " + email
      );
    }

    Role assignedRole = roleRepository.findByName(role)
        .orElseThrow(() -> new IllegalStateException(
            "Rôle introuvable en base : " + role
                + " — vérifier la migration Flyway V1"
        ));

    User user = new User();
    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode(password));
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEnabled(true);
    user.setCreatedAt(LocalDateTime.now());
    user.addRole(assignedRole);

    User saved = userRepository.save(user);
    log.info("Compte créé avec succès pour l'email : {}", email);
    return saved;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Recherche un utilisateur actif par son adresse email.</p>
   */
  @Override
  @Transactional(readOnly = true)
  public User findByEmail(String email) {
    log.debug("Recherche utilisateur par email : {}", email);
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new UserNotFoundException(
            "Aucun utilisateur trouvé pour l'email : " + email
        ));
  }

  /**
   * {@inheritDoc}
   *
   * <p>Recherche un utilisateur par son identifiant technique.</p>
   */
  @Override
  @Transactional(readOnly = true)
  public User findById(Long id) {
    log.debug("Recherche utilisateur par id : {}", id);
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(
            "Aucun utilisateur trouvé pour l'identifiant : " + id
        ));
  }
}