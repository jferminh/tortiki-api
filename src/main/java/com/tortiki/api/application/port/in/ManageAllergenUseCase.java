package com.tortiki.api.application.port.in;

import com.tortiki.api.domain.model.Allergen;
import java.util.List;

/**
 * Port primaire définissant les cas d'usage de consultation des allergènes.
 *
 * <p>Référentiel réglementaire INCO EU n°1169/2011 — les 14 allergènes
 * doivent être consultables publiquement pour toute annonce de plat.
 * La gestion (création/modification) est réservée à {@code ROLE_ADMIN}
 * et sera ajoutée dans une itération ultérieure si le besoin est confirmé.</p>
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
}