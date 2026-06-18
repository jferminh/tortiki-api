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
import com.tortiki.api.domain.exception.ListingNotFoundException;
import com.tortiki.api.domain.exception.SelfContactException;
import com.tortiki.api.domain.exception.UserNotFoundException;
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
 * <p>Vérifie les cinq règles métier : soumission nominale, annonce introuvable,
 * acheteur introuvable, interdiction vendeur = acheteur
 * et unicité de la demande.</p>
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

  private static final Long LISTING_ID = 10L;
  private static final Long SELLER_ID = 1L;
  private static final Long BUYER_ID = 2L;
  private static final String SELLER_EMAIL = "sofia@tortiki.fr";
  private static final String BUYER_EMAIL = "theo@tortiki.fr";

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
    seller.setId(SELLER_ID);
    seller.setEmail(SELLER_EMAIL);

    listing = new Listing();
    listing.setId(LISTING_ID);
    listing.setSeller(seller);

    command = new Command(LISTING_ID, BUYER_EMAIL, "Je suis intéressé !", 2);
  }

  // ─────────────────────────────────────────────────────────
  // Cas nominal
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Soumission nominale")
  @Description("Un acheteur soumet une demande valide — créée avec statut PENDING.")
  @DisplayName("Doit créer la demande avec statut PENDING")
  void shouldCreateContactRequestWithPendingStatus() {
    givenListingExists();
    givenBuyerExists();
    givenNoDuplicateExists();
    when(contactRequestRepository.save(any(ContactRequest.class)))
        .thenReturn(buildSavedContactRequest());

    ContactRequest result = whenSubmitCommand();

    thenContactRequestIsPending(result);
    verify(contactRequestRepository).save(any(ContactRequest.class));
  }

  // ─────────────────────────────────────────────────────────
  // Règle 1 : annonce introuvable
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Règle métier : annonce introuvable")
  @Description("L'annonce demandée n'existe pas — ListingNotFoundException levée.")
  @DisplayName("Doit lever ListingNotFoundException si annonce absente")
  void shouldThrowWhenListingNotFound() {
    when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> contactRequestService.submit(command))
        .isInstanceOf(ListingNotFoundException.class);

    verify(userRepository, never()).findByEmailAndEnabledTrue(any());
    verify(contactRequestRepository, never()).save(any());
  }

  // ─────────────────────────────────────────────────────────
  // Règle 2 : acheteur introuvable
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Règle métier : acheteur introuvable")
  @Description("L'acheteur est introuvable ou son compte est inactif — UserNotFoundException levée.")
  @DisplayName("Doit lever UserNotFoundException si acheteur absent ou inactif")
  void shouldThrowWhenBuyerNotFound() {
    givenListingExists();
    when(userRepository.findByEmailAndEnabledTrue(BUYER_EMAIL))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> contactRequestService.submit(command))
        .isInstanceOf(UserNotFoundException.class);

    verify(contactRequestRepository, never()).save(any());
  }

  // ─────────────────────────────────────────────────────────
  // Règle 3 : vendeur != acheteur
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Règle métier : vendeur = acheteur")
  @Description("Un vendeur tente de contacter sa propre annonce — SelfContactException levée.")
  @DisplayName("Doit lever SelfContactException si acheteur == vendeur")
  void shouldThrowWhenBuyerIsTheSeller() {
    Command selfCommand =
        new Command(LISTING_ID, SELLER_EMAIL, "Mon propre plat", 1);
    givenListingExists();
    givenSellerAsBuyer();

    assertThatThrownBy(() -> contactRequestService.submit(selfCommand))
        .isInstanceOf(SelfContactException.class);

    verify(contactRequestRepository, never()).save(any());
  }

  // ─────────────────────────────────────────────────────────
  // Règle 4 : unicité
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Règle métier : doublon")
  @Description("Un acheteur soumet une seconde demande pour la même annonce — exception levée.")
  @DisplayName("Doit lever ContactRequestAlreadyExistsException si doublon")
  void shouldThrowWhenDuplicateContactRequestExists() {
    givenListingExists();
    givenBuyerExists();
    givenDuplicateExists();

    assertThatThrownBy(() -> contactRequestService.submit(command))
        .isInstanceOf(ContactRequestAlreadyExistsException.class);

    verify(contactRequestRepository, never()).save(any());
  }

  // ─────────────────────────────────────────────────────────
  // Méthodes Given / When / Then (pattern Allure)
  // ─────────────────────────────────────────────────────────

  @Step("Étant donné que l'annonce {listingId} existe")
  private void givenListingExists() {
    when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
  }

  @Step("Étant donné que l'acheteur theo@tortiki.fr existe")
  private void givenBuyerExists() {
    User buyer = new User();
    buyer.setId(BUYER_ID);
    buyer.setEmail(BUYER_EMAIL);
    when(userRepository.findByEmailAndEnabledTrue(BUYER_EMAIL))
        .thenReturn(Optional.of(buyer));
  }

  @Step("Étant donné que le vendeur sofia@tortiki.fr se comporte en acheteur")
  private void givenSellerAsBuyer() {
    when(userRepository.findByEmailAndEnabledTrue(SELLER_EMAIL))
        .thenReturn(Optional.of(seller));
  }

  @Step("Étant donné qu'aucun doublon n'existe")
  private void givenNoDuplicateExists() {
    when(contactRequestRepository.existsByListingIdAndBuyerId(LISTING_ID, BUYER_ID))
        .thenReturn(false);
  }

  @Step("Étant donné qu'un doublon existe déjà")
  private void givenDuplicateExists() {
    when(contactRequestRepository.existsByListingIdAndBuyerId(LISTING_ID, BUYER_ID))
        .thenReturn(true);
  }

  @Step("Quand la commande de soumission est exécutée")
  private ContactRequest whenSubmitCommand() {
    return contactRequestService.submit(command);
  }

  @Step("Alors la demande est créée avec le statut PENDING")
  private void thenContactRequestIsPending(final ContactRequest result) {
    assertThat(result)
        .isNotNull()
        .extracting(ContactRequest::getStatus)
        .isEqualTo(ContactRequestStatus.PENDING);
    assertThat(result.getListing().getId()).isEqualTo(LISTING_ID);
  }

  private ContactRequest buildSavedContactRequest() {
    User buyer = new User();
    buyer.setId(BUYER_ID);
    buyer.setEmail(BUYER_EMAIL);
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