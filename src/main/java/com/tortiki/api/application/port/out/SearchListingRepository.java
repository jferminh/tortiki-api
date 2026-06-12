package com.tortiki.api.application.port.out;

import com.tortiki.api.application.port.in.SearchCriteria;
import com.tortiki.api.domain.model.Listing;
import java.util.List;

/**
 * Port secondaire — contrat de recherche d'annonces en base de données.
 *
 * <p>Définit le contrat entre la couche {@code application/service/}
 * et l'adaptateur JPA {@code ListingSearchRepositoryAdapter} dans
 * {@code infrastructure/adapter/out/persistence/}.</p>
 *
 * <p>Le service applicatif ne connaît pas JPA — il délègue la requête
 * à ce port et reçoit des objets domaine {@link Listing}.</p>
 */
public interface SearchListingRepository {

  /**
   * Recherche les annonces actives en base selon les critères fournis.
   *
   * <p>Les coordonnées GPS dans {@code criteria} sont déjà géocodées
   * par le service avant l'appel à cette méthode.</p>
   *
   * @param criteria critères de recherche enrichis avec latitude/longitude
   * @return liste d'annonces correspondantes, jamais {@code null}
   */
  List<Listing> search(SearchCriteria criteria);
}