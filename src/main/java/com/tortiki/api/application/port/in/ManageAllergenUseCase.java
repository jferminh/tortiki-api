package com.tortiki.api.application.port.in;

import com.tortiki.api.domain.model.Allergen;
import java.util.List;

/**
 * Port primaire définissant les cas d'usage de gestion des allergènes.
 *
 * <p>Référentiel réglementaire INCO EU n°1169/2011 — les 14 allergènes
 * doivent être consultables publiquement pour toute annonce de plat.
 * La création et la désactivation sont réservées à {@code ROLE_ADMIN},
 * afin de permettre l'ajout d'allergènes non-INCO (ex. spécificités locales)
 * sans jamais supprimer physiquement une référence utilisée par une annonce.</p>
 *
 * <p>Implémenté par {@code AllergenService} dans la couche
 * {@code application/service}. Consommé par l'adaptateur primaire
 * {@code AllergenController} dans {@code infrastructure/adapter/in/web}.</p>
 */
public interface ManageAllergenUseCase {

  /**
   * Retourne la liste complète des allergènes du référentiel.
   *
   * @return liste de tous les allergènes disponibles
   */
  List<Allergen> findAll();

  /**
   * Recherche un allergène par son identifiant.
   *
   * @param id identifiant de l'allergène
   * @return l'allergène correspondant
   * @throws com.tortiki.api.domain.exception.AllergenNotFoundException
   *     si aucun allergène ne correspond à l'identifiant
   */
  Allergen findById(Long id);

  /**
   * Crée un nouvel allergène dans le référentiel.
   *
   * <p>Réservé à {@code ROLE_ADMIN}. L'allergène est créé activé
   * ({@code enabled = true}) par défaut.</p>
   *
   * @param name nom officiel de l'allergène à créer
   * @return l'allergène créé, avec son identifiant généré
   */
  Allergen create(String name);

  /**
   * Désactive un allergène du référentiel.
   *
   * <p>Réservé à {@code ROLE_ADMIN}. La suppression physique n'est
   * jamais effectuée afin de préserver l'intégrité référentielle des
   * annonces existantes qui pointent vers cet allergène via la table
   * {@code listing_allergens}. Cette méthode bascule {@code enabled}
   * à {@code false} plutôt que d'exécuter un {@code DELETE} SQL.</p>
   *
   * @param id identifiant de l'allergène à désactiver
   * @throws com.tortiki.api.domain.exception.AllergenNotFoundException
   *     si aucun allergène ne correspond à l'identifiant
   */
  void delete(Long id);
}