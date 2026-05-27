package com.tortiki.api.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité JPA représentant un utilisateur de la plateforme Tortiki.
 *
 * <p>Correspond à la table {@code users} en base de données.
 * Le mot de passe est stocké sous forme de hash BCrypt — jamais en clair.</p>
 *
 * <p>Cette entité appartient à la couche {@code infrastructure/persistence}
 * et ne doit pas être exposée au-delà de cette couche.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity {

    /** Identifiant technique auto-incrémenté. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Adresse email — identifiant unique de connexion. */
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /** Hash BCrypt du mot de passe — jamais le mot de passe en clair. */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** Prénom de l'utilisateur. */
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    /** Nom de famille de l'utilisateur. */
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /** Indique si le compte est actif. */
    @Column(nullable = false)
    private boolean enabled = true;

    /** Date et heure de création du compte. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Date et heure de dernière modification. */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Rôles associés à cet utilisateur (ROLE_ADMIN, ROLE_SELLER, ROLE_BUYER). */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    /** Initialise les dates avant la première persistance. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /** Met à jour la date de modification à chaque sauvegarde. */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}