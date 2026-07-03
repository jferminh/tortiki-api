package com.tortiki.api.application.port.out;

import com.tortiki.api.domain.model.Allergen;
import java.util.List;
import java.util.Optional;

/**
 * Port secondaire — contrat de persistance des allergènes.
 *
 * <p>L'implémentation est assurée par {@code AllergenJpaAdapter} dans
 * {@code infrastructure/adapter/out/persistence/}.</p>
 */
public interface AllergenRepository {

  /**
   * Retourne tous les allergènes dont l'identifiant est dans la liste fournie.
   *
   * @param ids liste des identifiants
   * @return liste des allergènes trouvés
   */
  List<Allergen> findAllByIdIn(List<Long> ids);

  /**
   * Retourne tous les allergènes disponibles.
   *
   * @return liste complète des allergènes
   */
  List<Allergen> findAll();

  /**
   * Recherche un allergène par son identifiant.
   *
   * @param id identifiant de l'allergène
   * @return l'allergène ou vide
   */
  Optional<Allergen> findById(Long id);

  /**
   * Persiste un allergène, qu'il s'agisse d'une création ou d'une mise à jour.
   *
   * <p>Utilisé par {@code AllergenService.create} pour la création
   * initiale, et par {@code AllergenService.delete} pour enregistrer
   * le basculement de {@code enabled} à {@code false}.</p>
   *
   * @param allergen allergène à persister
   * @return l'allergène persisté, avec son identifiant généré si création
   */
  Allergen save(Allergen allergen);
}