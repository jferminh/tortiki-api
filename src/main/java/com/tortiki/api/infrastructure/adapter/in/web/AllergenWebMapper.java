package com.tortiki.api.infrastructure.adapter.in.web;

import com.tortiki.api.domain.model.Allergen;
import com.tortiki.api.infrastructure.adapter.in.web.dto.AllergenResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper entre le POJO domaine {@link Allergen} et le DTO de sortie
 * {@link AllergenResponse}.
 *
 * <p>Appartient exclusivement à la couche
 * {@code infrastructure/adapter/in/web}. Le domaine ne connaît jamais
 * ce mapper — seul {@code AllergenController} le consomme.</p>
 */
@Component
public class AllergenWebMapper {

  /**
   * Convertit un POJO domaine {@link Allergen} en DTO {@link AllergenResponse}.
   *
   * @param allergen le POJO domaine à convertir
   * @return le DTO de sortie correspondant
   */
  public AllergenResponse toResponse(Allergen allergen) {
    return new AllergenResponse(
        allergen.getId(),
        allergen.getName()
    );
  }
}