package com.tortiki.api.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.ListingStatus;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests d'intégration Testcontainers pour la recherche d'annonces.
 *
 * <p>Vérifie les requêtes JPA de {@link ListingSearchRepositoryAdapter}
 * contre un vrai PostgreSQL 16 — garantit la compatibilité
 * des requêtes JPQL avec le schéma Flyway V1.</p>
 *
 * <p>Chaque test est transactionnel et rollbacké automatiquement
 * pour garantir l'isolation.</p>
 */
@Epic("Recherche d'annonces")
@Feature("SearchListingRepository — intégration PostgreSQL")
@Transactional
@DisplayName("SearchListingRepositoryIT")
class SearchListingRepositoryIT extends AbstractIntegrationTest {

  @Autowired
  private CuisineTypeJpaRepository cuisineTypeJpaRepository;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private ListingJpaRepository listingJpaRepository;

  @Autowired
  private ListingSearchRepositoryAdapter listingSearchRepositoryAdapter;

  private CuisineTypeJpaEntity cuisineType;
  private UserEntity seller;

  @BeforeEach
  void setUp() {
    cuisineType = cuisineTypeJpaRepository.findAll().getFirst();

    seller = new UserEntity();
    seller.setEmail("sofia@tortiki.fr");
    seller.setPasswordHash("$2a$12$hash");
    seller.setFirstName("Sofia");
    seller.setLastName("Kovalenko");
    seller.setCity("Strasbourg");
    seller.setLatitude(new BigDecimal("48.5734053"));
    seller.setLongitude(new BigDecimal("7.7521113"));
    seller.setEnabled(true);
    seller = userJpaRepository.save(seller);
  }

  // ─────────────────────────────────────────────────────────
  // Recherche par coordonnées
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Recherche géographique")
  @Description("Recherche dans un rayon de 10 km — retourne les annonces proches.")
  @DisplayName("Doit retourner les annonces dans le rayon de recherche")
  void shouldReturnListingsWithinRadius() {
    givenListingInStrasbourg();

    List<Listing> results = listingSearchRepositoryAdapter.findByCoordinates(
        new BigDecimal("48.5734053"),
        new BigDecimal("7.7521113"),
        10.0
    );

    assertThat(results).isNotEmpty();
    assertThat(results).allMatch(l -> l.getStatus() == ListingStatus.ACTIVE);
  }

  // ─────────────────────────────────────────────────────────
  // Filtre par type de cuisine
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Filtres")
  @Description("Filtre par type de cuisine — retourne uniquement les annonces correspondantes.")
  @DisplayName("Doit filtrer les annonces par type de cuisine")
  void shouldFilterByCuisineType() {
    givenListingInStrasbourg();

    List<Listing> results = listingSearchRepositoryAdapter.findByCoordinatesAndCuisineType(
        new BigDecimal("48.5734053"),
        new BigDecimal("7.7521113"),
        10.0,
        cuisineType.getId()
    );

    assertThat(results).isNotEmpty();
    assertThat(results)
        .allMatch(l -> l.getCuisineType().getId().equals(cuisineType.getId()));
  }

  // ─────────────────────────────────────────────────────────
  // Aucun résultat
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Cas limites")
  @Description("Recherche dans une zone sans annonce — retourne une liste vide.")
  @DisplayName("Doit retourner une liste vide si aucune annonce dans la zone")
  void shouldReturnEmptyListWhenNoListingsInArea() {
    List<Listing> results = listingSearchRepositoryAdapter.findByCoordinates(
        new BigDecimal("48.8566101"),
        new BigDecimal("2.3514992"),
        1.0
    );

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
    listing.setPickupLat(new BigDecimal("48.5734053"));
    listing.setPickupLng(new BigDecimal("7.7521113"));
    listing.setPickupDatetime(LocalDateTime.now(Clock.systemUTC()).plusDays(1));
    listing.setStatus(ListingStatus.ACTIVE);
    listingJpaRepository.save(listing);
  }
}