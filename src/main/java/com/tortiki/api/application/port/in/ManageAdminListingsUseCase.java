package com.tortiki.api.application.port.in;

import com.tortiki.api.domain.model.Listing;
import java.util.List;

/**
 * Port primaire des cas d'usage réservés à l'administration pour la modération
 * des annonces.
 *
 * <p>Contrairement à {@code SearchListingsUseCase} (public, annonces actives
 * uniquement), ce port expose toutes les annonces quel que soit leur statut,
 * et autorise le changement de statut par un administrateur.</p>
 */
public interface ManageAdminListingsUseCase {

  /**
   * Récupère toutes les annonces de la plateforme, tous vendeurs et tous
   * statuts confondus.
   *
   * @return la liste complète des annonces, jamais {@code null}
   */
  List<Listing> findAll();

  /**
   * Modifie le statut d'une annonce (activation, désactivation).
   *
   * @param listingId identifiant de l'annonce à modifier
   * @param newStatus nouveau statut souhaité
   * @return l'annonce avec son statut mis à jour
   */
  Listing updateStatus(Long listingId, String newStatus);
}