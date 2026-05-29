package com.tortiki.api.application.port.in;

import com.tortiki.api.domain.model.CuisineType;
import java.util.List;

/**
 * Port primaire — cas d'usage : gestion du référentiel des origines culinaires.
 *
 * <p>Les opérations de création, modification et suppression sont
 * réservées au rôle {@code ROLE_ADMIN}. La consultation est publique.</p>
 *
 * <p>Ce port est appelé par {@code CuisineTypeController} dans
 * {@code infrastructure/adapter/in/web/}.</p>
 */
public interface ManageCuisineTypeUseCase {

  /**
   * Crée une nouvelle origine culinaire dans le référentiel.
   *
   * @param name        nom unique de l'origine culinaire
   * @param description description optionnelle
   * @return l'origine culinaire créée
   */
  CuisineType create(String name, String description);

  /**
   * Met à jour une origine culinaire existante.
   *
   * @param id          identifiant de l'origine culinaire
   * @param name        nouveau nom
   * @param description nouvelle description
   * @return l'origine culinaire mise à jour
   */
  CuisineType update(Long id, String name, String description);

  /**
   * Supprime une origine culinaire du référentiel.
   *
   * <p>La suppression n'est possible que si aucune annonce active
   * n'utilise cette origine culinaire.</p>
   *
   * @param id identifiant de l'origine culinaire à supprimer
   */
  void delete(Long id);

  /**
   * Retourne toutes les origines culinaires disponibles.
   *
   * @return liste complète des origines culinaires, vide si aucune
   */
  List<CuisineType> findAll();

  /**
   * Retourne une origine culinaire par son identifiant.
   *
   * @param id identifiant de l'origine culinaire
   * @return l'origine culinaire correspondante
   * @throws com.tortiki.api.domain.exception.CuisineTypeNotFoundException
   *         si l'origine culinaire est introuvable
   */
  CuisineType findById(Long id);
}