package com.tortiki.api.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.tortiki.api.application.port.in.SearchCriteria;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.ListingStatus;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests d'intégration Testcontainers pour la recherche d'annonces.
 *
 * <p>Vérifie les requêtes JPQL de {@link SearchListingRepositoryAdapter}
 * contre un vrai PostgreSQL 16 — garantit la compatibilité
 * avec le schéma Flyway V1 et les filtres {@link SearchCriteria}.</p>
 */
@Epic("Recherche d'annonces")
@Feature("SearchListingRepository — intégration PostgreSQL")
@Transactional
@DisplayName("SearchListingRepositoryIT")
class SearchListingRepositoryIT extends AbstractIntegrationTest {

  /** Date fixe déterministe — pas de system clock dans les tests. */
  private static final LocalDateTime PICKUP_DATETIME =
      LocalDateTime.of(2026, Month.JUNE, 20, 12, 0, 0);

  @Autowired
  private CuisineTypeJpaRepository cuisineTypeJpaRepository;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private ListingJpaRepository listingJpaRepository;

  @Autowired
  private SearchListingRepositoryAdapter searchListingRepositoryAdapter;

  private CuisineTypeJpaEntity cuisineType;
  private UserJpaEntity seller;

  @BeforeEach
  void setUp() {
    cuisineType = cuisineTypeJpaRepository.findAll().getFirst();

    seller = new UserJpaEntity();
    seller.setEmail("sofia-search-it@tortiki.fr");
    seller.setPasswordHash("$2a$12$hash");
    seller.setFirstName("Sofia");
    seller.setLastName("Kovalenko");
    seller.setCity("Strasbourg");
    seller.setLatitude(48.5734053);
    seller.setLongitude(7.7521113);
    seller.setEnabled(true);
    seller = userJpaRepository.save(seller);
  }

  // ─────────────────────────────────────────────────────────
  // Recherche nominale dans le rayon
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Recherche géographique")
  @Description("Recherche dans un rayon de 10 km — retourne les annonces actives.")
  @DisplayName("Doit retourner les annonces dans le rayon de recherche")
  void shouldReturnListingsWithinRadius() {
    givenListingInStrasbourg();

    SearchCriteria criteria = new SearchCriteria(
        null, "Strasbourg", null, List.of(),
        null, 48.5734053, 7.7521113, 10.0, 0, 10
    );

    List<Listing> results = searchListingRepositoryAdapter.search(criteria);

    assertThat(results)
        .isNotEmpty()
        .allMatch(l -> l.getStatus() == ListingStatus.ACTIVE);
  }

  // ─────────────────────────────────────────────────────────
  // Filtre par type de cuisine
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Filtres")
  @Description("Filtre par cuisineTypeId — retourne uniquement les annonces correspondantes.")
  @DisplayName("Doit filtrer les annonces par type de cuisine")
  void shouldFilterByCuisineType() {
    givenListingInStrasbourg();

    SearchCriteria criteria = new SearchCriteria(
        null, "Strasbourg", cuisineType.getId(), List.of(),
        null, 48.5734053, 7.7521113, 10.0, 0, 10
    );

    List<Listing> results = searchListingRepositoryAdapter.search(criteria);

    assertThat(results)
        .isNotEmpty()
        .allMatch(l -> l.getCuisineType().getId().equals(cuisineType.getId()));
  }

  // ─────────────────────────────────────────────────────────
  // Zone sans annonce
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Cas limites")
  @Description("Recherche dans une zone sans annonce — retourne une liste vide.")
  @DisplayName("Doit retourner une liste vide si aucune annonce dans la zone")
  void shouldReturnEmptyListWhenNoListingsInArea() {
    SearchCriteria criteria = new SearchCriteria(
        null, "Paris", null, List.of(),
        null, 48.8566101, 2.3514992, 1.0, 0, 10
    );

    List<Listing> results = searchListingRepositoryAdapter.search(criteria);

    assertThat(results).isEmpty();
  }

  // ─────────────────────────────────────────────────────────
  // Helper
  // ─────────────────────────────────────────────────────────

  private void givenListingInStrasbourg() {
    ListingJpaEntity listing = new ListingJpaEntity();
    listing.setSeller(seller);
    listing.setCuisineType(cuisineType);
    listing.setTitle("Bortsch maison");
    listing.setDescription("Recette traditionnelle ukrainienne");
    listing.setPrice(new BigDecimal("8.50"));
    listing.setPortions(4);
    listing.setPickupAddress("1 rue de la Paix, Strasbourg");
    listing.setCity("Strasbourg");
    listing.setPickupLat(48.5734053);
    listing.setPickupLng(7.7521113);
    listing.setPickupDatetime(PICKUP_DATETIME);
    listing.setStatus(ListingStatus.ACTIVE);
    listingJpaRepository.save(listing);
  }
}