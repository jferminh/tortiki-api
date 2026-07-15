package com.tortiki.api.application.port.in;

import com.tortiki.api.domain.model.ContactRequest;
import java.util.List;

/**
 * Port primaire pour la consultation des demandes de contact d'un acheteur.
 *
 * <p>Définit le contrat d'entrée du cas d'usage : un acheteur consulte
 * l'historique de ses propres demandes de contact, tous statuts confondus
 * (PENDING, CONFIRMED, REFUSED). Symétrique côté acheteur du cas d'usage
 * vendeur {@code ManageContactRequestUseCase#findBySeller}.</p>
 *
 * <p>Appartient à la couche {@code application/port/in} — aucune dépendance
 * vers {@code infrastructure} n'est autorisée ici.</p>
 */
public interface FindBuyerContactRequestsUseCase {

  /**
   * Recherche toutes les demandes de contact soumises par un acheteur.
   *
   * <p>Le tri est délégué à l'implémentation (ordre chronologique
   * décroissant recommandé, demandes les plus récentes en premier).</p>
   *
   * @param buyerEmail email de l'acheteur authentifié, résolu par Spring
   *     Security — jamais un identifiant fourni brut par le client
   * @return la liste des demandes de contact de l'acheteur, vide si aucune
   */
  List<ContactRequest> findByBuyer(String buyerEmail);
}