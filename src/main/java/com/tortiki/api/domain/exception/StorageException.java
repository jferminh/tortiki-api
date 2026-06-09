package com.tortiki.api.domain.exception;

/**
 * Exception métier levée en cas d'échec de stockage d'un fichier.
 *
 * <p>Appartient au domaine — indépendante de toute technologie de stockage.</p>
 */
public class StorageException extends RuntimeException {

  /**
   * Construit une exception de stockage avec un message et une cause.
   *
   * @param message description de l'erreur
   * @param cause   exception technique d'origine
   */
  public StorageException(String message, Throwable cause) {
    super(message, cause);
  }
}