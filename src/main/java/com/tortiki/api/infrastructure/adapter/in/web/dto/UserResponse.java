package com.tortiki.api.infrastructure.adapter.in.web.dto;

import com.tortiki.api.domain.model.RoleName;
import java.util.Set;

/**
 * DTO de réponse représentant un utilisateur authentifié.
 *
 * <p>N'expose jamais {@code passwordHash} — principe du moindre privilège
 * (OWASP) : on ne retourne que les données strictement nécessaires au client.</p>
 */
public record UserResponse(
    Long id,
    String email,
    String firstName,
    String lastName,
    Set<RoleName> roles
) {}