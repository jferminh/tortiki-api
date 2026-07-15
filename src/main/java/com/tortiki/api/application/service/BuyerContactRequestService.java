package com.tortiki.api.application.service;

import com.tortiki.api.application.port.in.FindBuyerContactRequestsUseCase;
import com.tortiki.api.application.port.out.ContactRequestRepository;
import com.tortiki.api.application.port.out.UserRepository;
import com.tortiki.api.domain.exception.UserNotFoundException;
import com.tortiki.api.domain.model.ContactRequest;
import com.tortiki.api.domain.model.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service applicatif pour la consultation des demandes de contact d'un acheteur.
 *
 * <p>Implémente {@link FindBuyerContactRequestsUseCase}. Résout l'identifiant
 * de l'acheteur depuis son email — le port {@code in} ne reçoit jamais
 * d'identifiant technique fourni par le client, uniquement l'email de session
 * Spring Security, conformément au pattern déjà validé sur
 * {@code ContactRequestService.submit}.</p>
 *
 * <p>Dépend uniquement des ports secondaires {@link ContactRequestRepository}
 * et {@link UserRepository} — aucune dépendance vers {@code infrastructure}.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BuyerContactRequestService implements FindBuyerContactRequestsUseCase {

  private final ContactRequestRepository contactRequestRepository;
  private final UserRepository userRepository;

  /**
   * {@inheritDoc}
   *
   * @throws UserNotFoundException si aucun compte actif ne correspond à l'email
   */
  @Override
  @Transactional(readOnly = true)
  public List<ContactRequest> findByBuyer(String buyerEmail) {
    User buyer = userRepository.findByEmailAndEnabledTrue(buyerEmail)
        .orElseThrow(() -> {
          log.warn("Acheteur introuvable ou inactif pour la consultation de son historique");
          return new UserNotFoundException(buyerEmail);
        });

    List<ContactRequest> requests = contactRequestRepository.findByBuyerId(buyer.getId());
    log.info("Historique consulté — acheteur={} nombre de demandes={}",
        buyer.getId(), requests.size());

    return requests;
  }
}