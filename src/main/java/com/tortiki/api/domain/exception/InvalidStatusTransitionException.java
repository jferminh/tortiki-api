package com.tortiki.api.domain.exception;

import com.tortiki.api.domain.model.ContactRequestStatus;

/**
 * Levée lorsqu'une transition de statut est interdite sur une demande de contact.
 *
 * <p>Les statuts {@code CONFIRMED} et {@code REFUSED} sont des statuts finaux —
 * aucune transition n'est autorisée depuis ces états.</p>
 */
public class InvalidStatusTransitionException extends RuntimeException {

  /**
   * Crée l'exception avec les statuts source et cible.
   *
   * @param current statut actuel de la demande
   * @param target  statut cible refusé
   */
  public InvalidStatusTransitionException(
      ContactRequestStatus current,
      ContactRequestStatus target) {
    super("Transition interdite : " + current + " → " + target
        + ". Les statuts CONFIRMED et REFUSED sont définitifs.");
  }
}