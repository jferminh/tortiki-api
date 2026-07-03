package com.tortiki.api.domain.exception;

/**
 * Exception levée lorsqu'un allergène demandé n'existe pas dans le référentiel.
 *
 * <p>Appartient au domaine métier — aucune dépendance HTTP ou JPA.
 * Traduite en réponse HTTP 404 par {@code GlobalExceptionHandler}.</p>
 */
public class AllergenNotFoundException extends RuntimeException {

  /**
   * Construit l'exception avec l'identifiant de l'allergène introuvable.
   *
   * @param id identifiant de l'allergène recherché
   */
  public AllergenNotFoundException(Long id) {
    super("Allergène introuvable avec l'id " + id);
  }
}