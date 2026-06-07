package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.application.port.out.UserRepository;
import com.tortiki.api.domain.model.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptateur secondaire — chargement de l'utilisateur pour Spring Security.
 *
 * <p>Implémente {@link UserDetailsService} en déléguant la recherche
 * au port secondaire {@link UserRepository}. Seuls les comptes actifs
 * ({@code enabled = true}) sont chargés — les comptes désactivés sont
 * rejetés directement en base via {@code findByEmailAndEnabledTrue}.</p>
 *
 * <p>Placée dans {@code infrastructure/adapter/out/persistence/} car elle
 * traduit un {@link User} domaine en {@link UserDetails} Spring Security
 * — deux détails techniques absents du domaine et de la couche application.</p>
 *
 * <p>Spring Security auto-configure {@code DaoAuthenticationProvider}
 * à partir des beans {@link UserDetailsService} et
 * {@link org.springframework.security.crypto.password.PasswordEncoder}
 * — aucune déclaration manuelle n'est requise (setter déprécié Spring 6.4).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  /**
   * Port secondaire de persistance — accès à la table {@code users}.
   */
  private final UserRepository userRepository;

  /**
   * Charge un utilisateur actif par son email pour Spring Security.
   *
   * <p>Seuls les comptes avec {@code enabled = true} sont retournés.
   * Les rôles sont mappés en {@link SimpleGrantedAuthority} avec le préfixe
   * {@code ROLE_} attendu par {@code hasRole()} dans {@code SecurityConfig}.</p>
   *
   * @param email adresse email utilisée comme identifiant de connexion
   * @return les détails Spring Security de l'utilisateur
   * @throws UsernameNotFoundException si aucun compte actif n'existe pour cet email
   */
  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email)
      throws UsernameNotFoundException {
    log.debug("Chargement de l'utilisateur pour l'email : {}", email);

    User user = userRepository.findByEmailAndEnabledTrue(email)
        .orElseThrow(() -> {
          log.warn("Tentative de connexion avec un email inconnu ou inactif : {}", email);
          return new UsernameNotFoundException(
              "Aucun compte actif trouvé pour : " + email
          );
        });

    List<SimpleGrantedAuthority> authorities = user.getRoles()
        .stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
        .toList();

    log.debug("Utilisateur {} chargé avec les rôles : {}", email, authorities);

    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getEmail())
        .password(user.getPasswordHash())
        .authorities(authorities)
        .accountExpired(false)
        .accountLocked(!user.isEnabled())
        .credentialsExpired(false)
        .disabled(!user.isEnabled())
        .build();
  }
}