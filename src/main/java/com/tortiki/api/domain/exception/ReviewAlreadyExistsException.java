package com.tortiki.api.domain.exception;

/**
 * Levée lorsqu'un acheteur tente de noter deux fois la même annonce.
 */
public class ReviewAlreadyExistsException extends RuntimeException {

  /**
   * Crée l'exception avec un message contextualisé.
   *
   * @param reviewerEmail email de l'acheteur
   * @param listingId     identifiant de l'annonce
   */
  public ReviewAlreadyExistsException(String reviewerEmail, Long listingId) {
    super("L'utilisateur " + reviewerEmail
        + " a déjà noté l'annonce #" + listingId);
  }
}