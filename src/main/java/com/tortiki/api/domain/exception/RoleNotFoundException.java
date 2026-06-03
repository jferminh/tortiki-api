package com.tortiki.api.domain.exception;

/**
 * Exception métier levée lorsqu'un rôle demandé est introuvable en base.
 *
 * <p>Indique une incohérence entre le code et la migration Flyway V1.</p>
 */
public class RoleNotFoundException extends RuntimeException {

  /**
   * Construit l'exception avec le nom du rôle manquant.
   *
   * @param roleName le nom du rôle introuvable
   */
  public RoleNotFoundException(String roleName) {
    super("Rôle introuvable en base : " + roleName
        + " — vérifier la migration Flyway V1");
  }
}