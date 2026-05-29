package com.tortiki.api.domain.exception;

/**
 * Exception levée lors d'une tentative d'inscription avec un email déjà utilisé.
 *
 * <p>Exception métier du domaine : ne dépend d'aucun framework.
 * Traduite en réponse HTTP 409 Conflict par l'adaptateur entrant REST.</p>
 */
public class UserAlreadyExistsException extends RuntimeException {

  /**
   * Construit l'exception avec un message descriptif.
   *
   * @param message description de l'erreur
   */
  public UserAlreadyExistsException(String message) {
    super(message);
  }
}