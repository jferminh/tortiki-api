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
 * Adaptateur secondaire — chargement de l'utilisateur pour Spring Security.
 *
 * <p>Implémente {@link UserDetailsService} de Spring Security en déléguant
 * la recherche à {@link UserJpaRepository}. Seuls les comptes actifs
 * ({@code enabled = true}) sont chargés — les comptes désactivés sont
 * rejetés directement en base via {@code findByEmailAndEnabledTrue}.</p>
 *
 * <p>Placée dans {@code infrastructure/adapter/out/persistence/} car elle
 * dépend de JPA et de Spring Security — deux détails techniques absents
 * du domaine et de la couche application.</p>
 *
 * <p>Spring Security auto-configure {@code DaoAuthenticationProvider}
 * à partir des beans {@link UserDetailsService} et
 * {@link org.springframework.security.crypto.password.PasswordEncoder}
 * — aucune déclaration manuelle n'est requise (setter déprécié Spring 6.4).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  /**
   * Repository JPA Spring Data — accès direct à la table {@code users}.
   */
  private final UserJpaRepository userJpaRepository;

  /**
   * Charge un utilisateur par son email pour l'authentification Spring Security.
   *
   * <p>Seuls les comptes actifs ({@code enabled = true}) sont retournés.
   * Les rôles sont mappés en {@link SimpleGrantedAuthority} avec le préfixe
   * {@code ROLE_} attendu par {@code hasRole()} dans {@code SecurityConfig}.</p>
   *
   * @param email adresse email utilisée comme identifiant de connexion
   * @return les détails de l'utilisateur pour Spring Security
   * @throws UsernameNotFoundException si aucun compte actif n'existe pour cet email
   */
  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email)
      throws UsernameNotFoundException {

    log.debug("Chargement de l'utilisateur pour l'email : {}", email);

    UserEntity userEntity = userJpaRepository.findByEmailAndEnabledTrue(email)
        .orElseThrow(() -> {
          log.warn("Tentative de connexion avec un email inconnu : {}", email);
          return new UsernameNotFoundException(
              "Aucun compte actif trouvé pour : " + email
          );
        });

    List<SimpleGrantedAuthority> authorities = userEntity.getRoles()
        .stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
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