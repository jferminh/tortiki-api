package com.tortiki.api.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.tortiki.api.domain.model.Allergen;
import com.tortiki.api.domain.model.CuisineType;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.ListingStatus;
import com.tortiki.api.domain.model.User;
import com.tortiki.api.infrastructure.adapter.in.web.dto.ListingResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires du mapper {@link ListingWebMapper}.
 *
 * <p>Vérifie en particulier les champs {@code cuisineTypeId} et
 * {@code allergenIds}, ajoutés pour corriger le mismatch de contrat
 * avec {@code ListingDetailResponse} (tortiki-frontend).</p>
 */
class ListingWebMapperTest {

  private final ListingWebMapper mapper = new ListingWebMapper();

  @Test
  @DisplayName("toResponse expose cuisineTypeId et allergenIds en plus des libellés")
  void toResponseShouldExposeIdsAlongsideNames() {
    CuisineType cuisineType = new CuisineType(1L, "Ukrainienne", "Cuisine d'Europe de l'Est", true);
    Allergen gluten = new Allergen(3L, "Gluten", true);
    Allergen lait = new Allergen(7L, "Lait", true);

    User seller = new User();
    seller.setEmail("sofia@tortiki.fr");

    Listing listing = new Listing();
    listing.setId(42L);
    listing.setTitle("Bortsch ukrainien maison");
    listing.setDescription("Soupe traditionnelle");
    listing.setPrice(BigDecimal.valueOf(8.50));
    listing.setPortions(4);
    listing.setPickupAddress("2 Allée Lys Rouge, 54000 Nancy");
    listing.setPickupDatetime(LocalDateTime.of(2026, Month.JUNE, 28, 12, 0));
    listing.setPhotoUrl(null);
    listing.setStatus(ListingStatus.ACTIVE);
    listing.setCuisineType(cuisineType);
    listing.setSeller(seller);
    listing.setAllergens(List.of(gluten, lait));
    listing.setCreatedAt(LocalDateTime.of(2026, Month.JUNE, 12, 10, 0));

    ListingResponse response = mapper.toResponse(listing);

    assertThat(response.cuisineTypeId()).isEqualTo(1L);
    assertThat(response.cuisineTypeName()).isEqualTo("Ukrainienne");
    assertThat(response.allergenIds()).containsExactlyInAnyOrder(3L, 7L);
    assertThat(response.allergenNames()).containsExactlyInAnyOrder("Gluten", "Lait");
  }

  @Test
  @DisplayName("toResponse gère un cuisineType null sans lever d'exception")
  void toResponseShouldHandleNullCuisineType() {
    Listing listing = new Listing();
    listing.setId(1L);
    listing.setAllergens(List.of());
    listing.setStatus(ListingStatus.ACTIVE);

    ListingResponse response = mapper.toResponse(listing);

    assertThat(response.cuisineTypeId()).isNull();
    assertThat(response.cuisineTypeName()).isNull();
    assertThat(response.allergenIds()).isEmpty();
  }
}