package com.tortiki.api.domain.exception;

/**
 * Exception levée lorsqu'une annonce demandée est introuvable.
 *
 * <p>Exception métier du domaine : ne dépend d'aucun framework.
 * Traduite en réponse HTTP 404 par {@code GlobalExceptionHandler}.</p>
 */
public class ListingNotFoundException extends RuntimeException {

  /**
   * Construit l'exception avec un message descriptif.
   *
   * @param message description de l'erreur
   */
  public ListingNotFoundException(String message) {
    super(message);
  }
}