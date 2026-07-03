package com.tortiki.api.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de requête pour la création d'un allergène.
 *
 * <p>Record immuable Java 21 — la validation Bean Validation est
 * déclenchée par {@code @Valid} dans
 * {@link com.tortiki.api.infrastructure.adapter.in.web.AllergenController}.</p>
 *
 * <p>Réservé à {@code ROLE_ADMIN}. Le champ {@code enabled} n'est
 * volontairement pas exposé ici — un allergène est toujours créé
 * activé par défaut, cette décision appartient au service applicatif,
 * jamais à l'appelant HTTP.</p>
 *
 * @param name nom officiel de l'allergène à créer
 */
@Schema(description = "Requête de création d'un allergène")
public record CreateAllergenRequest(

    @Schema(description = "Nom de l'allergène", example = "Sésame")
    @NotBlank(message = "Le nom de l'allergène est obligatoire")
    @Size(max = 100, message = "Le nom de l'allergène ne doit pas dépasser 100 caractères")
    String name

) {}