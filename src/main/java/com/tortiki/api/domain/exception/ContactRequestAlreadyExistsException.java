package com.tortiki.api.domain.exception;

/**
 * Exception levée lorsqu'un acheteur tente de soumettre une seconde demande
 * pour une annonce pour laquelle il a déjà une demande active.
 *
 * <p>Correspond à la contrainte {@code UNIQUE(listing_id, buyer_id)}
 * définie dans {@code V1__init_schema.sql}.</p>
 */
public class ContactRequestAlreadyExistsException extends RuntimeException {

  /**
   * Construit l'exception avec l'identifiant de l'annonce concernée.
   *
   * @param listingId identifiant de l'annonce déjà demandée
   */
  public ContactRequestAlreadyExistsException(Long listingId) {
    super("Une demande existe déjà pour l'annonce avec l'identifiant : " + listingId);
  }
}