package com.tortiki.api.application.port.out;

import com.tortiki.api.domain.model.CuisineType;
import java.util.List;
import java.util.Optional;

/**
 * Port secondaire — contrat de persistance des origines culinaires.
 *
 * <p>Utilisé par {@code CuisineTypeService} et {@code ListingService}
 * pour résoudre les origines culinaires lors de la création
 * ou modification d'une annonce.</p>
 */
public interface CuisineTypeRepository {

  /**
   * Persiste une origine culinaire ou met à jour une existante.
   *
   * @param cuisineType origine culinaire à persister
   * @return l'origine culinaire persistée
   */
  CuisineType save(CuisineType cuisineType);

  /**
   * Recherche une origine culinaire par son identifiant.
   *
   * @param id identifiant de l'origine culinaire
   * @return un {@link Optional} contenant l'origine, ou vide si absente
   */
  Optional<CuisineType> findById(Long id);

  /**
   * Retourne toutes les origines culinaires disponibles.
   *
   * @return liste complète, vide si aucune
   */
  List<CuisineType> findAll();

  /**
   * Vérifie si une origine culinaire est utilisée par au moins une annonce active.
   *
   * <p>Utilisé avant suppression pour éviter les incohérences métier.</p>
   *
   * @param id identifiant de l'origine culinaire
   * @return {@code true} si au moins une annonce active la référence
   */
  boolean isUsedByActiveListing(Long id);

  /**
   * Supprime une origine culinaire par son identifiant.
   *
   * @param id identifiant de l'origine culinaire à supprimer
   */
  void deleteById(Long id);
}