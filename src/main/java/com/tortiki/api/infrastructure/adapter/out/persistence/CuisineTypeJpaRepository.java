package com.tortiki.api.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository Spring Data JPA pour les origines culinaires.
 *
 * <p>Ne dois jamais être injecté hors de {@link CuisineTypeJpaAdapter}.</p>
 */
public interface CuisineTypeJpaRepository
    extends JpaRepository<CuisineTypeJpaEntity, Long> {

  /**
   * Retourne toutes les origines culinaires actives.
   *
   * @return liste des entités JPA avec {@code enabled = true}
   */
  List<CuisineTypeJpaEntity> findByEnabledTrue();

  /**
   * Recherche une origine culinaire par son nom exact.
   *
   * @param name le nom de l'origine (ex : "Ukrainienne")
   * @return un {@link Optional} contenant l'entité si elle existe
   */
  Optional<CuisineTypeJpaEntity> findByName(String name);
}