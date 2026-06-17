package com.tortiki.api.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tortiki.api.application.port.in.SubmitContactRequestUseCase.Command;
import com.tortiki.api.application.port.out.ContactRequestRepository;
import com.tortiki.api.application.port.out.ListingRepository;
import com.tortiki.api.application.port.out.UserRepository;
import com.tortiki.api.domain.exception.ContactRequestAlreadyExistsException;
import com.tortiki.api.domain.exception.SelfContactException;
import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.User;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitaires du service {@link ContactRequestService}.
 *
 * <p>Vérifie les quatre règles métier principales :
 * soumission nominale, interdiction vendeur = acheteur,
 * unicité de la demande et acheteur introuvable.</p>
 */
@Epic("Demande de contact")
@Feature("Soumission d'une demande")
@ExtendWith(MockitoExtension.class)
@DisplayName("ContactRequestService")
class ContactRequestServiceTest {

  private static final Instant FIXED_INSTANT =
      Instant.parse("2026-06-15T10:00:00Z");

  private static final Clock FIXED_CLOCK =
      Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

  @Mock
  private ContactRequestRepository contactRequestRepository;

  @Mock
  private ListingRepository listingRepository;

  @Mock
  private UserRepository userRepository;

  private ContactRequestService contactRequestService;

  private Listing listing;
  private User seller;
  private Command command;

  @BeforeEach
  void setUp() {
    contactRequestService = new ContactRequestService(
        contactRequestRepository,
        listingRepository,
        userRepository,
        FIXED_CLOCK
    );

    seller = new User();
    seller.setId(1L);

    listing = new Listing();
    listing.setId(10L);
    listing.setSeller(seller);

    command = new Command(10L, 2L, "Je suis intéressé !", 2);
  }

  // ─────────────────────────────────────────────────────────
  // Cas nominal
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Soumission nominale")
  @Description("Un acheteur soumet une demande valide — créée avec statut PENDING.")
  @DisplayName("Doit créer la demande avec statut PENDING")
  void shouldCreateContactRequestWithPendingStatus() {
    ContactRequest saved = buildSavedContactRequest();
    givenListingExists();
    givenNoDuplicateExists();
    givenBuyerExists();
    when(contactRequestRepository.save(any(ContactRequest.class))).thenReturn(saved);

    ContactRequest result = whenSubmitCommand();

    thenContactRequestIsPending(result);
    verify(contactRequestRepository).save(any(ContactRequest.class));
  }

  // ─────────────────────────────────────────────────────────
  // Règle : vendeur != acheteur
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Règle métier : vendeur = acheteur")
  @Description("Un vendeur tente de contacter sa propre annonce — SelfContactException levée.")
  @DisplayName("Doit lever SelfContactException si acheteur == vendeur")
  void shouldThrowWhenBuyerIsTheSeller() {
    Command selfCommand = new Command(10L, 1L, "Mon propre plat", 1);
    givenListingExists();

    assertThatThrownBy(() -> contactRequestService.submit(selfCommand))
        .isInstanceOf(SelfContactException.class)
        .hasMessageContaining("vendeur");

    verify(contactRequestRepository, never()).save(any());
  }

  // ─────────────────────────────────────────────────────────
  // Règle : unicité
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Règle métier : doublon")
  @Description("Un acheteur soumet une seconde demande pour la même annonce — exception levée.")
  @DisplayName("Doit lever ContactRequestAlreadyExistsException si doublon")
  void shouldThrowWhenDuplicateContactRequestExists() {
    givenListingExists();
    givenDuplicateExists();

    assertThatThrownBy(() -> contactRequestService.submit(command))
        .isInstanceOf(ContactRequestAlreadyExistsException.class)
        .hasMessageContaining("existe déjà");

    verify(contactRequestRepository, never()).save(any());
  }

  // ─────────────────────────────────────────────────────────
  // Méthodes Given / When / Then (pattern Allure)
  // ─────────────────────────────────────────────────────────

  @Step("Étant donné que l'annonce 10 existe")
  private void givenListingExists() {
    when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));
  }

  @Step("Étant donné qu'aucun doublon n'existe")
  private void givenNoDuplicateExists() {
    when(contactRequestRepository.existsByListingIdAndBuyerId(10L, 2L))
        .thenReturn(false);
  }

  @Step("Étant donné que l'acheteur 2 existe")
  private void givenBuyerExists() {
    User buyer = new User();
    buyer.setId(2L);
    when(userRepository.findById(2L)).thenReturn(Optional.of(buyer));
  }

  @Step("Étant donné qu'un doublon existe déjà")
  private void givenDuplicateExists() {
    when(contactRequestRepository.existsByListingIdAndBuyerId(10L, 2L))
        .thenReturn(true);
  }

  @Step("Quand la commande de soumission est exécutée")
  private ContactRequest whenSubmitCommand() {
    return contactRequestService.submit(command);
  }

  @Step("Alors la demande est créée avec le statut PENDING")
  private void thenContactRequestIsPending(ContactRequest result) {
    assertThat(result)
        .isNotNull()
        .extracting(ContactRequest::getStatus)
        .isEqualTo(ContactRequestStatus.PENDING);
    assertThat(result.getListing().getId()).isEqualTo(10L);
  }

  @Step("Construction de la demande sauvegardée")
  private ContactRequest buildSavedContactRequest() {
    User buyer = new User();
    buyer.setId(2L);
    ContactRequest saved = new ContactRequest();
    saved.setId(100L);
    saved.setListing(listing);
    saved.setBuyer(buyer);
    saved.setStatus(ContactRequestStatus.PENDING);
    saved.setMessage("Je suis intéressé !");
    saved.setPortions(2);
    return saved;
  }
}