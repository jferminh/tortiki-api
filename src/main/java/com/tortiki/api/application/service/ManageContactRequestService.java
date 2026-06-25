package com.tortiki.api.application.service;

import com.tortiki.api.application.port.in.ManageContactRequestUseCase;
import com.tortiki.api.application.port.out.ContactRequestRepository;
import com.tortiki.api.application.port.out.UserRepository;
import com.tortiki.api.domain.exception.ContactRequestNotFoundException;
import com.tortiki.api.domain.exception.InvalidStatusTransitionException;
import com.tortiki.api.domain.exception.UserNotFoundException;
import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.ContactRequestStatus;
import com.tortiki.api.domain.model.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service applicatif de gestion des demandes de contact côté vendeur.
 *
 * <p>Implémente {@link ManageContactRequestUseCase} et applique les règles métier
 * de transition de statut : seul le vendeur propriétaire peut confirmer ou refuser,
 * et les statuts finaux sont irréversibles.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ManageContactRequestService implements ManageContactRequestUseCase {

  private final ContactRequestRepository contactRequestRepository;
  private final UserRepository userRepository;

  /**
   * {@inheritDoc}
   *
   * <p>Résout le vendeur depuis son email, puis délègue la recherche au repository.</p>
   */
  @Override
  public List<ContactRequest> findBySeller(String sellerEmail) {
    log.debug("Récupération des demandes pour le vendeur {}", sellerEmail);
    User seller = resolveUser(sellerEmail);
    List<ContactRequest> requests = contactRequestRepository.findBySellerId(seller.getId());
    log.info("Vendeur {} : {} demande(s) trouvée(s)", sellerEmail, requests.size());
    return requests;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Vérifie que la demande appartient bien au vendeur authentifié,
   * contrôle la validité de la transition de statut, puis persiste la mise à jour.</p>
   */
  @Override
  public ContactRequest updateStatus(UpdateStatusCommand command) {
    log.debug(
        "Mise à jour statut demande #{} → {} par {}",
        command.contactRequestId(),
        command.newStatus(),
        command.sellerEmail()
    );

    User seller = resolveUser(command.sellerEmail());

    ContactRequest contactRequest = contactRequestRepository
        .findByIdAndSellerId(command.contactRequestId(), seller.getId())
        .orElseThrow(() -> new ContactRequestNotFoundException(command.contactRequestId()));

    validateTransition(contactRequest.getStatus(), command.newStatus());

    ContactRequest updated = contactRequestRepository
        .updateStatus(command.contactRequestId(), command.newStatus());

    log.info(
        "Demande #{} : statut {} → {} par {}",
        command.contactRequestId(),
        contactRequest.getStatus(),
        command.newStatus(),
        command.sellerEmail()
    );

    return updated;
  }

  /**
   * Résout un utilisateur depuis son email.
   *
   * @param email email de l'utilisateur à résoudre
   * @return l'utilisateur correspondant
   * @throws UserNotFoundException si aucun utilisateur actif n'est trouvé
   */
  private User resolveUser(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new UserNotFoundException(email));
  }

  /**
   * Valide qu'une transition de statut est autorisée.
   *
   * <p>Règle métier : {@code CONFIRMED} et {@code REFUSED} sont des statuts finaux.
   * Seul {@code PENDING} peut évoluer.</p>
   *
   * @param current statut actuel
   * @param target  statut cible
   * @throws InvalidStatusTransitionException si la transition est interdite
   */
  private void validateTransition(
      ContactRequestStatus current,
      ContactRequestStatus target) {
    if (current == ContactRequestStatus.CONFIRMED
        || current == ContactRequestStatus.REFUSED) {
      throw new InvalidStatusTransitionException(current, target);
    }
  }
}