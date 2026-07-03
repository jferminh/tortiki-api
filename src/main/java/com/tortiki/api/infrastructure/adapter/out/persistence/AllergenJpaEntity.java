package com.tortiki.api.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité JPA représentant un allergène réglementaire EU.
 *
 * <p>Référentiel des 14 allergènes définis par le règlement
 * INCO EU n°1169/2011. Administrable via {@code ROLE_ADMIN}.</p>
 *
 * <p>Le champ {@code enabled} permet une désactivation logique
 * (voir migration {@code V6__add_allergen_enabled.sql}) sans
 * jamais supprimer physiquement la ligne, afin de préserver
 * l'intégrité référentielle de {@code listing_allergens}.</p>
 *
 * <p>Appartient exclusivement à la couche
 * {@code infrastructure/adapter/out/persistence/}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "allergens")
public class AllergenJpaEntity {

  /** Identifiant technique. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Nom de l'allergène (ex. {@code Gluten}, {@code Lait}). */
  @Column(name = "name", nullable = false, unique = true, length = 100)
  private String name;

  /** Indique si l'allergène est actif et proposé dans les formulaires. */
  @Column(name = "enabled", nullable = false)
  private boolean enabled;
}