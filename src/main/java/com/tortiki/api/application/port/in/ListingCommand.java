package com.tortiki.api.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Objet commande encapsulant les données nécessaires à la création
 * ou modification d'une annonce de plat.
 *
 * <p>Utilisé par {@link ManageListingUseCase} pour éviter les méthodes
 * à trop nombreux paramètres (règle SonarQube S107 : maximum 7).</p>
 *
 * <p>Record immuable — les données d'une commande ne changent pas
 * une fois construites. Les coordonnées GPS sont résolues par
 * {@code ListingService} via {@code GeolocationPort} à partir
 * de {@code pickupAddress}.</p>
 *
 * @param title           titre de l'annonce
 * @param description     description détaillée du plat
 * @param price           prix unitaire en euros
 * @param portions        nombre de portions disponibles
 * @param cuisineTypeId   identifiant de l'origine culinaire
 * @param allergenIds     liste des identifiants d'allergènes (vide si aucun)
 * @param pickupAddress   adresse complète de retrait (géocodée par Nominatim)
 * @param pickupDatetime  date et heure de retrait
 */
public record ListingCommand(
    String title,
    String description,
    BigDecimal price,
    Integer portions,
    Long cuisineTypeId,
    List<Long> allergenIds,
    String pickupAddress,
    LocalDateTime pickupDatetime
) {
}