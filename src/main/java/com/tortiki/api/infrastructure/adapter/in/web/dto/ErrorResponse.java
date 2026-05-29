package com.tortiki.api.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;

/**
 * DTO de réponse d'erreur uniforme pour toutes les exceptions de l'API.
 *
 * <p>Record immuable — serialisé automatiquement en JSON par Jackson.</p>
 */
public record ErrorResponse(
    int status,
    String error,
    String message,
    LocalDateTime timestamp
) {

  /**
   * Construit une réponse d'erreur avec l'horodatage courant.
   *
   * @param status  code HTTP
   * @param error   libellé de l'erreur (ex. "Not Found")
   * @param message message descriptif
   * @return instance de {@link ErrorResponse}
   */
  public static ErrorResponse of(int status, String error, String message) {
    return new ErrorResponse(status, error, message, LocalDateTime.now());
  }
}