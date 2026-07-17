package com.tortiki.api.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tortiki.api.application.port.in.ManageListingUseCase;
import com.tortiki.api.application.port.out.AllergenRepository;
import com.tortiki.api.application.port.out.CuisineTypeRepository;
import com.tortiki.api.application.port.out.GeolocationPort;
import com.tortiki.api.application.port.out.ListingRepository;
import com.tortiki.api.application.port.out.StoragePort;
import com.tortiki.api.application.port.out.UserRepository;
import com.tortiki.api.domain.exception.CuisineTypeNotFoundException;
import com.tortiki.api.domain.exception.ListingNotFoundException;
import com.tortiki.api.domain.exception.UnauthorizedActionException;
import com.tortiki.api.domain.exception.UserNotFoundException;
import com.tortiki.api.domain.model.CuisineType;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.ListingStatus;
import com.tortiki.api.domain.model.User;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.math.BigDecimal;
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
 * Tests unitaires de {@link ListingService}.
 *
 * <p>Vérifie la logique métier des annonces : création, modification,
 * suppression logique, règle de propriété et changement de statut.
 * Aucune dépendance à la base de données (Mockito pur).</p>
 */
@Epic("Annonces")
@Feature("Gestion des annonces")
@Owner("Tortiki")
@ExtendWith(MockitoExtension.class)
@DisplayName("ListingService — Tests unitaires")
class ListingServiceTest {

  @Mock
  private ListingRepository listingRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private CuisineTypeRepository cuisineTypeRepository;

  @Mock
  private AllergenRepository allergenRepository;

  @Mock
  private StoragePort storagePort;

  @Mock
  private GeolocationPort geolocationPort;

  @InjectMocks
  private ListingService listingService;

  private User sofia;
  private CuisineType ukrainienne;
  private Listing listing;
  private ManageListingUseCase.Command command;

  /** Initialiser les fixtures partagées entre les tests. */
  @BeforeEach
  void setUp() {
    sofia = new User();
    sofia.setId(1L);
    sofia.setEmail("sofia@example.com");

    ukrainienne = new CuisineType();
    ukrainienne.setId(10L);
    ukrainienne.setName("Ukrainienne");

    listing = new Listing();
    listing.setId(100L);
    listing.setSeller(sofia);
    listing.setTitle("Bortsch maison");
    listing.setStatus(ListingStatus.ACTIVE);
    listing.setPickupAddress("12 rue des Acacias, 67000 Strasbourg");

    command = new ManageListingUseCase.Command(
        "Bortsch maison",
        "Soupe ukrainienne traditionnelle",
        new BigDecimal("8.50"),
        4,
        "12 rue des Acacias, 67000 Strasbourg",
        LocalDateTime.of(2026, Month.JUNE, 21, 12, 0),
        10L,
        List.of()
    );
  }

  // ── CREATE ────────────────────────────────────────────────────────────────

