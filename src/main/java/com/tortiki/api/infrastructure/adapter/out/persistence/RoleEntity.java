package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.RoleName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Les valeurs stockées sont {@code ADMIN}, {@code SELLER}, {@code BUYER}
 * — sans le préfixe {@code ROLE_} qui est ajouté par Spring Security
 * au moment de la construction des {@code GrantedAuthority}.</p>
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
   * Nom du rôle — valeur de l'énumération {@link RoleName}.
   * Stocké en base sans préfixe : {@code ADMIN}, {@code SELLER}, {@code BUYER}.
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, unique = true, length = 20)
  private RoleName name;
}