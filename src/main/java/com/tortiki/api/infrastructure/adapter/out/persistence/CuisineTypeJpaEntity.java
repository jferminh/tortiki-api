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
 * Entité JPA représentant une origine culinaire.
 *
 * <p>Référentiel administrable par {@code ROLE_ADMIN}.
 * Appartient exclusivement à la couche
 * {@code infrastructure/adapter/out/persistence/}.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cuisine_types")
public class CuisineTypeJpaEntity {

  /** Identifiant technique. */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Nom de l'origine culinaire (ex. {@code Ukrainienne}). */
  @Column(name = "name", nullable = false, unique = true, length = 100)
  private String name;

  /** Description de l'origine culinaire. */
  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  /** Indique si l'origine est active et visible dans l'interface. */
  @Column(name = "enabled", nullable = false)
  private boolean enabled = true;
}