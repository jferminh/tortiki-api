package com.tortiki.api.application.port.out;

import com.tortiki.api.domain.model.ContactRequest;
import java.util.Optional;

/**
 * Port secondaire — contrat de persistance des demandes de contact.
 *
 * <p>Définit le contrat entre {@code ContactRequestService} et
 * l'adaptateur JPA {@code ContactRequestRepositoryAdapter} dans
 * {@code infrastructure/adapter/out/persistence/}.</p>
 */
public interface ContactRequestRepository {

  /**
   * Persiste une nouvelle demande de contact.
   *
   * @param contactRequest demande à sauvegarder
   * @return demande sauvegardée avec identifiant généré
   */
  ContactRequest save(ContactRequest contactRequest);

  /**
   * Recherche une demande par son identifiant technique.
   *
   * @param id identifiant de la demande
   * @return demande ou {@code Optional.empty()} si introuvable
   */
  Optional<ContactRequest> findById(Long id);

  /**
   * Vérifie l'existence d'une demande pour un acheteur et une annonce donnés.
   *
   * <p>Utilisé pour appliquer la règle métier d'unicité avant
   * d'atteindre la contrainte {@code UNIQUE(listing_id, buyer_id)} SQL.</p>
   *
   * @param listingId identifiant de l'annonce
   * @param buyerId   identifiant de l'acheteur
   * @return {@code true} si une demande existe déjà
   */
  boolean existsByListingIdAndBuyerId(Long listingId, Long buyerId);
}