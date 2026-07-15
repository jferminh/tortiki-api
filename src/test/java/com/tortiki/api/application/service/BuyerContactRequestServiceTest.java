package com.tortiki.api.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tortiki.api.application.port.out.ContactRequestRepository;
import com.tortiki.api.application.port.out.UserRepository;
import com.tortiki.api.domain.exception.UserNotFoundException;
import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
import com.tortiki.api.domain.model.User;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitaires du service {@link BuyerContactRequestService}.
 *
 * <p>Utilise Mockito pour isoler le service de ses ports secondaires
 * {@link ContactRequestRepository} et {@link UserRepository}.
 * Pattern Given-When-Then avec étapes {@code @Step} Allure.</p>
 */
@Epic("Demande de contact")
@Feature("Historique acheteur — FindBuyerContactRequestsUseCase")
@ExtendWith(MockitoExtension.class)
@DisplayName("BuyerContactRequestService — Tests unitaires")
class BuyerContactRequestServiceTest {

  @Mock
  private ContactRequestRepository contactRequestRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private BuyerContactRequestService buyerContactRequestService;

  private static final String BUYER_EMAIL = "theo@tortiki.fr";
  private static final LocalDateTime FIXED_NOW =
      LocalDateTime.of(2026, Month.JULY, 15, 10, 0, 0);

  private User buyer;

  @BeforeEach
  void setUp() {
    buyer = new User();
    buyer.setId(2L);
    buyer.setEmail(BUYER_EMAIL);
  }

  @Step("Étant donné que l'acheteur {0} existe et est actif")
  private void givenBuyerExists(String email) {
    when(userRepository.findByEmailAndEnabledTrue(email))
        .thenReturn(Optional.of(buyer));
  }

  @Step("Étant donné que l'acheteur n'existe pas ou est inactif")
  private void givenBuyerDoesNotExist() {
    when(userRepository.findByEmailAndEnabledTrue(BUYER_EMAIL))
        .thenReturn(Optional.empty());
  }

  @Test
  @Story("Consultation nominale")
  @Description("L'acheteur a soumis deux demandes — la liste complète est retournée.")
  @DisplayName("findByBuyer retourne la liste des demandes de l'acheteur")
  void shouldReturnRequestsWhenBuyerHasContactRequests() {
    givenBuyerExists(BUYER_EMAIL);

    ContactRequest request1 = buildRequest(100L, ContactRequestStatus.PENDING);
    ContactRequest request2 = buildRequest(101L, ContactRequestStatus.CONFIRMED);

    when(contactRequestRepository.findByBuyerId(buyer.getId()))
        .thenReturn(List.of(request1, request2));

    List<ContactRequest> result = buyerContactRequestService.findByBuyer(BUYER_EMAIL);

    assertThat(result)
        .hasSize(2)
        .extracting(ContactRequest::getId)
        .containsExactly(100L, 101L);
  }

  @Test
  @Story("Consultation nominale")
  @Description("L'acheteur n'a soumis aucune demande — une liste vide est retournée.")
  @DisplayName("findByBuyer retourne une liste vide si aucune demande")
  void shouldReturnEmptyListWhenBuyerHasNoContactRequests() {
    givenBuyerExists(BUYER_EMAIL);
    when(contactRequestRepository.findByBuyerId(buyer.getId()))
        .thenReturn(List.of());

    List<ContactRequest> result = buyerContactRequestService.findByBuyer(BUYER_EMAIL);

    assertThat(result).isEmpty();
  }

  @Test
  @Story("Règle métier")
  @Description("L'email ne correspond à aucun compte actif — UserNotFoundException levée.")
  @DisplayName("findByBuyer lève UserNotFoundException si l'acheteur est introuvable")
  void shouldThrowWhenBuyerNotFound() {
    givenBuyerDoesNotExist();

    assertThatThrownBy(() -> buyerContactRequestService.findByBuyer(BUYER_EMAIL))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining(BUYER_EMAIL);

    verify(contactRequestRepository, never()).findByBuyerId(any());
  }

  private ContactRequest buildRequest(Long id, ContactRequestStatus status) {
    ContactRequest request = new ContactRequest();
    request.setId(id);
    request.setBuyer(buyer);
    request.setStatus(status);
    request.setPortions(2);
    request.setCreatedAt(FIXED_NOW);
    return request;
  }
}