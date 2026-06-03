package com.tortiki.api.infrastructure.adapter.in.web.dto;

import com.tortiki.api.domain.model.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO de requête pour l'inscription d'un nouvel utilisateur.
 *
 * <p>Record immuable : les données reçues par le contrôleur ne peuvent
 * pas être modifiées après désérialisation. La validation Bean Validation
 * est déclenchée par {@code @Valid} dans le contrôleur.</p>
 */
public record RegisterRequest(

    @NotBlank(message = "L'adresse email est obligatoire")
    @Email(message = "L'adresse email n'est pas valide")
    String email,

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    String password,

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
    String firstName,

    @NotBlank(message = "Le nom de famille est obligatoire")
    @Size(max = 100, message = "Le nom de famille ne peut pas dépasser 100 caractères")
    String lastName,

    @NotNull(message = "Le rôle est obligatoire")
    RoleName role
) {}