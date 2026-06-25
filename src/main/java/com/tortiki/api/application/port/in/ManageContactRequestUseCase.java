package com.tortiki.api.application.port.in;

import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
import java.util.List;

/**
 * Port primaire — cas d'usage de gestion des demandes de contact côté vendeur.
 *
 * <p>Permet au vendeur de consulter les demandes reçues sur ses annonces
 * et de les confirmer ou refuser.</p>
 *
 * <p>Implémenté par {@code ManageContactRequestService} dans la couche application.</p>
 */
public interface ManageContactRequestUseCase {

  /**
   * Récupère toutes les demandes de contact reçues par un vendeur.
   *
   * @param sellerEmail email du vendeur authentifié
   * @return liste des demandes reçues, triées par date décroissante
   */
  List<ContactRequest> findBySeller(String sellerEmail);

  /**
   * Met à jour le statut d'une demande de contact.
   *
   * <p>Seul le vendeur propriétaire de l'annonce concernée peut modifier le statut.
   * Transition autorisée : {@code PENDING} → {@code CONFIRMED} ou {@code REFUSED}.
   * Les statuts finaux {@code CONFIRMED} et {@code REFUSED} ne peuvent plus évoluer.</p>
   *
   * @param command commande immuable contenant les paramètres de mise à jour
   * @return la demande de contact mise à jour
   */
  ContactRequest updateStatus(UpdateStatusCommand command);

  /**
   * Commande immuable pour la mise à jour du statut d'une demande de contact.
   *
   * @param contactRequestId identifiant de la demande à mettre à jour
   * @param sellerEmail      email du vendeur authentifié (contrôle d'accès métier)
   * @param newStatus        nouveau statut souhaité
   */
  record UpdateStatusCommand(
      Long contactRequestId,
      String sellerEmail,
      ContactRequestStatus newStatus
  ) {}
}