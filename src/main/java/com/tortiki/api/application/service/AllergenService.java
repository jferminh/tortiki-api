package com.tortiki.api.application.service;

import com.tortiki.api.application.port.in.ManageAllergenUseCase;
import com.tortiki.api.application.port.out.AllergenRepository;
import com.tortiki.api.domain.exception.AllergenNotFoundException;
import com.tortiki.api.domain.model.Allergen;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service métier gérant la consultation des allergènes.
 *
 * <p>Implémente {@link ManageAllergenUseCase}. Dépend uniquement du port
 * secondaire {@link AllergenRepository} — aucune dépendance directe vers
 * JPA ou HTTP, conformément à l'architecture hexagonale.</p>
 *
 * <p>Règles métier appliquées :</p>
 * <ul>
 *   <li>Le référentiel des allergènes est conforme au règlement INCO
 *   EU n°1169/2011 et administrable uniquement par {@code ROLE_ADMIN}.</li>
 *   <li>La consultation ({@code findAll}, {@code findById}) reste
 *   publique — aucune restriction de rôle n'est appliquée en lecture.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AllergenService implements ManageAllergenUseCase {

  private static final String ALLERGEN_NOT_FOUND = "Allergène introuvable";

  private final AllergenRepository allergenRepository;

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public List<Allergen> findAll() {
    log.debug("Récupération de tous les allergènes");
    return allergenRepository.findAll();
  }

  /** {@inheritDoc} */
  @Override
  @Transactional(readOnly = true)
  public Allergen findById(Long id) {
    log.debug("Récupération de l'allergène id {}", id);
    return allergenRepository.findById(id)
        .orElseThrow(() -> new AllergenNotFoundException(id));
  }
}