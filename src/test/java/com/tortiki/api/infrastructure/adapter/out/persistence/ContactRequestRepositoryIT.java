package com.tortiki.api.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
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
 * Tests d'intégration Testcontainers pour la persistance des demandes de contact.
 *
 * <p>Vérifie les opérations JPA de {@link ContactRequestRepositoryAdapter}
 * contre un vrai PostgreSQL 16 — notamment la contrainte d'unicité
 * {@code UNIQUE(listing_id, buyer_id)} définie dans {@code V1__init_schema.sql}.</p>
 */
@Epic("Demande de contact")
@Feature("ContactRequestRepository — intégration PostgreSQL")
@Transactional
@DisplayName("ContactRequestRepositoryIT")
class ContactRequestRepositoryIT extends AbstractIntegrationTest {

  /** Date fixe déterministe — pas de system clock dans les tests. */
  private static final LocalDateTime PICKUP_DATETIME =
      LocalDateTime.of(2026, Month.JUNE, 20, 12, 0, 0);

  @Autowired
  private ContactRequestRepositoryAdapter contactRequestRepositoryAdapter;

  @Autowired
  private ListingJpaRepository listingJpaRepository;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @Autowired
  private CuisineTypeJpaRepository cuisineTypeJpaRepository;

  private ListingJpaEntity listing;
  private UserEntity buyer;

  @BeforeEach
  void setUp() {
    CuisineTypeJpaEntity cuisineType = cuisineTypeJpaRepository.findAll().getFirst();

    UserEntity seller = new UserEntity();
    seller.setEmail("sofia-it@tortiki.fr");
    seller.setPasswordHash("$2a$12$hash");
    seller.setFirstName("Sofia");
    seller.setLastName("Kovalenko");
    seller.setEnabled(true);
    seller = userJpaRepository.save(seller);

    buyer = new UserEntity();
    buyer.setEmail("theo-it@tortiki.fr");
    buyer.setPasswordHash("$2a$12$hash");
    buyer.setFirstName("Théo");
    buyer.setLastName("Martin");
    buyer.setEnabled(true);
    buyer = userJpaRepository.save(buyer);

    listing = new ListingJpaEntity();
    listing.setSeller(seller);
    listing.setCuisineType(cuisineType);
    listing.setTitle("Bortsch IT test");
    listing.setPrice(new BigDecimal("8.50"));
    listing.setPortions(4);
    listing.setPickupAddress("1 rue de la Paix, Strasbourg");
    listing.setPickupDatetime(PICKUP_DATETIME); // ← constante fixe
    listing.setStatus(ListingStatus.ACTIVE);
    listing = listingJpaRepository.save(listing);
  }

  // ─────────────────────────────────────────────────────────
  // Persistance nominale
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Persistance")
  @Description("Une demande valide est persistée et retournée avec son id généré.")
  @DisplayName("Doit persister une demande et retourner l'id généré")
  void shouldSaveContactRequestAndReturnGeneratedId() {
    ContactRequest domain = buildContactRequest();

    ContactRequest saved = contactRequestRepositoryAdapter.save(domain);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getStatus()).isEqualTo(ContactRequestStatus.PENDING);
  }

  // ─────────────────────────────────────────────────────────
  // Vérification doublon
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Règle unicité")
  @Description("existsByListingIdAndBuyerId retourne true après une première persistance.")
  @DisplayName("Doit détecter un doublon via existsByListingIdAndBuyerId")
  void shouldDetectDuplicateContactRequest() {
    contactRequestRepositoryAdapter.save(buildContactRequest());

    boolean exists = contactRequestRepositoryAdapter
        .existsByListingIdAndBuyerId(listing.getId(), buyer.getId());

    assertThat(exists).isTrue();
  }

  // ─────────────────────────────────────────────────────────
  // Recherche par listing
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Requêtes")
  @Description("findByListingId retourne toutes les demandes pour une annonce.")
  @DisplayName("Doit retourner les demandes par listingId")
  void shouldFindContactRequestsByListingId() {
    contactRequestRepositoryAdapter.save(buildContactRequest());

    List<ContactRequest> results = contactRequestRepositoryAdapter
        .findByListingId(listing.getId());

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().getListing().getId()).isEqualTo(listing.getId());
  }

  // ─────────────────────────────────────────────────────────
  // Helper
  // ─────────────────────────────────────────────────────────

  private ContactRequest buildContactRequest() {
    com.tortiki.api.domain.model.Listing domainListing =
        new com.tortiki.api.domain.model.Listing();
    domainListing.setId(listing.getId());

    com.tortiki.api.domain.model.User domainBuyer =
        new com.tortiki.api.domain.model.User();
    domainBuyer.setId(buyer.getId());

    ContactRequest cr = new ContactRequest();
    cr.setListing(domainListing);
    cr.setBuyer(domainBuyer);
    cr.setPortions(2);
    cr.setStatus(ContactRequestStatus.PENDING);
    cr.setMessage("Je suis intéressé !");
    return cr;
  }
}