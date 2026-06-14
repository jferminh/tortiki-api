package com.tortiki.api.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO d'entrée pour la création ou la modification d'une annonce de plat.
 *
 * <p>Record immuable Java 21 — validé par Bean Validation avant d'atteindre
 * le contrôleur. Converti en {@code ManageListingUseCase.Command} par
 * {@code ListingWebMapper}.</p>
 *
 * <p>Ne contient pas le {@code sellerId} — résolu depuis Spring Security.</p>
 *
 * @param title          titre de l'annonce
 * @param description    description détaillée du plat
 * @param price          prix unitaire en euros
 * @param portions       nombre de portions disponibles
 * @param pickupAddress  adresse complète de retrait
 * @param pickupDatetime date et heure du créneau de retrait
 * @param cuisineTypeId  identifiant de l'origine culinaire
 * @param allergenIds    identifiants des allergènes présents (liste vide si aucun)
 */
@Schema(description = "Données de création ou modification d'une annonce")
public record CreateListingRequest(

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
    @Schema(description = "Titre de l'annonce", example = "Bortsch ukrainien maison")
    String title,

    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    @Schema(description = "Description détaillée du plat", example = "Soupe traditionnelle...")
    String description,

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    @Schema(description = "Prix unitaire en euros", example = "8.50")
    BigDecimal price,

    @NotNull(message = "Le nombre de portions est obligatoire")
    @Positive(message = "Le nombre de portions doit être supérieur à 0")
    @Schema(description = "Nombre de portions disponibles", example = "4")
    Integer portions,

    @NotBlank(message = "L'adresse de retrait est obligatoire")
    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
    @Schema(
        description = "Adresse complète de retrait",
        example = "12 rue de la Paix, 54000 Nancy"
    )
    String pickupAddress,

    @NotNull(message = "Le créneau de retrait est obligatoire")
    @Future(message = "Le créneau de retrait doit être dans le futur")
    @Schema(
        description = "Date et heure du créneau de retrait",
        example = "2026-06-21T14:00:00"
    )
    LocalDateTime pickupDatetime,

    @NotNull(message = "L'origine culinaire est obligatoire")
    @Schema(description = "Identifiant de l'origine culinaire", example = "1")
    Long cuisineTypeId,

    @Schema(
        description = "Identifiants des allergènes présents (liste vide si aucun)",
        example = "[1, 3, 7]"
    )
    List<Long> allergenIds

) {
  /**
   * Constructeur compact — normalise {@code allergenIds} à liste vide si null.
   *
   * <p>Garantit que {@code ListingWebMapper.toCommand()} ne reçoit jamais
   * {@code null} pour les allergènes.</p>
   */
  public CreateListingRequest {
    allergenIds = allergenIds != null ? allergenIds : List.of();
  }
}