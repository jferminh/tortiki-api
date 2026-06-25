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
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitaires de {@link ManageContactRequestService}.
 *
 * <p>Vérifie les règles métier de gestion des demandes de contact :
 * consultation du tableau de bord vendeur et transitions de statut.</p>
 */
@Epic("Demande de contact")
@Feature("Gestion des demandes côté vendeur")
@ExtendWith(MockitoExtension.class)
@DisplayName("ManageContactRequestService")
class ManageContactRequestServiceTest {

  private static final Long CONTACT_REQUEST_ID = 1L;
  private static final Long SELLER_ID = 1L;
  private static final String SELLER_EMAIL = "sofia@tortiki.fr";

  @Mock
  private ContactRequestRepository contactRequestRepository;

  @Mock
  private UserRepository userRepository;

  private ManageContactRequestService service;

  private User seller;

  @BeforeEach
  void setUp() {
    service = new ManageContactRequestService(contactRequestRepository, userRepository);

    seller = new User();
    seller.setId(SELLER_ID);
    seller.setEmail(SELLER_EMAIL);
  }

  // ─────────────────────────────────────────────────────────
  // Tableau de bord vendeur
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Tableau de bord vendeur")
  @Description("Le vendeur consulte son tableau de bord — la liste de ses demandes est retournée.")
  @DisplayName("Doit retourner les demandes reçues par le vendeur")
  void shouldReturnContactRequestsForSeller() {
    ContactRequest cr = buildContactRequest(CONTACT_REQUEST_ID, ContactRequestStatus.PENDING);
    givenSellerExists();
    when(contactRequestRepository.findBySellerId(SELLER_ID)).thenReturn(List.of(cr));

    List<ContactRequest> result = whenFindBySeller();

    thenResultHasSize(result, 1);
    verify(contactRequestRepository).findBySellerId(SELLER_ID);
  }

  // ─────────────────────────────────────────────────────────
  // Confirmation
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Confirmation d'une demande")
  @Description("Le vendeur confirme une demande PENDING — statut mis à jour en CONFIRMED.")
  @DisplayName("Doit confirmer une demande PENDING → CONFIRMED")
  void shouldConfirmPendingContactRequest() {
    ContactRequest pending = buildContactRequest(CONTACT_REQUEST_ID, ContactRequestStatus.PENDING);
    ContactRequest confirmed = buildContactRequest(CONTACT_REQUEST_ID,
        ContactRequestStatus.CONFIRMED);
    UpdateStatusCommand command = new UpdateStatusCommand(
        CONTACT_REQUEST_ID, SELLER_EMAIL, ContactRequestStatus.CONFIRMED);

    givenSellerExists();
    givenContactRequestFound(pending);
    when(contactRequestRepository.updateStatus(CONTACT_REQUEST_ID,
        ContactRequestStatus.CONFIRMED)).thenReturn(confirmed);

    ContactRequest result = whenUpdateStatus(command);

    thenStatusIs(result, ContactRequestStatus.CONFIRMED);
  }

  // ─────────────────────────────────────────────────────────
  // Refus
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Refus d'une demande")
  @Description("Le vendeur refuse une demande PENDING — statut mis à jour en REFUSED.")
  @DisplayName("Doit refuser une demande PENDING → REFUSED")
  void shouldRefusePendingContactRequest() {
    ContactRequest pending = buildContactRequest(CONTACT_REQUEST_ID, ContactRequestStatus.PENDING);
    ContactRequest refused = buildContactRequest(CONTACT_REQUEST_ID,
        ContactRequestStatus.REFUSED);
    UpdateStatusCommand command = new UpdateStatusCommand(
        CONTACT_REQUEST_ID, SELLER_EMAIL, ContactRequestStatus.REFUSED);

    givenSellerExists();
    givenContactRequestFound(pending);
    when(contactRequestRepository.updateStatus(CONTACT_REQUEST_ID,
        ContactRequestStatus.REFUSED)).thenReturn(refused);

    ContactRequest result = whenUpdateStatus(command);

    thenStatusIs(result, ContactRequestStatus.REFUSED);
  }

  // ─────────────────────────────────────────────────────────
  // Règle : statut final irréversible
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Règle métier : statut final irréversible")
  @Description("Le vendeur tente de modifier un statut CONFIRMED — InvalidStatusTransitionException levée.")
  @DisplayName("Doit lever InvalidStatusTransitionException si statut déjà final")
  void shouldThrowWhenStatusAlreadyFinal() {
    ContactRequest confirmed = buildContactRequest(CONTACT_REQUEST_ID,
        ContactRequestStatus.CONFIRMED);
    UpdateStatusCommand command = new UpdateStatusCommand(
        CONTACT_REQUEST_ID, SELLER_EMAIL, ContactRequestStatus.REFUSED);

    givenSellerExists();
    givenContactRequestFound(confirmed);

    assertThatThrownBy(() -> service.updateStatus(command))
        .isInstanceOf(InvalidStatusTransitionException.class)
        .hasMessageContaining("CONFIRMED");
  }

  // ─────────────────────────────────────────────────────────
  // Règle : demande hors périmètre ou inexistante
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Règle métier : demande introuvable")
  @Description("La demande n'appartient pas au vendeur ou n'existe pas — ContactRequestNotFoundException levée.")
  @DisplayName("Doit lever ContactRequestNotFoundException si demande absente ou hors périmètre")
  void shouldThrowWhenContactRequestNotFound() {
    UpdateStatusCommand command = new UpdateStatusCommand(
        99L, SELLER_EMAIL, ContactRequestStatus.CONFIRMED);

    givenSellerExists();
    when(contactRequestRepository.findByIdAndSellerId(99L, SELLER_ID))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.updateStatus(command))
        .isInstanceOf(ContactRequestNotFoundException.class);
  }

  // ─────────────────────────────────────────────────────────
  // Méthodes Given / When / Then (pattern Allure)
  // ─────────────────────────────────────────────────────────

  @Step("Étant donné que le vendeur sofia@tortiki.fr existe")
  private void givenSellerExists() {
    when(userRepository.findByEmail(SELLER_EMAIL)).thenReturn(Optional.of(seller));
  }

  @Step("Étant donné que la demande #{contactRequestId} appartient au vendeur")
  private void givenContactRequestFound(final ContactRequest contactRequest) {
    when(contactRequestRepository.findByIdAndSellerId(
        contactRequest.getId(), SELLER_ID))
        .thenReturn(Optional.of(contactRequest));
  }

  @Step("Quand le vendeur consulte son tableau de bord")
  private List<ContactRequest> whenFindBySeller() {
    return service.findBySeller(SELLER_EMAIL);
  }

  @Step("Quand le vendeur met à jour le statut de la demande")
  private ContactRequest whenUpdateStatus(final UpdateStatusCommand command) {
    return service.updateStatus(command);
  }

  @Step("Alors la liste contient {expectedSize} demande(s)")
  private void thenResultHasSize(final List<ContactRequest> result, final int expectedSize) {
    assertThat(result).hasSize(expectedSize);
  }

  @Step("Alors le statut de la demande est {expectedStatus}")
  private void thenStatusIs(final ContactRequest result,
                            final ContactRequestStatus expectedStatus) {
    assertThat(result.getStatus()).isEqualTo(expectedStatus);
  }

  // ─────────────────────────────────────────────────────────
  // Helpers
  // ─────────────────────────────────────────────────────────

  private ContactRequest buildContactRequest(
      final Long id,
      final ContactRequestStatus status) {
    ContactRequest cr = new ContactRequest();
    cr.setId(id);
    cr.setStatus(status);
    return cr;
  }
}