// src/test/java/com/tortiki/api/application/service/ManageContactRequestServiceTest.java
package com.tortiki.api.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tortiki.api.application.port.in.ManageContactRequestUseCase.UpdateStatusCommand;
import com.tortiki.api.application.port.out.ContactRequestRepository;
import com.tortiki.api.application.port.out.UserRepository;
import com.tortiki.api.domain.exception.ContactRequestNotFoundException;
import com.tortiki.api.domain.exception.InvalidStatusTransitionException;
import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
import com.tortiki.api.domain.model.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitaires de {@link ManageContactRequestService}.
 *
 * <p>Vérifie les règles métier de gestion des demandes de contact :
 * consultation du tableau de bord vendeur et transitions de statut.</p>
 */
@ExtendWith(MockitoExtension.class)
class ManageContactRequestServiceTest {

  @Mock
  private ContactRequestRepository contactRequestRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private ManageContactRequestService service;

  @Test
  @DisplayName("findBySeller : retourne les demandes du vendeur")
  void findBySeller_nominal() {
    User seller = buildUser(1L, "sofia@tortiki.fr");
    ContactRequest cr = buildContactRequest(1L, ContactRequestStatus.PENDING, seller);

    when(userRepository.findByEmail("sofia@tortiki.fr")).thenReturn(Optional.of(seller));
    when(contactRequestRepository.findBySellerId(1L)).thenReturn(List.of(cr));

    List<ContactRequest> result = service.findBySeller("sofia@tortiki.fr");

    assertThat(result).hasSize(1);
    verify(contactRequestRepository).findBySellerId(1L);
  }

  @Test
  @DisplayName("updateStatus : PENDING → CONFIRMED nominal")
  void updateStatus_pendingToConfirmed() {
    User seller = buildUser(1L, "sofia@tortiki.fr");
    ContactRequest pending = buildContactRequest(1L, ContactRequestStatus.PENDING, seller);
    ContactRequest confirmed = buildContactRequest(1L, ContactRequestStatus.CONFIRMED, seller);
    UpdateStatusCommand command = new UpdateStatusCommand(1L, "sofia@tortiki.fr",
        ContactRequestStatus.CONFIRMED);

    when(userRepository.findByEmail("sofia@tortiki.fr")).thenReturn(Optional.of(seller));
    when(contactRequestRepository.findByIdAndSellerId(1L, 1L))
        .thenReturn(Optional.of(pending));
    when(contactRequestRepository.updateStatus(1L, ContactRequestStatus.CONFIRMED))
        .thenReturn(confirmed);

    ContactRequest result = service.updateStatus(command);

    assertThat(result.getStatus()).isEqualTo(ContactRequestStatus.CONFIRMED);
  }

  @Test
  @DisplayName("updateStatus : PENDING → REFUSED nominal")
  void updateStatus_pendingToRefused() {
    User seller = buildUser(1L, "sofia@tortiki.fr");
    ContactRequest pending = buildContactRequest(1L, ContactRequestStatus.PENDING, seller);
    ContactRequest refused = buildContactRequest(1L, ContactRequestStatus.REFUSED, seller);
    UpdateStatusCommand command = new UpdateStatusCommand(1L, "sofia@tortiki.fr",
        ContactRequestStatus.REFUSED);

    when(userRepository.findByEmail("sofia@tortiki.fr")).thenReturn(Optional.of(seller));
    when(contactRequestRepository.findByIdAndSellerId(1L, 1L))
        .thenReturn(Optional.of(pending));
    when(contactRequestRepository.updateStatus(1L, ContactRequestStatus.REFUSED))
        .thenReturn(refused);

    ContactRequest result = service.updateStatus(command);

    assertThat(result.getStatus()).isEqualTo(ContactRequestStatus.REFUSED);
  }

  @Test
  @DisplayName("updateStatus : statut final CONFIRMED → exception InvalidStatusTransition")
  void updateStatus_fromConfirmed_throws() {
    User seller = buildUser(1L, "sofia@tortiki.fr");
    ContactRequest confirmed = buildContactRequest(1L, ContactRequestStatus.CONFIRMED, seller);
    UpdateStatusCommand command = new UpdateStatusCommand(1L, "sofia@tortiki.fr",
        ContactRequestStatus.REFUSED);

    when(userRepository.findByEmail("sofia@tortiki.fr")).thenReturn(Optional.of(seller));
    when(contactRequestRepository.findByIdAndSellerId(1L, 1L))
        .thenReturn(Optional.of(confirmed));

    assertThatThrownBy(() -> service.updateStatus(command))
        .isInstanceOf(InvalidStatusTransitionException.class)
        .hasMessageContaining("CONFIRMED");
  }

  @Test
  @DisplayName("updateStatus : demande inexistante ou hors périmètre vendeur → ContactRequestNotFoundException")
  void updateStatus_notFound_throws() {
    User seller = buildUser(1L, "sofia@tortiki.fr");
    UpdateStatusCommand command = new UpdateStatusCommand(99L, "sofia@tortiki.fr",
        ContactRequestStatus.CONFIRMED);

    when(userRepository.findByEmail("sofia@tortiki.fr")).thenReturn(Optional.of(seller));
    when(contactRequestRepository.findByIdAndSellerId(99L, 1L))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.updateStatus(command))
        .isInstanceOf(ContactRequestNotFoundException.class);
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  private User buildUser(Long id, String email) {
    User user = new User();
    user.setId(id);
    user.setEmail(email);
    return user;
  }

  private ContactRequest buildContactRequest(
      Long id,
      ContactRequestStatus status,
      User seller) {
    ContactRequest cr = new ContactRequest();
    cr.setId(id);
    cr.setStatus(status);
    return cr;
  }
}