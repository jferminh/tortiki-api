package com.tortiki.api.domain.exception;

/**
 * Exception levée lorsqu'une origine culinaire demandée est introuvable.
 *
 * <p>Exception métier du domaine : ne dépend d'aucun framework.
 * Traduite en réponse HTTP 404 par {@code GlobalExceptionHandler}.</p>
 */
public class CuisineTypeNotFoundException extends RuntimeException {

  /**
   * Construit l'exception avec un message descriptif.
   *
   * @param message description de l'erreur
   */
  public CuisineTypeNotFoundException(String message) {
    super(message);
  }
}