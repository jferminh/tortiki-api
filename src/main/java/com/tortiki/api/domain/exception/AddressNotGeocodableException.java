package com.tortiki.api.domain.exception;

/**
 * Exception levée lorsqu'une adresse de retrait ne peut pas être géolocalisée.
 *
 * <p>Exception métier du domaine : ne dépend d'aucun framework. Traduite en
 * réponse HTTP 422 par {@code GlobalExceptionHandler}. Empêche la création
 * ou la modification d'une annonce sans ville résolue — la colonne
 * {@code listings.city} porte une contrainte {@code NOT NULL} (issue #147,
 * recherche par ville).</p>
 */
public class AddressNotGeocodableException extends RuntimeException {

  /**
   * Construit l'exception avec un message descriptif.
   *
   * @param message description de l'erreur
   */
  public AddressNotGeocodableException(String message) {
    super(message);
  }
}