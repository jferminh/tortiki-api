package com.tortiki.api.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * DTO d'entrée pour la création d'une annonce de plat.
 *
 * <p>Record immuable validé par {@code @Valid} dans {@link
 * com.tortiki.api.infrastructure.adapter.in.web.ListingController}.
 * Mappé vers {@link com.tortiki.api.application.port.in.ListingCommand}
 * avant transmission au port primaire.</p>
 *
 * @param title         titre de l'annonce
 * @param description   description du plat
 * @param price         prix unitaire en euros (min 0.01)
 * @param portions      nombre de portions disponibles (min 1)
 * @param pickupSlot    créneau de retrait
 * @param city          ville de retrait
 * @param postalCode    code postal de retrait
 * @param cuisineTypeId identifiant de l'origine culinaire
 */
public record CreateListingRequest(
    @NotBlank(message = "Le titre est obligatoire")
    String title,

    @NotBlank(message = "La description est obligatoire")
    String description,

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    BigDecimal price,

    @NotNull(message = "Le nombre de portions est obligatoire")
    @Min(value = 1, message = "Au moins une portion est requise")
    Integer portions,

    @NotBlank(message = "Le créneau de retrait est obligatoire")
    String pickupSlot,

    @NotBlank(message = "La ville est obligatoire")
    String city,

    @NotBlank(message = "Le code postal est obligatoire")
    String postalCode,

    @NotNull(message = "L'origine culinaire est obligatoire")
    Long cuisineTypeId
) {
}