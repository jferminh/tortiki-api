package com.tortiki.api.application.service;

import com.tortiki.api.application.port.in.ManageCuisineTypeUseCase;
import com.tortiki.api.application.port.out.CuisineTypeRepository;
import com.tortiki.api.domain.exception.CuisineTypeInUseException;
import com.tortiki.api.domain.exception.CuisineTypeNotFoundException;
import com.tortiki.api.domain.model.CuisineType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier gérant le référentiel des origines culinaires.
 *
 * <p>Implémente le port primaire {@link ManageCuisineTypeUseCase}.
 * Dépend uniquement du port secondaire {@link CuisineTypeRepository} —
 * aucune dépendance directe vers JPA ou la base de données.</p>
 *
 * <p>Les opérations de création, modification et suppression sont
 * réservées au rôle {@code ROLE_ADMIN}. La vérification est faite
 * par Spring Security en amont dans le contrôleur.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CuisineTypeService implements ManageCuisineTypeUseCase {

  /**
   * Message d'erreur pour une origine culinaire introuvable.
   */
  private static final String CUISINE_TYPE_NOT_FOUND =
      "Origine culinaire introuvable pour l'identifiant : ";

  /**
   * Message d'erreur pour une suppression bloquée par des annonces actives.
   */
  private static final String CUISINE_TYPE_IN_USE =
      "Impossible de supprimer l'origine culinaire id=";

  /**
   * Port secondaire de persistance des origines culinaires.
   */
  private final CuisineTypeRepository cuisineTypeRepository;

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  public CuisineType create(String name, String description) {
    log.debug("Création d'une origine culinaire : {}", name);
    CuisineType cuisineType = new CuisineType();
    cuisineType.setName(name);
    cuisineType.setDescription(description);
    CuisineType saved = cuisineTypeRepository.save(cuisineType);
    log.info("Origine culinaire créée : {}", name);
    return saved;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional
  public CuisineType update(Long id, String name, String description) {
    log.debug("Mise à jour de l'origine culinaire id={}", id);
    CuisineType existing = cuisineTypeRepository.findById(id)
        .orElseThrow(() -> new CuisineTypeNotFoundException(
            CUISINE_TYPE_NOT_FOUND + id
        ));
    existing.setName(name);
    existing.setDescription(description);
    CuisineType updated = cuisineTypeRepository.save(existing);
    log.info("Origine culinaire mise à jour : id={}", id);
    return updated;
  }

  /**
   * {@inheritDoc}
   *
   * <p>La suppression est bloquée si une annonce active référence
   * cette origine culinaire — cohérence métier garantie.</p>
   */
  @Override
  @Transactional
  public void delete(Long id) {
    log.debug("Suppression de l'origine culinaire id={}", id);
    cuisineTypeRepository.findById(id)
        .orElseThrow(() -> new CuisineTypeNotFoundException(
            CUISINE_TYPE_NOT_FOUND + id
        ));
    if (cuisineTypeRepository.isUsedByActiveListing(id)) {
      throw new CuisineTypeInUseException(
          CUISINE_TYPE_IN_USE + id + " : des annonces actives la référencent"
      );
    }
    cuisineTypeRepository.deleteById(id);
    log.info("Origine culinaire supprimée : id={}", id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(readOnly = true)
  public List<CuisineType> findAll() {
    log.debug("Récupération de toutes les origines culinaires");
    return cuisineTypeRepository.findAll();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Transactional(readOnly = true)
  public CuisineType findById(Long id) {
    log.debug("Recherche origine culinaire id={}", id);
    return cuisineTypeRepository.findById(id)
        .orElseThrow(() -> new CuisineTypeNotFoundException(
            CUISINE_TYPE_NOT_FOUND + id
        ));
  }
}