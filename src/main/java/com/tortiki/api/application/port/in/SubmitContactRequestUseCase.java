package com.tortiki.api.application.port.in;

import com.tortiki.api.domain.model.ContactRequest;

/**
 * Port primaire pour la soumission d'une demande de contact sur Tortiki.
 *
 * <p>Définit le contrat d'entrée du cas d'usage : un acheteur exprime son intérêt
 * pour une annonce. L'implémentation est assurée par
 * {@code ContactRequestService} dans la couche {@code application/service}.</p>
 *
 * <p>Appartient à la couche {@code application/port/in} —
 * aucune dépendance vers {@code infrastructure} n'est autorisée ici.</p>
 */
public interface SubmitContactRequestUseCase {

  /**
   * Soumet une demande de contact pour une annonce.
   *
   * <p>Applique les règles métier avant persistance :</p>
   * <ul>
   *   <li>L'acheteur ne peut pas contacter sa propre annonce.</li>
   *   <li>Un seul contact par acheteur par annonce (unicité).</li>
   * </ul>
   *
   * @param command données de la demande (listingId, buyerId, message, portions)
   * @return la {@link ContactRequest} créée avec son identifiant et statut {@code PENDING}
   * @throws com.tortiki.api.domain.exception.SelfContactException
   *     si l'acheteur est le vendeur de l'annonce
   * @throws com.tortiki.api.domain.exception.ContactRequestAlreadyExistsException
   *     si l'acheteur a déjà soumis une demande pour cette annonce
   * @throws com.tortiki.api.domain.exception.ListingNotFoundException
   *     si l'annonce n'existe pas
   * @throws com.tortiki.api.domain.exception.UserNotFoundException
   *     si l'acheteur n'existe pas
   */
  ContactRequest submit(SubmitContactRequestUseCase.Command command);

  /**
   * Commande d'entrée pour la soumission d'une demande de contact.
   *
   * <p>Record immuable Java 21 — garantit qu'aucune donnée n'est modifiée
   * entre le controller et le service.</p>
   *
   * @param listingId  identifiant de l'annonce ciblée
   * @param buyerId    identifiant de l'acheteur authentifié
   * @param message    message optionnel laissé par l'acheteur
   * @param portions   nombre de portions souhaitées
   */
  record Command(
      Long listingId,
      Long buyerId,
      String message,
      Integer portions
  ) {}
}