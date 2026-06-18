package com.tortiki.api.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.ListingStatus;
import com.tortiki.api.domain.model.User;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import jakarta.persistence.EntityManager;
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
 * {@code UNIQUE(listing_id, buyer_id)} définie dans {@code V1__init_schema.sql}.
 * Le contexte Spring est mutualisé via {@link AbstractIntegrationTest} :
 * un seul conteneur PostgreSQL pour toutes les classes {@code *IT}.</p>
 */
@Epic("Demande de contact")
@Feature("ContactRequestRepository — intégration PostgreSQL")
@Transactional
@DisplayName("ContactRequestRepositoryIT")
class ContactRequestRepositoryIT extends AbstractIntegrationTest {

  /** Date fixe déterministe — aucune dépendance au clock système. */
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

  /**
   * Injecté pour forcer le flush JPA avant les assertions sur les champs
   * générés côté base (ex : {@code created_at DEFAULT NOW()}).
   */
  @Autowired
  private EntityManager entityManager;

  /**
   * Injectés pour construire des objets domain complets via la chaîne
   * de conversion — teste le mapper, pas seulement la persistance brute.
   */
  @Autowired
  private ListingPersistenceMapper listingPersistenceMapper;

  @Autowired
  private UserPersistenceMapper userPersistenceMapper;

  private ListingJpaEntity listing;
  private UserJpaEntity buyer;

  @BeforeEach
  void setUp() {
    CuisineTypeJpaEntity cuisineType = cuisineTypeJpaRepository
        .findByName("Ukrainienne")
        .orElseGet(() -> {
          CuisineTypeJpaEntity ct = new CuisineTypeJpaEntity();
          ct.setName("Ukrainienne");
          return cuisineTypeJpaRepository.save(ct);
        });

    UserJpaEntity seller = new UserJpaEntity();
    seller.setEmail("sofia-it@tortiki.fr");
    seller.setPasswordHash("$2a$12$hash");
    seller.setFirstName("Sofia");
    seller.setLastName("Kovalenko");
    seller.setEnabled(true);
    seller = userJpaRepository.save(seller);

    buyer = new UserJpaEntity();
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
    listing.setPickupDatetime(PICKUP_DATETIME);
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
    ContactRequest saved = contactRequestRepositoryAdapter.save(buildContactRequest());

    // flush → force l'INSERT vers PostgreSQL (champs DEFAULT côté BDD)
    // clear → vide le cache de 1er niveau pour lire depuis la base réelle
    entityManager.flush();
    entityManager.clear();

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getStatus()).isEqualTo(ContactRequestStatus.PENDING);
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getListing().getId()).isEqualTo(listing.getId());
    assertThat(saved.getBuyer().getId()).isEqualTo(buyer.getId());
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
    entityManager.flush();

    boolean exists = contactRequestRepositoryAdapter
        .existsByListingIdAndBuyerId(listing.getId(), buyer.getId());

    assertThat(exists).isTrue();
  }

  @Test
  @Story("Règle unicité")
  @Description("existsByListingIdAndBuyerId retourne false si aucune demande n'existe.")
  @DisplayName("Doit retourner false si aucune demande pour ce couple listing/buyer")
  void shouldReturnFalseWhenNoDuplicateExists() {
    boolean exists = contactRequestRepositoryAdapter
        .existsByListingIdAndBuyerId(listing.getId(), buyer.getId());

    assertThat(exists).isFalse();
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
    entityManager.flush();
    entityManager.clear();

    List<ContactRequest> results = contactRequestRepositoryAdapter
        .findByListingId(listing.getId());

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().getListing().getId()).isEqualTo(listing.getId());
  }

  @Test
  @Story("Requêtes")
  @Description("findByListingId retourne une liste vide si aucune demande pour cette annonce.")
  @DisplayName("Doit retourner une liste vide si aucune demande pour ce listingId")
  void shouldReturnEmptyListWhenNoRequestsForListing() {
    List<ContactRequest> results = contactRequestRepositoryAdapter
        .findByListingId(listing.getId());

    assertThat(results).isEmpty();
  }

  // ─────────────────────────────────────────────────────────
  // Recherche par acheteur
  // ─────────────────────────────────────────────────────────

  @Test
  @Story("Requêtes")
  @Description("findByBuyerId retourne toutes les demandes soumises par un acheteur.")
  @DisplayName("Doit retourner les demandes par buyerId")
  void shouldFindContactRequestsByBuyerId() {
    contactRequestRepositoryAdapter.save(buildContactRequest());
    entityManager.flush();
    entityManager.clear();

    List<ContactRequest> results = contactRequestRepositoryAdapter
        .findByBuyerId(buyer.getId());

    assertThat(results).hasSize(1);
    assertThat(results.getFirst().getBuyer().getId()).isEqualTo(buyer.getId());
  }

  @Test
  @Story("Requêtes")
  @Description("findByBuyerId retourne une liste vide si l'acheteur n'a soumis aucune demande.")
  @DisplayName("Doit retourner une liste vide si aucune demande pour cet acheteur")
  void shouldReturnEmptyListWhenNoBuyerRequests() {
    List<ContactRequest> results = contactRequestRepositoryAdapter
        .findByBuyerId(buyer.getId());

    assertThat(results).isEmpty();
  }

  // ─────────────────────────────────────────────────────────
  // Helper — construction via mappers injectés
  // ─────────────────────────────────────────────────────────

  /**
   * Construit un {@link ContactRequest} domain complet via les mappers injectés.
   * Teste ainsi toute la chaîne de conversion, pas uniquement la persistance brute.
   */
  private ContactRequest buildContactRequest() {
    Listing domainListing =
        listingPersistenceMapper.toDomain(listing);
    User domainBuyer =
        userPersistenceMapper.toDomain(buyer);

    ContactRequest cr = new ContactRequest();
    cr.setListing(domainListing);
    cr.setBuyer(domainBuyer);
    cr.setPortions(2);
    cr.setStatus(ContactRequestStatus.PENDING);
    cr.setMessage("Je suis intéressé !");
    return cr;
  }
}