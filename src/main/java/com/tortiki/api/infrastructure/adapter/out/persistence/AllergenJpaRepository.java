package com.tortiki.api.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository Spring Data JPA pour les allergènes.
 *
 * <p>Ne dois jamais être injecté hors de {@link AllergenJpaAdapter}.</p>
 */
public interface AllergenJpaRepository extends JpaRepository<AllergenJpaEntity, Long> {}