package com.tortiki.api.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tortiki.api.application.port.out.ListingRepository;
import com.tortiki.api.domain.exception.ListingNotFoundException;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.ListingStatus;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
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
 * Tests unitaires du service {@link AdminListingService}.
 *
 * <p>Utilise Mockito pur — aucun contexte Spring chargé. Isole
 * totalement la logique métier du port secondaire {@link ListingRepository},
 * qui est mocké.</p>
 */
@Epic("Administration")
@Feature("Modération des annonces")
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminListingService — Tests unitaires")
class AdminListingServiceTest {

  private static final Long LISTING_ID = 1L;
  private static final Long UNKNOWN_ID = 99L;

  @Mock
  private ListingRepository listingRepository;

  @InjectMocks
  private AdminListingService adminListingService;

  private Listing listing;

  @BeforeEach
  void setUp() {
    listing = new Listing();
    listing.setId(LISTING_ID);
    listing.setTitle("Bortsch ukrainien");
    listing.setStatus(ListingStatus.ACTIVE);
  }

  @Test
  @Story("Consultation modération")
  @Severity(SeverityLevel.NORMAL)
  @Description("findAll délègue au port out et retourne toutes les annonces")
  @DisplayName("findAll() retourne toutes les annonces du repository")
  void findAll_shouldReturnAllListings() {
    when(listingRepository.findAll()).thenReturn(List.of(listing));

    List<Listing> result = adminListingService.findAll();

    assertThat(result).hasSize(1).containsExactly(listing);
  }

  @Test
  @Story("Consultation modération")
  @Severity(SeverityLevel.NORMAL)
  @Description("findAll retourne une liste vide si aucune annonce n'existe")
  @DisplayName("findAll() retourne une liste vide si aucune annonce")
  void findAll_shouldReturnEmptyList_whenNoListings() {
    when(listingRepository.findAll()).thenReturn(List.of());

    List<Listing> result = adminListingService.findAll();

    assertThat(result).isEmpty();
  }

  @Test
  @Story("Changement de statut")
  @Severity(SeverityLevel.CRITICAL)
  @Description("updateStatus modifie le statut et persiste l'annonce")
  @DisplayName("updateStatus() change le statut et sauvegarde")
  void updateStatus_shouldChangeStatusAndSave_whenListingExists() {
    when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
    when(listingRepository.save(any(Listing.class))).thenReturn(listing);

    Listing result = adminListingService.updateStatus(LISTING_ID, "INACTIVE");

    assertThat(result.getStatus()).isEqualTo(ListingStatus.INACTIVE);
    verify(listingRepository).save(listing);
  }

  @Test
  @Story("Changement de statut")
  @Severity(SeverityLevel.CRITICAL)
  @Description("updateStatus lève ListingNotFoundException si l'annonce est introuvable")
  @DisplayName("updateStatus() lève ListingNotFoundException si annonce absente")
  void updateStatus_shouldThrow_whenListingNotFound() {
    when(listingRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> adminListingService.updateStatus(UNKNOWN_ID, "INACTIVE"))
        .isInstanceOf(ListingNotFoundException.class)
        .hasMessageContaining(String.valueOf(UNKNOWN_ID));

    verify(listingRepository, never()).save(any(Listing.class));
  }

  @Test
  @Story("Changement de statut")
  @Severity(SeverityLevel.NORMAL)
  @Description("updateStatus accepte DELETED comme statut valide")
  @DisplayName("updateStatus() accepte le statut DELETED")
  void updateStatus_shouldAcceptDeletedStatus() {
    when(listingRepository.findById(LISTING_ID)).thenReturn(Optional.of(listing));
    when(listingRepository.save(any(Listing.class))).thenReturn(listing);

    Listing result = adminListingService.updateStatus(LISTING_ID, "DELETED");

    assertThat(result.getStatus()).isEqualTo(ListingStatus.DELETED);
  }
}