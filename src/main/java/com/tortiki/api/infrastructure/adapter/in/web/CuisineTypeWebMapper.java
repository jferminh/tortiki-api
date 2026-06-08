package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.domain.model.CuisineType;
import com.tortiki.api.infrastructure.adapter.in.web.dto.CuisineTypeResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le modèle domaine {@link CuisineType} et le DTO
 * {@link CuisineTypeResponse} de la couche REST.
 *
 * <p>Garantit que les DTOs HTTP ne remontent jamais dans les couches
 * {@code application} ou {@code domain}.</p>
 */
@Component
public class CuisineTypeWebMapper {

  /**
   * Convertit un {@link CuisineType} domaine en {@link CuisineTypeResponse} HTTP.
   *
   * @param cuisineType l'origine culinaire domaine à convertir
   * @return le DTO de réponse HTTP
   */
  public CuisineTypeResponse toResponse(CuisineType cuisineType) {
    return new CuisineTypeResponse(
        cuisineType.getId(),
        cuisineType.getName(),
        cuisineType.getDescription()
    );
  }
}