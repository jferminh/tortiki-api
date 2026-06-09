package com.tortiki.api.domain.exception;

/**
 * Exception levée lorsqu'une demande de contact est introuvable en base.
 *
 * <p>Utilisée par les services de la couche {@code application/service/}
 * lors de la récupération ou de la mise à jour d'une demande.</p>
 */
public class ContactRequestNotFoundException extends RuntimeException {

  /**
   * Construit l'exception avec l'identifiant de la demande introuvable.
   *
   * @param id identifiant de la demande introuvable
   */
  public ContactRequestNotFoundException(Long id) {
    super("Demande de contact introuvable avec l'identifiant : " + id);
  }
}