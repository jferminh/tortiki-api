package com.tortiki.api.infrastructure.adapter.out.geolocation;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de désérialisation d'une entrée de réponse Nominatim OSM.
 *
 * <p>Mappe uniquement les champs nécessaires au géocodage.
 * Ce Record est un détail d'implémentation de {@code NominatimGateway}
 * — il ne doit jamais sortir de la couche
 * {@code infrastructure/adapter/out/geolocation/}.</p>
 *
 * @param lat latitude sous forme de chaîne (format Nominatim)
 * @param lon longitude sous forme de chaîne (format Nominatim)
 */
public record NominatimResponse(
    @JsonProperty("lat") String lat,
    @JsonProperty("lon") String lon
) {
}