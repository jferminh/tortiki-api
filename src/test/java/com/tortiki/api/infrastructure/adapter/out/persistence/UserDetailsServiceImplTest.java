package com.tortiki.api.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tortiki.api.application.port.out.UserRepository;
import com.tortiki.api.domain.model.Role;
import com.tortiki.api.domain.model.RoleName;
import com.tortiki.api.domain.model.User;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Tests unitaires de {@link UserDetailsServiceImpl}.
 *
 * <p>Vérifie que le pont Spring Security / domaine produit
 * un {@link UserDetails} correct pour chaque cas nominal et d'erreur.
 * Aucune base de données — {@link UserRepository} est mocké par Mockito.</p>
 */
@ExtendWith(MockitoExtension.class)
@Epic("Sécurité")
@Feature("Chargement utilisateur Spring Security")
@DisplayName("UserDetailsServiceImpl — Tests unitaires")
class UserDetailsServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserDetailsServiceImpl userDetailsService;

  /** Utilisateur actif avec le rôle SELLER — persona Sofia. */
  private User sofia;

  /** Utilisateur actif avec le rôle BUYER — persona Théo. */
  private User theo;

  /**
   * Initialisation des données communes avant chaque test.
   */
  @BeforeEach
  void setUp() {
    sofia = new User();
    sofia.setId(1L);
    sofia.setEmail("sofia@example.com");
    sofia.setPasswordHash("$2a$12$hashedPassword");
    sofia.setEnabled(true);
    sofia.addRole(new Role(1L, RoleName.SELLER));

    theo = new User();
    theo.setId(2L);
    theo.setEmail("theo@example.com");
    theo.setPasswordHash("$2a$12$anotherHash");
    theo.setEnabled(true);
    theo.addRole(new Role(2L, RoleName.BUYER));
  }

  // ─── Cas nominaux ───────────────────────────────────────────────────────

  @Test
  @Story("Connexion")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Sofia est vendeuse — loadUserByUsername retourne un UserDetails avec ROLE_SELLER.")
  @DisplayName("loadUserByUsername retourne UserDetails avec ROLE_SELLER pour un vendeur actif")
  void loadUserByUsername_shouldReturnUserDetails_withSellerRole() {
    when(userRepository.findByEmailAndEnabledTrue("sofia@example.com"))
        .thenReturn(Optional.of(sofia));

    UserDetails result = userDetailsService.loadUserByUsername("sofia@example.com");

    assertThat(result.getUsername()).isEqualTo("sofia@example.com");
    assertThat(result.getPassword()).isEqualTo("$2a$12$hashedPassword");
    assertThat(result.isEnabled()).isTrue();
    assertThat(result.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("ROLE_SELLER");

    verify(userRepository).findByEmailAndEnabledTrue("sofia@example.com");
  }

  @Test
  @Story("Connexion")
  @Severity(SeverityLevel.NORMAL)
  @Description("Théo est acheteur — loadUserByUsername retourne un UserDetails avec ROLE_BUYER.")
  @DisplayName("loadUserByUsername retourne UserDetails avec ROLE_BUYER pour un acheteur actif")
  void loadUserByUsername_shouldReturnUserDetails_withBuyerRole() {
    when(userRepository.findByEmailAndEnabledTrue("theo@example.com"))
        .thenReturn(Optional.of(theo));

    UserDetails result = userDetailsService.loadUserByUsername("theo@example.com");

    assertThat(result.getUsername()).isEqualTo("theo@example.com");
    assertThat(result.isEnabled()).isTrue();
    assertThat(result.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("ROLE_BUYER");
  }

  @Test
  @Story("Connexion")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Email inconnu ou compte inactif — UsernameNotFoundException levée.")
  @DisplayName("loadUserByUsername lève UsernameNotFoundException si email inconnu ou inactif")
  void loadUserByUsername_shouldThrowUsernameNotFoundException_whenEmailNotFound() {
    when(userRepository.findByEmailAndEnabledTrue("inconnu@example.com"))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
        () -> userDetailsService.loadUserByUsername("inconnu@example.com"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("inconnu@example.com");

    verify(userRepository).findByEmailAndEnabledTrue("inconnu@example.com");
  }

  @Test
  @Story("Connexion")
  @Severity(SeverityLevel.NORMAL)
  @Description("Compte désactivé — le UserDetails retourné a isEnabled() = false.")
  @DisplayName("loadUserByUsername retourne isEnabled=false pour un compte désactivé")
  void loadUserByUsername_shouldReturnDisabledUserDetails_whenAccountIsDisabled() {
    User compteDesactive = new User();
    compteDesactive.setId(3L);
    compteDesactive.setEmail("desactive@example.com");
    compteDesactive.setPasswordHash("$2a$12$someHash");
    compteDesactive.setEnabled(false);
    compteDesactive.addRole(new Role(1L, RoleName.BUYER));

    when(userRepository.findByEmailAndEnabledTrue("desactive@example.com"))
        .thenReturn(Optional.of(compteDesactive));

    UserDetails result = userDetailsService.loadUserByUsername("desactive@example.com");

    assertThat(result.isEnabled()).isFalse();
    assertThat(result.isAccountNonLocked()).isFalse();
  }
}