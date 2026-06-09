package com.tortiki.api.domain.exception;

/**
 * Exception levée lorsqu'un allergène est introuvable en base de données.
 *
 * <p>Utilisée par les services de la couche {@code application/service/}
 * lors de la validation des allergènes déclarés sur une annonce.</p>
 */
public class AllergenNotFoundException extends RuntimeException {

  /**
   * Construit l'exception avec l'identifiant de l'allergène introuvable.
   *
   * @param id identifiant de l'allergène introuvable
   */
  public AllergenNotFoundException(Long id) {
    super("Allergène introuvable avec l'identifiant : " + id);
  }
}