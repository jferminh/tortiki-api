package com.tortiki.api.domain.exception;

/**
 * Exception levée lorsqu'un utilisateur tente une action
 * sur une ressource qui ne lui appartient pas.
 *
 * <p>Exemple : un vendeur tente de modifier l'annonce d'un autre vendeur.</p>
 *
 * <p>Exception métier du domaine : ne dépend d'aucun framework.
 * Traduite en réponse HTTP 403 Forbidden par {@code GlobalExceptionHandler}.</p>
 */
public class UnauthorizedActionException extends RuntimeException {

  /**
   * Construit l'exception avec un message descriptif.
   *
   * @param message description de l'erreur
   */
  public UnauthorizedActionException(String message) {
    super(message);
  }
}