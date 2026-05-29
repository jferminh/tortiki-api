package com.tortiki.api.application.port.in;

import java.math.BigDecimal;

/**
 * Objet commande encapsulant les données nécessaires à la création
 * ou modification d'une annonce de plat.
 *
 * <p>Utilisé par {@link ManageListingUseCase} pour éviter les méthodes
 * à trop nombreux paramètres (règle SonarQube S107 : maximum 7).</p>
 *
 * <p>Record immuable — les données d'une commande ne changent pas
 * une fois construites.</p>
 *
 * @param title         titre de l'annonce
 * @param description   description détaillée du plat
 * @param price         prix unitaire en euros
 * @param portions      nombre de portions disponibles
 * @param pickupSlot    créneau de retrait unique (v1)
 * @param city          ville de retrait
 * @param postalCode    code postal de retrait
 * @param cuisineTypeId identifiant de l'origine culinaire (null pour update)
 */
public record ListingCommand(
    String title,
    String description,
    BigDecimal price,
    Integer portions,
    String pickupSlot,
    String city,
    String postalCode,
    Long cuisineTypeId
) {
}