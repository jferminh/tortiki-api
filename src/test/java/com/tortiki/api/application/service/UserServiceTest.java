package com.tortiki.api.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tortiki.api.application.port.in.RegisterUserUseCase;
import com.tortiki.api.application.port.out.RoleRepository;
import com.tortiki.api.application.port.out.UserRepository;
import com.tortiki.api.domain.exception.RoleNotFoundException;
import com.tortiki.api.domain.exception.UserAlreadyExistsException;
import com.tortiki.api.domain.exception.UserNotFoundException;
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
import org.junit.jupiter.api.Disabled;
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
@Epic("Utilisateurs")
@Feature("Gestion des comptes")
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — Tests unitaires")
@Disabled("En attente : RegisterUserUseCase.Command alignement — refs #23")
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

  /** Rôle SELLER utilisé dans les tests d'inscription. */
  private Role roleSeller;

  /** Initialisation des données communes avant chaque test. */
  @BeforeEach
  void setUp() {
    roleSeller = new Role(1L, RoleName.SELLER);
  }

  // ── REGISTER ──────────────────────────────────────────────────────────────

  @Test
  @Story("Inscription d'un vendeur")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Sofia s'inscrit avec un email disponible — compte créé, mot de passe encodé.")
  @DisplayName("register — crée et retourne l'utilisateur si l'email est disponible")
  void register_shouldCreateUser_whenEmailIsAvailable() {
    when(userRepository.existsByEmail("sofia@example.com")).thenReturn(false);
    when(roleRepository.findByName(RoleName.SELLER))
        .thenReturn(Optional.of(roleSeller));
    when(passwordEncoder.encode("motdepasse")).thenReturn("$2a$12$hash");

    User saved = new User();
    saved.setId(1L);
    saved.setEmail("sofia@example.com");
    when(userRepository.save(any(User.class))).thenReturn(saved);

    User result = userService.register(new RegisterUserUseCase.Command(
        "sofia@example.com", "motdepasse", "Sofia", "Kovalenko", RoleName.SELLER
    ));

    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getEmail()).isEqualTo("sofia@example.com");
    verify(userRepository).save(any(User.class));
    verify(passwordEncoder).encode("motdepasse");
  }

  @Test
  @Story("Inscription d'un vendeur")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Le mot de passe stocké est le hash BCrypt, jamais le mot de passe en clair.")
  @DisplayName("register — stocke le hash BCrypt et non le mot de passe en clair")
  void register_shouldStoreHashedPassword_notPlainText() {
    when(userRepository.existsByEmail("sofia@example.com")).thenReturn(false);
    when(roleRepository.findByName(RoleName.SELLER))
        .thenReturn(Optional.of(roleSeller));
    when(passwordEncoder.encode("motdepasse")).thenReturn("$2a$12$hash");
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    User result = userService.register(new RegisterUserUseCase.Command(
        "sofia@example.com", "motdepasse", "Sofia", "Kovalenko", RoleName.SELLER
    ));

    assertThat(result.getPasswordHash()).isEqualTo("$2a$12$hash");
    assertThat(result.getPasswordHash()).doesNotContain("motdepasse");
  }

  @Test
  @Story("Inscription d'un vendeur")
  @Severity(SeverityLevel.NORMAL)
  @Description("Le rôle demandé est bien attribué à l'utilisateur lors de l'inscription.")
  @DisplayName("register — attribue le rôle demandé à l'utilisateur créé")
  void register_shouldAssignRequestedRole_toNewUser() {
    when(userRepository.existsByEmail("sofia@example.com")).thenReturn(false);
    when(roleRepository.findByName(RoleName.SELLER))
        .thenReturn(Optional.of(roleSeller));
    when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hash");
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    User result = userService.register(new RegisterUserUseCase.Command(
        "sofia@example.com", "motdepasse", "Sofia", "Kovalenko", RoleName.SELLER
    ));

    assertThat(result.getRoles())
        .hasSize(1)
        .extracting(Role::getName)
        .containsExactly(RoleName.SELLER);
  }

  @Test
  @Story("Inscription d'un vendeur")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Email déjà enregistré — UserAlreadyExistsException levée, aucune persistance.")
  @DisplayName("register — lève UserAlreadyExistsException si l'email est déjà utilisé")
  void register_shouldThrowException_whenEmailAlreadyExists() {
    when(userRepository.existsByEmail("sofia@example.com")).thenReturn(true);

    assertThatThrownBy(() -> userService.register(new RegisterUserUseCase.Command(
        "sofia@example.com", "motdepasse", "Sofia", "Kovalenko", RoleName.SELLER
    )))
        .isInstanceOf(UserAlreadyExistsException.class)
        .hasMessageContaining("sofia@example.com");

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @Story("Inscription d'un vendeur")
  @Severity(SeverityLevel.NORMAL)
  @Description("Rôle absent en base (migration Flyway manquante) — RoleNotFoundException levée.")
  @DisplayName("register — lève RoleNotFoundException si le rôle est absent en base")
  void register_shouldThrowRoleNotFound_whenRoleNotInDatabase() {
    when(userRepository.existsByEmail("sofia@example.com")).thenReturn(false);
    when(roleRepository.findByName(RoleName.SELLER)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.register(new RegisterUserUseCase.Command(
        "sofia@example.com", "motdepasse", "Sofia", "Kovalenko", RoleName.SELLER
    )))
        .isInstanceOf(RoleNotFoundException.class)
        .hasMessageContaining("SELLER");

    verify(userRepository, never()).save(any(User.class));
  }

  // ── FIND BY EMAIL ─────────────────────────────────────────────────────────

  @Test
  @Story("Consultation d'un compte")
  @Severity(SeverityLevel.NORMAL)
  @Description("Recherche par email existant — utilisateur retourné.")
  @DisplayName("findByEmail — retourne l'utilisateur si l'email existe")
  void findByEmail_shouldReturnUser_whenExists() {
    User user = new User();
    user.setEmail("sofia@example.com");
    when(userRepository.findByEmail("sofia@example.com"))
        .thenReturn(Optional.of(user));

    User result = userService.findByEmail("sofia@example.com");

    assertThat(result.getEmail()).isEqualTo("sofia@example.com");
  }

  @Test
  @Story("Consultation d'un compte")
  @Severity(SeverityLevel.NORMAL)
  @Description("Email inconnu en base — UserNotFoundException levée avec l'email dans le message.")
  @DisplayName("findByEmail — lève UserNotFoundException si l'email est inconnu")
  void findByEmail_shouldThrowException_whenNotFound() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findByEmail("inconnu@example.com"))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("inconnu@example.com");
  }

  // ── FIND BY ID ────────────────────────────────────────────────────────────

  @Test
  @Story("Consultation d'un compte")
  @Severity(SeverityLevel.NORMAL)
  @Description("Recherche par identifiant existant — utilisateur retourné.")
  @DisplayName("findById — retourne l'utilisateur si l'identifiant existe")
  void findById_shouldReturnUser_whenExists() {
    User user = new User();
    user.setId(42L);
    when(userRepository.findById(42L)).thenReturn(Optional.of(user));

    User result = userService.findById(42L);

    assertThat(result.getId()).isEqualTo(42L);
  }

  @Test
  @Story("Consultation d'un compte")
  @Severity(SeverityLevel.NORMAL)
  @Description("Identifiant inconnu en base — UserNotFoundException levée avec l'id dans le message.")
  @DisplayName("findById — lève UserNotFoundException si l'identifiant est inconnu")
  void findById_shouldThrowException_whenNotFound() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.findById(99L))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("99");
  }
}