package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.application.port.out.ContactRequestRepository;
import com.tortiki.api.domain.model.ContactRequest;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * Adaptateur secondaire — implémentation JPA de {@link ContactRequestRepository}.
 *
 * <p>Traduit les appels du port secondaire en requêtes Spring Data JPA.
 * La couche application ne connaît jamais cette classe concrète —
 * elle dépend uniquement de l'interface {@link ContactRequestRepository}.</p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ContactRequestRepositoryAdapter implements ContactRequestRepository {

  /** Repository Spring Data JPA — accès direct à la table contact_requests. */
  private final ContactRequestJpaRepository jpaRepository;

  /** {@inheritDoc} */
  @Override
  public ContactRequest save(ContactRequest contactRequest) {
    log.debug("Persistance demande de contact — listing {} buyer {}",
        contactRequest.getListing().getId(), contactRequest.getBuyer().getId());
    ContactRequestJpaEntity entity = ContactRequestPersistenceMapper.toEntity(contactRequest);
    ContactRequestJpaEntity saved = jpaRepository.save(entity);
    log.info("Demande de contact {} persistée avec statut {}",
        saved.getId(), saved.getStatus());
    return ContactRequestPersistenceMapper.toDomain(saved);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<ContactRequest> findById(Long id) {
    log.debug("Recherche demande de contact par id : {}", id);
    return jpaRepository.findById(id).map(ContactRequestPersistenceMapper::toDomain);
  }

  /** {@inheritDoc} */
  @Override
  public boolean existsByListingIdAndBuyerId(Long listingId, Long buyerId) {
    boolean exists = jpaRepository.existsByListingIdAndBuyerId(listingId, buyerId);
    log.debug("Vérification doublon listing {} buyer {} → {}", listingId, buyerId, exists);
    return exists;
  }

  /** {@inheritDoc} */
  @Override
  public List<ContactRequest> findByListingId(Long listingId) {
    log.debug("Recherche demandes pour l'annonce : {}", listingId);
    return jpaRepository.findByListingId(listingId)
        .stream()
        .map(ContactRequestPersistenceMapper::toDomain)
        .toList();
  }

  /** {@inheritDoc} */
  @Override
  public List<ContactRequest> findByBuyerId(Long buyerId) {
    log.debug("Recherche demandes pour l'acheteur : {}", buyerId);
    return jpaRepository.findByBuyerId(buyerId)
        .stream()
        .map(ContactRequestPersistenceMapper::toDomain)
        .toList();
  }
}