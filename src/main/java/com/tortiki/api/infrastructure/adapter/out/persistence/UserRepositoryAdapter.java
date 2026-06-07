package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.application.port.out.UserRepository;
import com.tortiki.api.domain.model.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * Adaptateur secondaire de persistance pour les utilisateurs.
 *
 * <p>Implémente le port {@link UserRepository} défini dans la couche
 * {@code application}. Délègue les opérations à {@link UserJpaRepository}
 * (Spring Data JPA) et traduit les entités via {@link UserMapper}.</p>
 *
 * <p>Cette classe est le seul point de contact entre la couche
 * {@code application} et JPA pour l'entité {@code User}.</p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

  /** Repository Spring Data JPA. */
  private final UserJpaRepository userJpaRepository;

  /** Mapper domaine ↔ entité JPA. */
  private final UserMapper userMapper;

  /**
   * {@inheritDoc}
   *
   * <p>Traduit le POJO domaine en entité JPA, persiste, puis retraduit
   * en POJO domaine avec l'identifiant généré.</p>
   */
  @Override
  public User save(User user) {
    UserEntity entity = userMapper.toEntity(user);
    UserEntity saved = userJpaRepository.save(entity);
    log.debug("Utilisateur persisté : id={}, email={}", saved.getId(), saved.getEmail());
    return userMapper.toDomain(saved);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Recherche un utilisateur par email, actif ou non.
   * Ne filtre PAS les comptes désactivés — usage métier général.</p>
   */
  @Override
  public Optional<User> findByEmail(String email) {
    return userJpaRepository.findByEmail(email)
        .map(userMapper::toDomain);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<User> findById(Long id) {
    return userJpaRepository.findById(id)
        .map(userMapper::toDomain);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean existsByEmail(String email) {
    return userJpaRepository.existsByEmail(email);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Utilisé exclusivement par Spring Security pour rejeter
   * les comptes désactivés directement au niveau de la requête.</p>
   */
  @Override
  public Optional<User> findByEmailAndEnabledTrue(String email) {
    return userJpaRepository.findByEmailAndEnabledTrue(email)
        .map(userMapper::toDomain);
  }
}