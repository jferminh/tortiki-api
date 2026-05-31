package com.tortiki.api.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tortiki.api.application.port.out.RoleRepository;
import com.tortiki.api.application.port.out.UserRepository;
import com.tortiki.api.domain.exception.UserAlreadyExistsException;
import com.tortiki.api.domain.exception.UserNotFoundException;
import com.tortiki.api.domain.model.Role;
import com.tortiki.api.domain.model.RoleName;
import com.tortiki.api.domain.model.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Tests unitaires du service {@link UserService}.
 *
 * <p>Aucune base de données — les ports secondaires sont mockés
 * par Mockito. Chaque test couvre un cas nominal ou un cas d'erreur.</p>
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

  /**
   * Rôle SELLER utilisé dans les tests d'inscription.
   */
  private Role roleSeller;

  /**
   * Initialisation des données communes avant chaque test.
   */
  @BeforeEach
  void setUp() {
    roleSeller = new Role(1L, RoleName.SELLER);
  }

  // -------------------------------------------------------------------------
  // register()
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("register : cas nominal — crée et retourne l'utilisateur")
  void register_shouldCreateUser_whenEmailIsAvailable() {
    when(userRepository.existsByEmail("sofia@example.com")).thenReturn(false);
    when(roleRepository.findByName(RoleName.SELLER))
        .thenReturn(Optional.of(roleSeller));
    when(passwordEncoder.encode("motdepasse")).thenReturn("$2a$12$hash");

    User saved = new User();
    saved.setId(1L);
    saved.setEmail("sofia@example.com");
    when(userRepository.save(any(User.class))).thenReturn(saved);

    User result = userService.register(
        "sofia@example.com", "motdepasse", "Sofia", "Kovalenko", RoleName.SELLER
    );

    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getEmail()).isEqualTo("sofia@example.com");
    verify(userRepository).save(any(User.class));
    verify(passwordEncoder).encode("motdepasse");
  }

  @Test
  @DisplayName("register : email déjà utilisé — lève UserAlreadyExistsException")
  void register_shouldThrowException_whenEmailAlreadyExists() {
    when(userRepository.existsByEmail("sofia@example.com")).thenReturn(true);

    assertThatThrownBy(() -> userService.register(
        "sofia@example.com", "motdepasse", "Sofia", "Kovalenko", RoleName.SELLER
    ))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("sofia@example.com");

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("register : rôle introuvable en base — lève IllegalStateException")
  void register_shouldThrowIllegalState_whenRoleNotFound() {
    when(userRepository.existsByEmail("sofia@example.com")).thenReturn(false);
    when(roleRepository.findByName(RoleName.SELLER)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.register(
        "sofia@example.com", "motdepasse", "Sofia", "Kovalenko", RoleName.SELLER
    ))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Flyway");

    verify(userRepository, never()).save(any(User.class));
  }

  // -------------------------------------------------------------------------
  // findByEmail()
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("findByEmail : cas nominal — retourne l'utilisateur")
  void findByEmail_shouldReturnUser_whenExists() {
    User user = new User();
    user.setEmail("sofia@example.com");
    when(userRepository.findByEmail("sofia@example.com"))
        .thenReturn(Optional.of(user));

    User result = userService.findByEmail("sofia@example.com");

    assertThat(result.getEmail()).isEqualTo("sofia@example.com");
  }

  @Test
  @DisplayName("findByEmail : utilisateur absent — lève UserNotFoundException")
  void findByEmail_shouldThrowException_whenNotFound() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findByEmail("inconnu@example.com"))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("inconnu@example.com");
  }

  // -------------------------------------------------------------------------
  // findById()
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("findById : cas nominal — retourne l'utilisateur")
  void findById_shouldReturnUser_whenExists() {
    User user = new User();
    user.setId(42L);
    when(userRepository.findById(42L)).thenReturn(Optional.of(user));

    User result = userService.findById(42L);

    assertThat(result.getId()).isEqualTo(42L);
  }

  @Test
  @DisplayName("findById : identifiant absent — lève UserNotFoundException")
  void findById_shouldThrowException_whenNotFound() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findById(99L))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("99");
  }
}