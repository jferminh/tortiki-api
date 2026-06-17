package com.tortiki.api.domain.exception;

/**
 * Exception levée lorsqu'un vendeur tente de contacter sa propre annonce.
 *
 * <p>Règle métier Tortiki : l'acheteur et le vendeur d'une même annonce
 * ne peuvent pas être la même personne.</p>
 */
public class SelfContactException extends RuntimeException {

  /**
   * Construit l'exception avec un message explicite.
   *
   * @param message description de la violation
   */
  public SelfContactException(String message) {
    super(message);
  }
}