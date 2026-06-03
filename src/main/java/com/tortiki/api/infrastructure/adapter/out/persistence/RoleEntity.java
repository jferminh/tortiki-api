package com.tortiki.api.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité JPA représentant un rôle de la plateforme Tortiki.
 *
 * <p>Correspond à la table {@code roles} en base de données.
 * Les valeurs possibles sont {@code ROLE_ADMIN}, {@code ROLE_SELLER}, {@code ROLE_BUYER}.</p>
 *
 * <p>Cette entité appartient à la couche {@code infrastructure/adapter/out/persistence}
 * et ne doit jamais être exposée au-delà de cette couche.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class RoleEntity {

  /**
   * Identifiant technique auto-incrémenté.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Nom du rôle Spring Security — ex : {@code ROLE_ADMIN}.
   */
  @Column(nullable = false, unique = true, length = 50)
  private String name;
}