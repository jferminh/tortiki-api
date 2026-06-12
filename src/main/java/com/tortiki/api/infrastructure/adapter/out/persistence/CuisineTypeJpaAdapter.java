// infrastructure/adapter/out/persistence/CuisineTypeJpaAdapter.java
package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.application.port.out.CuisineTypeRepository;
import com.tortiki.api.domain.model.CuisineType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adaptateur secondaire de persistance des origines culinaires.
 *
 * <p>Implémente {@link CuisineTypeRepository} (port out) en déléguant
 * à {@link CuisineTypeJpaRepository} (Spring Data JPA).</p>
 */
@Component
@RequiredArgsConstructor
public class CuisineTypeJpaAdapter implements CuisineTypeRepository {

  private final CuisineTypeJpaRepository jpaRepository;
  private final CuisineTypePersistenceMapper mapper;

  /** {@inheritDoc} */
  @Override
  public Optional<CuisineType> findById(Long id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  /** {@inheritDoc} */
  @Override
  public List<CuisineType> findAll() {
    return jpaRepository.findAll()
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  /** {@inheritDoc} */
  @Override
  public List<CuisineType> findAllEnabled() {
    return jpaRepository.findByEnabledTrue()
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  /** {@inheritDoc} */
  @Override
  public CuisineType save(CuisineType cuisineType) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(cuisineType)));
  }
}