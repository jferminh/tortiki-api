package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.application.port.out.ContactRequestRepository;
import com.tortiki.api.domain.exception.ContactRequestNotFoundException;
import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
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
  private final ContactRequestJpaRepository contactRequestJpaRepository;

  /** {@inheritDoc} */
  @Override
  public ContactRequest save(final ContactRequest contactRequest) {
    log.debug("Persistance demande de contact — listing {} buyer {}",
        contactRequest.getListing().getId(), contactRequest.getBuyer().getId());
    ContactRequestJpaEntity entity = ContactRequestPersistenceMapper.toEntity(contactRequest);
    ContactRequestJpaEntity saved = contactRequestJpaRepository.save(entity);
    log.info("Demande de contact {} persistée avec statut {}",
        saved.getId(), saved.getStatus());
    return ContactRequestPersistenceMapper.toDomain(saved);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<ContactRequest> findById(final Long id) {
    log.debug("Recherche demande de contact par id : {}", id);
    return contactRequestJpaRepository.findById(id).map(ContactRequestPersistenceMapper::toDomain);
  }

  /** {@inheritDoc} */
  @Override
  public boolean existsByListingIdAndBuyerId(final Long listingId, final Long buyerId) {
    boolean exists = contactRequestJpaRepository.existsByListingIdAndBuyerId(listingId, buyerId);
    log.debug("Vérification doublon listing {} buyer {} → {}", listingId, buyerId, exists);
    return exists;
  }

  /** {@inheritDoc} */
  @Override
  public List<ContactRequest> findByListingId(final Long listingId) {
    log.debug("Recherche demandes pour l'annonce : {}", listingId);
    return contactRequestJpaRepository.findByListingId(listingId)
        .stream()
        .map(ContactRequestPersistenceMapper::toDomain)
        .toList();
  }

  /** {@inheritDoc} */
  @Override
  public List<ContactRequest> findByBuyerId(final Long buyerId) {
    log.debug("Recherche demandes pour l'acheteur : {}", buyerId);
    return contactRequestJpaRepository.findByBuyerId(buyerId)
        .stream()
        .map(ContactRequestPersistenceMapper::toDomain)
        .toList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ContactRequest> findBySellerId(final Long sellerId) {
    log.debug("Recherche des demandes pour le vendeur id={}", sellerId);
    return contactRequestJpaRepository.findBySellerId(sellerId)
        .stream()
        .map(ContactRequestPersistenceMapper::toDomain)
        .toList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<ContactRequest> findByIdAndSellerId(
      final Long contactRequestId,
      final Long sellerId) {
    log.debug("Recherche demande #{} pour vendeur id={}", contactRequestId, sellerId);
    return contactRequestJpaRepository.findByIdForSeller(contactRequestId, sellerId)
        .map(ContactRequestPersistenceMapper::toDomain);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ContactRequest updateStatus(
      final Long contactRequestId,
      final ContactRequestStatus newStatus) {
    log.debug("Mise à jour statut demande #{} → {}", contactRequestId, newStatus);
    ContactRequestJpaEntity entity = contactRequestJpaRepository.findById(contactRequestId)
        .orElseThrow(() -> new ContactRequestNotFoundException(contactRequestId));
    entity.setStatus(newStatus);
    ContactRequestJpaEntity saved = contactRequestJpaRepository.save(entity);
    log.info("Demande #{} : statut mis à jour → {}", saved.getId(), saved.getStatus());
    return ContactRequestPersistenceMapper.toDomain(saved);
  }
}