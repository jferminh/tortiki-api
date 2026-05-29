package com.tortiki.api.domain.exception;

/**
 * Exception levée lorsqu'un utilisateur demandé est introuvable.
 *
 * <p>Exception métier du domaine : ne dépend d'aucun framework.
 * Traduite en réponse HTTP 404 par l'adaptateur entrant REST.</p>
 */
public class UserNotFoundException extends RuntimeException {

  /**
   * Construit l'exception avec un message descriptif.
   *
   * @param message description de l'erreur
   */
  public UserNotFoundException(String message) {
    super(message);
  }
}