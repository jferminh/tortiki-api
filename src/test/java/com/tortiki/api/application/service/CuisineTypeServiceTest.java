package com.tortiki.api.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tortiki.api.application.port.out.CuisineTypeRepository;
import com.tortiki.api.domain.exception.CuisineTypeNotFoundException;
import com.tortiki.api.domain.model.CuisineType;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests unitaires de {@link CuisineTypeService}.
 *
 * <p>Vérifie la logique métier de gestion des origines culinaires
 * sans dépendance à la base de données (Mockito pur).</p>
 */
@Epic("Référentiel")
@Feature("Origines culinaires")
@ExtendWith(MockitoExtension.class)
@DisplayName("CuisineTypeService — Tests unitaires")
class CuisineTypeServiceTest {

  @Mock
  private CuisineTypeRepository cuisineTypeRepository;

  @InjectMocks
  private CuisineTypeService cuisineTypeService;

  private CuisineType italiana;
  private CuisineType ukrainienne;

  /**
   * Initialiser les fixtures partagées entre les tests.
   */
  @BeforeEach
  void setUp() {
    italiana = new CuisineType();
    italiana.setId(1L);
    italiana.setName("Italiana");
    italiana.setDescription("Cuisine italienne traditionnelle");

    ukrainienne = new CuisineType();
    ukrainienne.setId(2L);
    ukrainienne.setName("Ukrainienne");
    ukrainienne.setDescription("Cuisine ukrainienne traditionnelle");
  }

  // ── CREATE ────────────────────────────────────────────────────────────────

  @Test
  @Story("Création d'une origine culinaire")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Un admin crée une nouvelle origine culinaire — elle est persistée et retournée.")
  @DisplayName("create — persiste et retourne la nouvelle origine culinaire")
  void create_shouldPersistAndReturnCuisineType() {
    when(cuisineTypeRepository.save(any(CuisineType.class))).thenReturn(italiana);

    CuisineType result = cuisineTypeService.create("Italiana", "Cuisine italienne traditionnelle");

    assertThat(result.getName()).isEqualTo("Italiana");
    assertThat(result.getDescription()).isEqualTo("Cuisine italienne traditionnelle");
    verify(cuisineTypeRepository).save(any(CuisineType.class));
  }

  // ── UPDATE ────────────────────────────────────────────────────────────────

  @Test
  @Story("Modification d'une origine culinaire")
  @Severity(SeverityLevel.NORMAL)
  @Description("Mise à jour d'une origine existante — les nouvelles valeurs sont persistées.")
  @DisplayName("update — met à jour le nom et la description d'une origine existante")
  void update_shouldUpdateExistingCuisineType() {
    when(cuisineTypeRepository.findById(1L)).thenReturn(Optional.of(italiana));
    when(cuisineTypeRepository.save(any(CuisineType.class))).thenReturn(italiana);

    CuisineType result = cuisineTypeService.update(1L, "Italiana v2", "Mise à jour");

    assertThat(result).isNotNull();
    verify(cuisineTypeRepository).findById(1L);
    verify(cuisineTypeRepository).save(any(CuisineType.class));
  }

  @Test
  @Story("Modification d'une origine culinaire")
  @Severity(SeverityLevel.NORMAL)
  @Description("Tentative de mise à jour d'une origine introuvable — exception métier levée.")
  @DisplayName("update — lève CuisineTypeNotFoundException si l'id est inconnu")
  void update_shouldThrowException_whenCuisineTypeNotFound() {
    when(cuisineTypeRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cuisineTypeService.update(99L, "X", "Y"))
        .isInstanceOf(CuisineTypeNotFoundException.class)
        .hasMessageContaining("99");

    verify(cuisineTypeRepository, never()).save(any());
  }

  // ── DELETE ────────────────────────────────────────────────────────────────

