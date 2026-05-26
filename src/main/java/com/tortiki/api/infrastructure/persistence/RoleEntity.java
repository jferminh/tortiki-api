package com.tortiki.api.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité JPA représentant un rôle de la plateforme.
 *
 * <p>Correspond à la table {@code roles} en base de données.
 * Les valeurs possibles sont : ROLE_ADMIN, ROLE_SELLER, ROLE_BUYER.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class RoleEntity {

    /** Identifiant technique auto-incrémenté. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom du rôle Spring Security (ex : ROLE_ADMIN). */
    @Column(nullable = false, unique = true, length = 50)
    private String name;
}