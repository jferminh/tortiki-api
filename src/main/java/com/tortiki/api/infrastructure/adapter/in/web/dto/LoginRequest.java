package com.tortiki.api.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO de requête pour l'authentification d'un utilisateur.
 *
 * <p>Contient uniquement les credentials nécessaires à Spring Security.
 * Le mot de passe en clair n'est jamais journalisé ni retourné.</p>
 */
public record LoginRequest(

    @NotBlank(message = "L'adresse email est obligatoire")
    @Email(message = "L'adresse email n'est pas valide")
    String email,

    @NotBlank(message = "Le mot de passe est obligatoire")
    String password
) {}