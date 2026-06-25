package com.tortiki.api.application.port.out;

import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
import java.util.List;
import java.util.Optional;

/**
 * Port secondaire pour la persistance des demandes de contact.
 *
 * <p>Définit le contrat de stockage et de récupération des demandes de contact
 * sans exposer les détails de l'implémentation JPA.</p>
 *
 * <p>Appartient à la couche {@code application/port/out} —
 * aucune dépendance vers {@code infrastructure} n'est autorisée ici.</p>
 */
public interface ContactRequestRepository {

  /**
   * Persiste une nouvelle demande de contact.
   *
   * @param contactRequest la demande de contact à sauvegarder
   * @return la demande sauvegardée avec son identifiant généré
   */
  ContactRequest save(ContactRequest contactRequest);

  /**
   * Recherche une demande de contact par son identifiant.
   *
   * @param id identifiant technique de la demande
   * @return la demande trouvée, ou {@link Optional#empty()} si absente
   */
  Optional<ContactRequest> findById(Long id);

  /**
   * Vérifie si un acheteur a déjà soumis une demande pour une annonce donnée.
   *
   * <p>Utilisé pour appliquer la règle métier d'unicité :
   * un acheteur ne peut soumettre qu'une seule demande par annonce.</p>
   *
   * @param listingId identifiant de l'annonce
   * @param buyerId   identifiant de l'acheteur
   * @return {@code true} si une demande existe déjà
   */
  boolean existsByListingIdAndBuyerId(Long listingId, Long buyerId);

  /**
   * Récupère toutes les demandes reçues pour une annonce donnée.
   *
   * <p>Utilisé par le vendeur pour consulter les demandes sur ses annonces.</p>
   *
   * @param listingId identifiant de l'annonce
   * @return liste des demandes associées, vide si aucune
   */
  List<ContactRequest> findByListingId(Long listingId);

  /**
   * Récupère toutes les demandes émises par un acheteur.
   *
   * @param buyerId identifiant de l'acheteur
   * @return liste des demandes de l'acheteur, vide si aucune
   */
  List<ContactRequest> findByBuyerId(Long buyerId);

  /**
   * Recherche les demandes de contact reçues pour un vendeur donné.
   *
   * @param sellerId identifiant du vendeur
   * @return liste des demandes reçues, triées par date décroissante
   */
  List<ContactRequest> findBySellerId(Long sellerId);

  /**
   * Recherche une demande par son identifiant et le vendeur propriétaire.
   *
   * <p>Garantit qu'un vendeur ne peut accéder qu'aux demandes
   * de ses propres annonces.</p>
   *
   * @param contactRequestId identifiant de la demande
   * @param sellerId         identifiant du vendeur propriétaire
   * @return la demande si trouvée et appartenant au vendeur
   */
  Optional<ContactRequest> findByIdAndSellerId(Long contactRequestId, Long sellerId);

  /**
   * Met à jour le statut d'une demande de contact.
   *
   * @param contactRequestId identifiant de la demande
   * @param newStatus        nouveau statut à appliquer
   * @return la demande mise à jour
   */
  ContactRequest updateStatus(Long contactRequestId, ContactRequestStatus newStatus);
}