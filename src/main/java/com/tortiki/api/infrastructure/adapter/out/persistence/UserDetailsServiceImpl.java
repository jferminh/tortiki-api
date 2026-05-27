package com.tortiki.api.infrastructure.adapter.out.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implémentation de {@link UserDetailsService} pour Spring Security.
 *
 * <p>Charge les informations d'authentification d'un utilisateur
 * depuis la base de données à partir de son adresse email.</p>
 *
 * <p>Placé dans {@code infrastructure/persistence} car il dépend
 * directement de la couche JPA — il ne fait pas partie du domaine métier.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  /**
   * Charge un utilisateur par son email pour l'authentification Spring Security.
   *
   * @param email l'adresse email utilisée comme identifiant de connexion
   * @return les détails de l'utilisateur pour Spring Security
   * @throws UsernameNotFoundException si aucun compte actif n'existe pour cet email
   */
  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email)
      throws UsernameNotFoundException {

    log.debug("Chargement de l'utilisateur pour l'email : {}", email);

    UserEntity userEntity = userRepository.findByEmailAndEnabledTrue(email)
        .orElseThrow(() -> {
          log.warn("Tentative de connexion avec un email inconnu : {}", email);
          return new UsernameNotFoundException(
              "Aucun compte actif trouvé pour : " + email
          );
        });

    List<SimpleGrantedAuthority> authorities = userEntity.getRoles()
        .stream()
        .map(role -> new SimpleGrantedAuthority(role.getName()))
        .toList();

    log.debug("Utilisateur {} chargé avec les rôles : {}", email, authorities);

    return User.builder()
        .username(userEntity.getEmail())
        .password(userEntity.getPasswordHash())
        .authorities(authorities)
        .accountExpired(false)
        .accountLocked(!userEntity.isEnabled())
        .credentialsExpired(false)
        .disabled(!userEntity.isEnabled())
        .build();
  }
}