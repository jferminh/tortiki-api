package com.tortiki.api.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tortiki.api.application.port.in.SearchCriteria;
import com.tortiki.api.application.port.in.SearchListingsUseCase;
import com.tortiki.api.config.SecurityConfig;
import com.tortiki.api.domain.model.CuisineType;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.User;
import com.tortiki.api.infrastructure.adapter.out.persistence.UserDetailsServiceImpl;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.math.BigDecimal;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests unitaires de {@link SearchListingController}.
 *
 * <p>Utilise {@code @WebMvcTest} pour tester la couche HTTP
 * en isolation — {@link SearchListingsUseCase} est mocké.</p>
 */
@Epic("Annonces")
@Feature("SearchListingController")
@Owner("Tortiki")
@WebMvcTest(SearchListingController.class)
@Import(SecurityConfig.class)
@DisplayName("SearchListingController — Tests unitaires WebMvcTest")
@Disabled("En attente : refactor Listing.pickupAddress + pickupDatetime — refs #25")
class SearchListingControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private SearchListingsUseCase searchListingsUseCase;

  @MockitoBean
  private UserDetailsServiceImpl userDetailsServiceImp;

  // ── Cas nominaux ────────────────────────────────────────────────────────────

  @Test
  @WithAnonymousUser
  @Story("Recherche publique")
  @Severity(SeverityLevel.CRITICAL)
  @DisplayName("GET /search : retourne 200 avec liste d'annonces")
  @Description("""
      Vérifie que GET /api/v1/listings/search retourne HTTP 200
      avec la liste d'annonces mappées en DTO.
      """)
  void search_shouldReturn200WithListings_whenResultsFound() throws Exception {
    when(searchListingsUseCase.search(any(SearchCriteria.class)))
        .thenReturn(List.of(buildListing(1L, "Bortsch maison", "8.50")));

    mockMvc.perform(get("/api/v1/listings/search")
            .param("city", "Nancy"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].title").value("Bortsch maison"))
        .andExpect(jsonPath("$[0].price").value(8.50))
        .andExpect(jsonPath("$[0].pickupAddress").value("12 rue de la Paix, 54000 Nancy"))
        .andExpect(jsonPath("$[0].cuisineType").value("Ukrainienne"))
        .andExpect(jsonPath("$[0].sellerName").value("Sofia Kovalenko"));
  }

  @Test
  @WithAnonymousUser
  @Story("Recherche publique")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("GET /search : retourne 200 avec liste vide si aucun résultat")
  @Description("""
      Vérifie que GET /api/v1/listings/search retourne HTTP 200
      avec une liste vide lorsqu'aucune annonce ne correspond.
      """)
  void search_shouldReturn200WithEmptyList_whenNoResults() throws Exception {
    when(searchListingsUseCase.search(any(SearchCriteria.class)))
        .thenReturn(List.of());

    mockMvc.perform(get("/api/v1/listings/search"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @WithAnonymousUser
  @Story("Recherche publique")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("GET /search : accepte tous les paramètres de filtre")
  @Description("""
      Vérifie que GET /api/v1/listings/search accepte query, city,
      cuisineTypeId, maxPrice, radiusKm, page, size sans erreur.
      """)
  void search_shouldAcceptAllFilterParams_whenProvided() throws Exception {
    when(searchListingsUseCase.search(any(SearchCriteria.class)))
        .thenReturn(List.of());

    mockMvc.perform(get("/api/v1/listings/search")
            .param("query", "bortsch")
            .param("city", "Nancy")
            .param("cuisineTypeId", "1")
            .param("maxPrice", "15.00")
            .param("radiusKm", "20.0")
            .param("page", "0")
            .param("size", "10"))
        .andExpect(status().isOk());
  }

  @Test
  @WithAnonymousUser
  @Story("Recherche publique")
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("GET /search : retourne 200 sans paramètres (recherche globale)")
  @Description("""
      Vérifie que GET /api/v1/listings/search sans paramètre
      retourne HTTP 200 — tous les filtres sont optionnels.
      """)
  void search_shouldReturn200_whenNoParamsProvided() throws Exception {
    when(searchListingsUseCase.search(any(SearchCriteria.class)))
        .thenReturn(List.of());

    mockMvc.perform(get("/api/v1/listings/search"))
        .andExpect(status().isOk());
  }

  // ── Helper ──────────────────────────────────────────────────────────────────

  private Listing buildListing(Long id, String title, String price) {
    CuisineType cuisineType = new CuisineType();
    cuisineType.setId(1L);
    cuisineType.setName("Ukrainienne");

    User seller = new User();
    seller.setId(10L);
    seller.setFirstName("Sofia");
    seller.setLastName("Kovalenko");

    Listing listing = new Listing();
    listing.setId(id);
    listing.setTitle(title);
    listing.setDescription("Recette traditionnelle ukrainienne");
    listing.setPrice(new BigDecimal(price));
    listing.setPickupAddress("12 rue de la Paix, 54000 Nancy"); // ← était city + postalCode
    listing.setPickupDatetime(
        java.time.LocalDateTime.of(2026, Month.JUNE, 21, 12, 0));       // ← était absent
    listing.setPortions(4);
    listing.setPhotoUrl("http://localhost:9000/tortiki-photos/bortsch.jpg");
    listing.setCuisineType(cuisineType);
    listing.setSeller(seller);
    return listing;
  }
}