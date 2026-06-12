package com.tortiki.api.application.port.in;

import com.tortiki.api.domain.model.ContactRequest;

/**
 * Port primaire — cas d'usage de soumission d'une demande de contact.
 *
 * <p>Définit le contrat entre {@code ContactRequestController} et
 * {@code ContactRequestService}. Un acheteur ({@code ROLE_BUYER})
 * exprime son intérêt pour une annonce via ce port.</p>
 *
 * <p>Règles métier vérifiées par l'implémentation :</p>
 * <ul>
 *   <li>L'acheteur ne peut pas contacter sa propre annonce</li>
 *   <li>Une seule demande active par acheteur et par annonce</li>
 * </ul>
 */
public interface SubmitContactRequestUseCase {

  /**
   * Soumet une demande d'intérêt pour une annonce.
   *
   * @param listingId identifiant de l'annonce ciblée
   * @param buyerId   identifiant de l'acheteur connecté
   * @param message   message optionnel laissé au vendeur
   * @param portions  nombre de portions souhaitées
   * @return la demande de contact créée avec statut {@code PENDING}
   */
  ContactRequest submit(Long listingId, Long buyerId, String message, Integer portions);
}