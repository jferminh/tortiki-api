package com.tortiki.api.domain.exception;

/**
 * Exception levée lorsqu'une suppression d'origine culinaire est refusée
 * car au moins une annonce active la référence encore.
 *
 * <p>Exception métier du domaine : ne dépend d'aucun framework.
 * Traduite en réponse HTTP 409 Conflict par {@code GlobalExceptionHandler}.</p>
 */
public class CuisineTypeInUseException extends RuntimeException {

  /**
   * Construit l'exception avec un message descriptif.
   *
   * @param message description de l'erreur
   */
  public CuisineTypeInUseException(String message) {
    super(message);
  }
}