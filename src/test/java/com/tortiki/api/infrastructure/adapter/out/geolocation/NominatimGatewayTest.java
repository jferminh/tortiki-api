package com.tortiki.api.infrastructure.adapter.out.geolocation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tortiki.api.application.port.out.GeolocationPort;
import com.tortiki.api.config.NominatimProperties;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Tests unitaires de {@link NominatimGateway}.
 *
 * <p>Utilise {@link ExchangeFunction} pour simuler les réponses HTTP
 * sans démarrer de serveur réel — approche recommandée par Spring
 * pour les tests unitaires de {@link WebClient}.</p>
 */
@Epic("Géolocalisation")
@Feature("NominatimGateway")
@Owner("Tortiki")
@DisplayName("NominatimGateway — Tests unitaires")
class NominatimGatewayTest {

  private ExchangeFunction exchangeFunction;
  private NominatimGateway gateway;

  /** Initialisation du gateway avec ExchangeFunction mockée avant chaque test. */
  @BeforeEach
  void setUp() {
    exchangeFunction = mock(ExchangeFunction.class);
    WebClient webClient = WebClient.builder()
        .exchangeFunction(exchangeFunction)
        .build();
    gateway = new NominatimGateway(webClient, buildProperties());
  }

  // ── Cas nominaux ────────────────────────────────────────────────────────────

  @Test
  @Story("Recherche par ville")
  @Severity(SeverityLevel.CRITICAL)
  @DisplayName("geocode : retourne les coordonnées GPS pour Nancy")
  @Description("""
      Vérifie que geocode() retourne les coordonnées GPS correctes
      lorsque Nominatim renvoie un résultat valide pour Nancy.
      Latitude et longitude sont parsées depuis les chaînes JSON Nominatim.
      """)
  void geocode_shouldReturnCoordinates_whenCityIsKnown() {
    givenNominatimReturnsJson("""
        [{"lat":"48.6921","lon":"6.1844","display_name":"Nancy, France"}]
        """);

    Optional<GeolocationPort.Coordinates> result = gateway.geocode("Nancy");

    assertThat(result).isPresent();
    assertThat(result.get().latitude()).isEqualTo(48.6921);
    assertThat(result.get().longitude()).isEqualTo(6.1844);
  }

  @Test
  @Story("Recherche par ville")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("geocode : retourne Optional.empty() si Nominatim renvoie liste vide")
  @Description("""
      Vérifie que geocode() retourne Optional.empty() sans exception
      lorsque Nominatim ne trouve aucun résultat pour la ville fournie.
      Cas typique : ville inconnue ou mal orthographiée.
      """)
  void geocode_shouldReturnEmpty_whenNominatimReturnsNoResult() {
    givenNominatimReturnsJson("[]");

    Optional<GeolocationPort.Coordinates> result = gateway.geocode("VilleInconnue");

    assertThat(result).isEmpty();
  }

  // ── Cas limites — entrée invalide ───────────────────────────────────────────

  @Test
  @Story("Validation des entrées")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("geocode : retourne Optional.empty() si city est null")
  @Description("""
      Vérifie que geocode() retourne Optional.empty() immédiatement
      sans appeler WebClient lorsque city est null.
      Aucune requête HTTP ne doit être émise.
      """)
  void geocode_shouldReturnEmpty_whenCityIsNull() {
    Optional<GeolocationPort.Coordinates> result = gateway.geocode(null);

    assertThat(result).isEmpty();
  }

  @Test
  @Story("Validation des entrées")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("geocode : retourne Optional.empty() si city est blank")
  @Description("""
      Vérifie que geocode() retourne Optional.empty() immédiatement
      sans appeler WebClient lorsque city est une chaîne vide ou espaces.
      Aucune requête HTTP ne doit être émise.
      """)
  void geocode_shouldReturnEmpty_whenCityIsBlank() {
    Optional<GeolocationPort.Coordinates> result = gateway.geocode("   ");

    assertThat(result).isEmpty();
  }

  // ── Cas d'erreur réseau ─────────────────────────────────────────────────────

  @Test
  @Story("Résilience réseau")
  @Severity(SeverityLevel.CRITICAL)
  @DisplayName("geocode : retourne Optional.empty() si le service est indisponible")
  @Description("""
      Vérifie que geocode() absorbe les erreurs réseau et retourne
      Optional.empty() sans propager l'exception au service applicatif.
      Garantit que Nominatim indisponible ne fait pas planter la recherche.
      """)
  void geocode_shouldReturnEmpty_whenNetworkErrorOccurs() {
    givenNominatimThrowsNetworkError();

    Optional<GeolocationPort.Coordinates> result = gateway.geocode("Nancy");

    assertThat(result).isEmpty();
  }

  // ── Steps Allure ────────────────────────────────────────────────────────────

  @Step("Étant donné les propriétés Nominatim (base-url, user-agent, timeout=5s)")
  private NominatimProperties buildProperties() {
    NominatimProperties props = new NominatimProperties();
    props.setBaseUrl("https://nominatim.openstreetmap.org");
    props.setUserAgent("Tortiki/1.0");
    props.setTimeoutSeconds(5);
    return props;
  }

  @Step("Étant donné que Nominatim retourne le JSON : {json}")
  private void givenNominatimReturnsJson(String json) {
    ClientResponse clientResponse = ClientResponse
        .create(HttpStatus.OK)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .body(json)
        .build();
    when(exchangeFunction.exchange(any(ClientRequest.class)))
        .thenReturn(Mono.just(clientResponse));
  }

  @Step("Étant donné que Nominatim est indisponible (erreur réseau)")
  private void givenNominatimThrowsNetworkError() {
    when(exchangeFunction.exchange(any(ClientRequest.class)))
        .thenReturn(Mono.error(new org.springframework.web.reactive.function.client
            .WebClientRequestException(
            new java.net.ConnectException("Connection refused"),
            HttpMethod.GET,
            URI.create("https://nominatim.openstreetmap.org/search"),
            HttpHeaders.EMPTY)));
  }
}