  @Test
  @Story("Création d'une annonce")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Sofia crée une annonce — vendeur et origine culinaire résolus, annonce persistée.")
  @DisplayName("create — crée et retourne une annonce avec statut ACTIVE")
  void create_shouldReturnSavedListing_whenSellerAndCuisineTypeExist() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(sofia));
    when(cuisineTypeRepository.findById(10L)).thenReturn(Optional.of(ukrainienne));
    when(allergenRepository.findAllByIdIn(List.of())).thenReturn(List.of());
    when(geolocationPort.geocode(anyString()))
        .thenReturn(Optional.of(new GeolocationPort.Coordinates(48.57, 7.75)));
    when(listingRepository.save(any(Listing.class))).thenReturn(listing);

    Listing result = listingService.create(1L, command);

    assertThat(result.getId()).isEqualTo(100L);
    assertThat(result.getStatus()).isEqualTo(ListingStatus.ACTIVE);
    verify(listingRepository).save(any(Listing.class));
  }

  @Test
  @Story("Création d'une annonce")
  @Severity(SeverityLevel.NORMAL)
  @Description("Nominatim indisponible — annonce créée sans coordonnées, pas d'exception.")
  @DisplayName("create — crée l'annonce sans coordonnées si Nominatim échoue")
  void create_shouldSaveListing_whenGeolocationReturnsEmpty() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(sofia));
    when(cuisineTypeRepository.findById(10L)).thenReturn(Optional.of(ukrainienne));
    when(allergenRepository.findAllByIdIn(List.of())).thenReturn(List.of());
    when(geolocationPort.geocode(anyString())).thenReturn(Optional.empty());
    when(listingRepository.save(any(Listing.class))).thenReturn(listing);

    Listing result = listingService.create(1L, command);

    assertThat(result).isNotNull();
    verify(listingRepository).save(any(Listing.class));
  }

  @Test
  @Story("Création d'une annonce")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Vendeur introuvable lors de la création — UserNotFoundException levée.")
  @DisplayName("create — lève UserNotFoundException si le vendeur est inconnu")
  void create_shouldThrowUserNotFoundException_whenSellerNotFound() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> listingService.create(99L, command))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("99");
  }

  @Test
  @Story("Création d'une annonce")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Origine culinaire introuvable lors de la création — CuisineTypeNotFoundException.")
  @DisplayName("create — lève CuisineTypeNotFoundException si l'origine culinaire est inconnue")
  void create_shouldThrowCuisineTypeNotFoundException_whenCuisineTypeNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(sofia));
    when(cuisineTypeRepository.findById(10L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> listingService.create(1L, command))
        .isInstanceOf(CuisineTypeNotFoundException.class)
        .hasMessageContaining("10");
  }

  // ── UPDATE ────────────────────────────────────────────────────────────────

  @Test
  @Story("Modification d'une annonce")
  @Severity(SeverityLevel.NORMAL)
  @Description("Sofia modifie son annonce avec une nouvelle adresse — Nominatim appelé.")
  @DisplayName("update — met à jour une annonce et re-géocode si l'adresse change")
  void update_shouldUpdateListing_whenSellerIsOwner() {
    final ManageListingUseCase.Command updatedCommand = new ManageListingUseCase.Command(
        "Bortsch maison — édition été",
        "Recette estivale",
        new BigDecimal("9.50"),
        4,
        "2 Allée Lys Rouge, 54000 Nancy",
        LocalDateTime.of(2026, Month.JULY, 15, 12, 0),
        10L,
        List.of()
    );
    when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));
    when(allergenRepository.findAllByIdIn(List.of())).thenReturn(List.of());
    when(geolocationPort.geocode(anyString()))
        .thenReturn(Optional.of(new GeolocationPort.Coordinates(48.69, 6.18)));
    when(listingRepository.save(any(Listing.class))).thenReturn(listing);

    Listing result = listingService.update(100L, 1L, updatedCommand);

    assertThat(result).isNotNull();
    verify(geolocationPort).geocode("2 Allée Lys Rouge, 54000 Nancy");
    verify(listingRepository).save(any(Listing.class));
  }

  @Test
  @Story("Modification d'une annonce")
  @Severity(SeverityLevel.NORMAL)
  @Description("Sofia modifie son annonce sans changer l'adresse — Nominatim non appelé.")
  @DisplayName("update — ne re-géocode pas si l'adresse est identique")
  void update_shouldNotCallGeolocation_whenAddressUnchanged() {
    when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));
    when(allergenRepository.findAllByIdIn(List.of())).thenReturn(List.of());
    when(listingRepository.save(any(Listing.class))).thenReturn(listing);

    Listing result = listingService.update(100L, 1L, command);

    assertThat(result).isNotNull();
    verify(geolocationPort, never()).geocode(anyString());
    verify(listingRepository).save(any(Listing.class));
  }

  @Test
  @Story("Modification d'une annonce")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Un vendeur tiers tente de modifier l'annonce — UnauthorizedActionException levée.")
  @DisplayName("update — lève UnauthorizedActionException si le vendeur n'est pas propriétaire")
  void update_shouldThrowUnauthorizedActionException_whenSellerIsNotOwner() {
    when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));

    assertThatThrownBy(() -> listingService.update(100L, 999L, command))
        .isInstanceOf(UnauthorizedActionException.class)
        .hasMessageContaining("999");
  }

  @Test
  @Story("Modification d'une annonce")
  @Severity(SeverityLevel.NORMAL)
  @Description("Annonce introuvable lors de la modification — ListingNotFoundException levée.")
  @DisplayName("update — lève ListingNotFoundException si l'annonce est inconnue")
  void update_shouldThrowListingNotFoundException_whenListingNotFound() {
    when(listingRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> listingService.update(99L, 1L, command))
        .isInstanceOf(ListingNotFoundException.class)
        .hasMessageContaining("99");
  }

  // ── UPDATE PHOTO ──────────────────────────────────────────────────────────

  @Test
  @Story("Modification d'une annonce")
  @Severity(SeverityLevel.MINOR)
  @Description("Sofia upload la photo de son annonce — StoragePort appelé, URL persistée.")
  @DisplayName("updatePhoto — upload la photo via StoragePort et persiste l'URL")
  void updatePhoto_shouldUploadAndPersistPhotoUrl_whenSellerIsOwner() {
    final byte[] photoBytes = "photo".getBytes();
    final String photoUrl = "http://localhost:9000/tortiki-photos/uuid-listing-100.jpg";
    final ManageListingUseCase.PhotoCommand photoCommand =
        new ManageListingUseCase.PhotoCommand(photoBytes, "image/jpeg", "uuid-listing-100.jpg");

    when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));
    when(storagePort.upload(
        any(String.class), any(byte[].class), any(String.class)))
        .thenReturn(photoUrl);
    when(listingRepository.save(any(Listing.class))).thenReturn(listing);

    Listing result = listingService.updatePhoto(100L, 1L, photoCommand);

    assertThat(result).isNotNull();
    verify(storagePort).upload(
        any(String.class), any(byte[].class), any(String.class));
    verify(listingRepository).save(any(Listing.class));
  }

  // ── DELETE ────────────────────────────────────────────────────────────────

  @Test
  @Story("Suppression d'une annonce")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Sofia supprime son annonce — suppression logique, statut passe à INACTIVE.")
  @DisplayName("delete — passe le statut à INACTIVE (suppression logique)")
  void delete_shouldSetStatusInactive_whenSellerIsOwner() {
    when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));
    when(listingRepository.save(any(Listing.class))).thenReturn(listing);

    listingService.delete(100L, 1L);

    assertThat(listing.getStatus()).isEqualTo(ListingStatus.INACTIVE);
    verify(listingRepository).save(listing);
  }

  @Test
  @Story("Suppression d'une annonce")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Un vendeur tiers tente de supprimer l'annonce — UnauthorizedActionException levée.")
  @DisplayName("delete — lève UnauthorizedActionException si le vendeur n'est pas propriétaire")
  void delete_shouldThrowUnauthorizedActionException_whenSellerIsNotOwner() {
    when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));

    assertThatThrownBy(() -> listingService.delete(100L, 999L))
        .isInstanceOf(UnauthorizedActionException.class)
        .hasMessageContaining("999");
  }

  // ── FIND BY SELLER ────────────────────────────────────────────────────────

  @Test
  @Story("Consultation des annonces")
  @Severity(SeverityLevel.NORMAL)
  @Description("Récupération des annonces actives d'un vendeur.")
  @DisplayName("findBySeller — retourne les annonces ACTIVE du vendeur")
  void findBySeller_shouldReturnActiveListings() {
    when(listingRepository.findBySellerIdAndStatus(1L, ListingStatus.ACTIVE))
        .thenReturn(List.of(listing));

    List<Listing> result = listingService.findBySeller(1L);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getStatus()).isEqualTo(ListingStatus.ACTIVE);
  }

  @Test
  @Story("Consultation des annonces")
  @Severity(SeverityLevel.MINOR)
  @Description("Vendeur sans annonce active — liste vide retournée.")
  @DisplayName("findBySeller — retourne une liste vide si aucune annonce active")
  void findBySeller_shouldReturnEmptyList_whenNoActiveListings() {
    when(listingRepository.findBySellerIdAndStatus(1L, ListingStatus.ACTIVE))
        .thenReturn(List.of());

    List<Listing> result = listingService.findBySeller(1L);

    assertThat(result).isEmpty();
  }

  // ── FIND BY ID ────────────────────────────────────────────────────────────

  @Test
  @Story("Consultation des annonces")
  @Severity(SeverityLevel.NORMAL)
  @Description("Récupération d'une annonce par identifiant — retourne l'annonce.")
  @DisplayName("findById — retourne l'annonce correspondant à l'id")
  void findById_shouldReturnListing_whenFound() {
    when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));

    Listing result = listingService.findById(100L);

    assertThat(result.getId()).isEqualTo(100L);
  }

  @Test
  @Story("Consultation des annonces")
  @Severity(SeverityLevel.NORMAL)
  @Description("Annonce introuvable — ListingNotFoundException levée avec l'id dans le message.")
  @DisplayName("findById — lève ListingNotFoundException si l'id est inconnu")
  void findById_shouldThrowException_whenNotFound() {
    when(listingRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> listingService.findById(99L))
        .isInstanceOf(ListingNotFoundException.class)
        .hasMessageContaining("99");
  }

  // ── CHANGE STATUS ─────────────────────────────────────────────────────────

  @Test
  @Story("Changement de statut")
  @Severity(SeverityLevel.NORMAL)
  @Description("Admin change le statut d'une annonce vers INACTIVE.")
  @DisplayName("changeStatus — met à jour le statut de l'annonce")
  void changeStatus_shouldUpdateStatus() {
    when(listingRepository.findById(100L)).thenReturn(Optional.of(listing));
    when(listingRepository.save(any(Listing.class))).thenReturn(listing);

    Listing result = listingService.changeStatus(100L, ListingStatus.INACTIVE);

    assertThat(result.getStatus()).isEqualTo(ListingStatus.INACTIVE);
    verify(listingRepository).save(listing);
  }

  @Test
  @Story("Changement de statut")
  @Severity(SeverityLevel.NORMAL)
  @Description("Annonce introuvable lors du changement de statut — ListingNotFoundException.")
  @DisplayName("changeStatus — lève ListingNotFoundException si l'annonce est inconnue")
  void changeStatus_shouldThrowException_whenListingNotFound() {
    when(listingRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> listingService.changeStatus(99L, ListingStatus.INACTIVE))
        .isInstanceOf(ListingNotFoundException.class)
        .hasMessageContaining("99");
  }

  // ── FIND DISTINCT ACTIVE CITIES ──────────────────────────────────────────

  @Test
  @Story("Autocomplétion recherche ville")
  @Severity(SeverityLevel.NORMAL)
  @Description("Villes actives présentes en base — liste triée retournée sans transformation.")
  @DisplayName("findDistinctActiveCities — retourne la liste triée des villes actives")
  void findDistinctActiveCities_shouldReturnCityList_whenActiveListingsExist() {
    when(listingRepository.findDistinctActiveCities())
        .thenReturn(List.of("Nancy", "Paris", "Strasbourg"));

    List<String> result = listingService.findDistinctActiveCities();

    assertThat(result).containsExactly("Nancy", "Paris", "Strasbourg");
    verify(listingRepository).findDistinctActiveCities();
  }

  @Test
  @Story("Autocomplétion recherche ville")
  @Severity(SeverityLevel.MINOR)
  @Description("Aucune annonce active — liste vide retournée, aucune exception levée.")
  @DisplayName("findDistinctActiveCities — retourne une liste vide si aucune annonce active")
  void findDistinctActiveCities_shouldReturnEmptyList_whenNoActiveListings() {
    when(listingRepository.findDistinctActiveCities()).thenReturn(List.of());

    List<String> result = listingService.findDistinctActiveCities();

    assertThat(result).isEmpty();
  }
}