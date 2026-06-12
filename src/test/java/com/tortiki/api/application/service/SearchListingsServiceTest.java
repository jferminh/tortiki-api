package com.tortiki.api.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tortiki.api.application.port.in.SearchCriteria;
import com.tortiki.api.application.port.out.GeolocationPort;
import com.tortiki.api.application.port.out.GeolocationPort.Coordinates;
import com.tortiki.api.application.port.out.SearchListingRepository;
import com.tortiki.api.domain.model.Listing;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitaires de {@link SearchListingsService}.
 *
 * <p>Vérifie l'orchestration géolocalisation + recherche,
 * ainsi que la résilience en cas d'échec Nominatim.</p>
 */
@Epic("Annonces")
@Feature("SearchListingsService")
@Owner("Tortiki")
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchListingsService — Tests unitaires")
class SearchListingsServiceTest {

  @Mock
  private GeolocationPort geolocationPort;

  @Mock
  private SearchListingRepository searchListingRepository;

  private SearchListingsService service;

  /** Initialisation du service avant chaque test. */
  @BeforeEach
  void setUp() {
    service = new SearchListingsService(geolocationPort, searchListingRepository);
  }

  // ── Cas nominaux ────────────────────────────────────────────────────────────

  @Test
  @Story("Recherche avec géolocalisation")
  @Severity(SeverityLevel.CRITICAL)
  @DisplayName("search : enrichit les critères avec coordonnées GPS si city fournie")
  @Description("""
      Vérifie que search() appelle GeolocationPort.geocode() lorsque city
      est renseignée, puis transmet les coordonnées enrichies au repository.
      """)
  void search_shouldEnrichCriteriaWithCoordinates_whenCityIsProvided() {
    SearchCriteria criteria = givenCriteriaWithCity("Nancy");
    givenGeolocationReturns("Nancy", 48.6921, 6.1844);
    List<Listing> expected = givenRepositoryReturns(List.of(buildListing(1L)));

    List<Listing> result = service.search(criteria);

    assertThat(result).isEqualTo(expected);
    verify(geolocationPort).geocode("Nancy");
    verify(searchListingRepository).search(any(SearchCriteria.class));
  }

  @Test
  @Story("Recherche sans ville")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("search : n'appelle pas GeolocationPort si city est null")
  @Description("""
      Vérifie que search() ne sollicite pas le port de géolocalisation
      lorsque city est null — évite un appel réseau inutile.
      """)
  void search_shouldNotCallGeolocation_whenCityIsNull() {
    SearchCriteria criteria = givenCriteriaWithoutCity();
    givenRepositoryReturns(List.of());

    service.search(criteria);

    verify(geolocationPort, never()).geocode(any());
  }

  @Test
  @Story("Recherche sans ville")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("search : n'appelle pas GeolocationPort si city est blank")
  @Description("""
      Vérifie que search() ne sollicite pas le port de géolocalisation
      lorsque city est une chaîne vide ou espaces.
      """)
  void search_shouldNotCallGeolocation_whenCityIsBlank() {
    SearchCriteria criteria = givenCriteriaWithCity("   ");
    givenRepositoryReturns(List.of());

    service.search(criteria);

    verify(geolocationPort, never()).geocode(any());
  }

  // ── Résilience ──────────────────────────────────────────────────────────────

  @Test
  @Story("Résilience géolocalisation")
  @Severity(SeverityLevel.CRITICAL)
  @DisplayName("search : recherche sans coordonnées si Nominatim échoue")
  @Description("""
      Vérifie que search() continue sans filtre géographique
      lorsque GeolocationPort retourne Optional.empty().
      Nominatim indisponible ne doit pas bloquer la recherche.
      """)
  void search_shouldSearchWithoutCoordinates_whenGeolocationFails() {
    SearchCriteria criteria = givenCriteriaWithCity("Nancy");
    givenGeolocationReturnsEmpty("Nancy");
    List<Listing> expected = givenRepositoryReturns(List.of(buildListing(1L)));

    List<Listing> result = service.search(criteria);

    assertThat(result).isEqualTo(expected);
    verify(searchListingRepository).search(any(SearchCriteria.class));
  }

  @Test
  @Story("Recherche avec géolocalisation")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("search : retourne liste vide si aucune annonce trouvée")
  @Description("""
      Vérifie que search() retourne une liste vide sans exception
      lorsque le repository ne trouve aucune annonce correspondante.
      """)
  void search_shouldReturnEmptyList_whenNoListingsFound() {
    SearchCriteria criteria = givenCriteriaWithCity("Nancy");
    givenGeolocationReturns("Nancy", 48.6921, 6.1844);
    givenRepositoryReturns(List.of());

    List<Listing> result = service.search(criteria);

    assertThat(result).isEmpty();
  }

  // ── Steps Allure ────────────────────────────────────────────────────────────

  @Step("Étant donné des critères avec city='{city}'")
  private SearchCriteria givenCriteriaWithCity(String city) {
    return new SearchCriteria(
        null, city, null, null, null, null, null, 10.0, 0, 10);
  }

  @Step("Étant donné des critères sans ville")
  private SearchCriteria givenCriteriaWithoutCity() {
    return new SearchCriteria(
        null, null, null, null, null, null, null, 10.0, 0, 10);
  }

  @Step("Étant donné que GeolocationPort retourne lat={lat}, lng={lng} pour '{city}'")
  private void givenGeolocationReturns(String city, double lat, double lng) {
    when(geolocationPort.geocode(city))
        .thenReturn(Optional.of(new Coordinates(lat, lng)));
  }

  @Step("Étant donné que GeolocationPort retourne Optional.empty() pour '{city}'")
  private void givenGeolocationReturnsEmpty(String city) {
    when(geolocationPort.geocode(city)).thenReturn(Optional.empty());
  }

  @Step("Étant donné que le repository retourne une liste d'annonces")
  private List<Listing> givenRepositoryReturns(List<Listing> listings) {
    when(searchListingRepository.search(any(SearchCriteria.class)))
        .thenReturn(listings);
    return listings;
  }

  @Step("Construction d'une annonce de test id={id}")
  private Listing buildListing(Long id) {
    Listing listing = new Listing();
    listing.setId(id);
    listing.setTitle("Bortsch maison");
    listing.setPrice(new BigDecimal("8.50"));
    return listing;
  }
}