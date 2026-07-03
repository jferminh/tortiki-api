package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.application.port.out.AllergenRepository;
import com.tortiki.api.domain.model.Allergen;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adaptateur secondaire de persistance des allergènes.
 *
 * <p>Implémente {@link AllergenRepository} (port out) en déléguant
 * à {@link AllergenJpaRepository} (Spring Data JPA).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AllergenJpaAdapter implements AllergenRepository {

  private final AllergenJpaRepository jpaRepository;
  private final AllergenPersistenceMapper mapper;

  /** {@inheritDoc} */
  @Override
  public List<Allergen> findAllByIdIn(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return jpaRepository.findAllById(ids)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  /** {@inheritDoc} */
  @Override
  public List<Allergen> findAll() {
    return jpaRepository.findAll()
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Allergen> findById(Long id) {
    return jpaRepository.findById(id).map(mapper::toDomain);
  }

  /** {@inheritDoc} */
  @Override
  public Allergen save(Allergen allergen) {
    log.debug("Persistance de l'allergène id={} name={}", allergen.getId(), allergen.getName());
    final AllergenJpaEntity entity = mapper.toEntity(allergen);
    final AllergenJpaEntity saved = jpaRepository.save(entity);
    log.debug("Allergène persisté avec l'id {}", saved.getId());
    return mapper.toDomain(saved);
  }
}