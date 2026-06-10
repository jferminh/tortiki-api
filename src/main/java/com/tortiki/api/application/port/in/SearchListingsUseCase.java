package com.tortiki.api.application.port.in;

import com.tortiki.api.application.port.out.GeolocationPort;
import com.tortiki.api.domain.model.Listing;
import java.util.List;

/**
 * Port primaire — cas d'usage de recherche d'annonces.
 *
 * <p>Définit le contrat entre la couche {@code infrastructure/adapter/in/web/}
 * et la couche {@code application/service/}. Le contrôleur REST ne connaît
 * que cette interface — jamais l'implémentation concrète.</p>
 *
 * <p>La géolocalisation de la ville est déléguée en interne au port secondaire
 * {@link GeolocationPort} — transparente pour l'appelant.</p>
 */
public interface SearchListingsUseCase {

  /**
   * Recherche les annonces actives correspondant aux critères fournis.
   *
   * <p>Si {@code criteria.city()} est renseigné, la ville est géocodée
   * via {@link GeolocationPort} avant la requête en base. Si la ville
   * est inconnue de Nominatim, la liste retournée est vide.</p>
   *
   * @param criteria critères de recherche et de filtrage
   * @return liste paginée d'annonces correspondantes, jamais {@code null}
   */
  List<Listing> search(SearchCriteria criteria);
}