  @Test
  @Story("Suppression d'une origine culinaire")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Suppression d'une origine non référencée par des annonces actives — OK.")
  @DisplayName("delete — supprime une origine non utilisée par des annonces actives")
  void delete_shouldDeleteCuisineType_whenNotUsedByActiveListing() {
    when(cuisineTypeRepository.findById(1L)).thenReturn(Optional.of(italiana));
    when(cuisineTypeRepository.isUsedByActiveListing(1L)).thenReturn(false);

    cuisineTypeService.delete(1L);

    verify(cuisineTypeRepository).deleteById(1L);
  }

  @Test
  @Story("Suppression d'une origine culinaire")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Suppression bloquée si des annonces actives référencent l'origine — règle métier.")
  @DisplayName("delete — lève IllegalStateException si des annonces actives la référencent")
  void delete_shouldThrowException_whenUsedByActiveListing() {
    when(cuisineTypeRepository.findById(1L)).thenReturn(Optional.of(italiana));
    when(cuisineTypeRepository.isUsedByActiveListing(1L)).thenReturn(true);

    assertThatThrownBy(() -> cuisineTypeService.delete(1L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("annonces actives");

    verify(cuisineTypeRepository, never()).deleteById(any());
  }

  @Test
  @Story("Suppression d'une origine culinaire")
  @Severity(SeverityLevel.NORMAL)
  @Description("Suppression d'une origine introuvable — exception métier levée.")
  @DisplayName("delete — lève CuisineTypeNotFoundException si l'id est inconnu")
  void delete_shouldThrowException_whenCuisineTypeNotFound() {
    when(cuisineTypeRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cuisineTypeService.delete(99L))
        .isInstanceOf(CuisineTypeNotFoundException.class)
        .hasMessageContaining("99");

    verify(cuisineTypeRepository, never()).deleteById(any());
  }

  // ── FIND ALL ──────────────────────────────────────────────────────────────

  @Test
  @Story("Consultation des origines culinaires")
  @Severity(SeverityLevel.NORMAL)
  @Description("Récupération de la liste complète des origines culinaires.")
  @DisplayName("findAll — retourne toutes les origines culinaires")
  void findAll_shouldReturnAllCuisineTypes() {
    when(cuisineTypeRepository.findAll()).thenReturn(List.of(italiana, ukrainienne));

    List<CuisineType> result = cuisineTypeService.findAll();

    assertThat(result).hasSize(2);
    assertThat(result).extracting(CuisineType::getName)
        .containsExactlyInAnyOrder("Italiana", "Ukrainienne");
  }

  @Test
  @Story("Consultation des origines culinaires")
  @Severity(SeverityLevel.MINOR)
  @Description("Retourne une liste vide si aucune origine n'est enregistrée.")
  @DisplayName("findAll — retourne une liste vide si aucune origine enregistrée")
  void findAll_shouldReturnEmptyList_whenNoCuisineTypes() {
    when(cuisineTypeRepository.findAll()).thenReturn(List.of());

    List<CuisineType> result = cuisineTypeService.findAll();

    assertThat(result).isEmpty();
  }

  // ── FIND BY ID ────────────────────────────────────────────────────────────

  @Test
  @Story("Consultation des origines culinaires")
  @Severity(SeverityLevel.NORMAL)
  @Description("Récupération d'une origine par son identifiant — retourne l'objet.")
  @DisplayName("findById — retourne l'origine culinaire correspondant à l'id")
  void findById_shouldReturnCuisineType_whenFound() {
    when(cuisineTypeRepository.findById(1L)).thenReturn(Optional.of(italiana));

    CuisineType result = cuisineTypeService.findById(1L);

    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("Italiana");
  }

  @Test
  @Story("Consultation des origines culinaires")
  @Severity(SeverityLevel.NORMAL)
  @Description("Identifiant inconnu — exception métier levée avec l'id dans le message.")
  @DisplayName("findById — lève CuisineTypeNotFoundException si l'id est inconnu")
  void findById_shouldThrowException_whenNotFound() {
    when(cuisineTypeRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> cuisineTypeService.findById(99L))
        .isInstanceOf(CuisineTypeNotFoundException.class)
        .hasMessageContaining("99");
  }
}