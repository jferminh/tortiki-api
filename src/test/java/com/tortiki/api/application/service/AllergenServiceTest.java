package com.tortiki.api.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.tortiki.api.application.port.out.AllergenRepository;
import com.tortiki.api.domain.exception.AllergenNotFoundException;
import com.tortiki.api.domain.model.Allergen;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
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
 * Tests unitaires de {@link AllergenService}.
 *
 * <p>Vérifie la logique métier de consultation des allergènes.
 * Aucune dépendance à la base de données — Mockito pur.</p>
 */
@Epic("Référentiel")
@Feature("Gestion des allergènes")
@Owner("Tortiki")
@ExtendWith(MockitoExtension.class)
@DisplayName("AllergenService — Tests unitaires")
class AllergenServiceTest {

  private static final Long ALLERGEN_ID = 1L;
  private static final String ALLERGEN_NAME = "Gluten";
  private static final Long UNKNOWN_ID = 99L;

  @Mock
  private AllergenRepository allergenRepository;

  @InjectMocks
  private AllergenService allergenService;

  private Allergen allergen;

  @BeforeEach
  void setUp() {
    allergen = new Allergen();
    allergen.setId(ALLERGEN_ID);
    allergen.setName(ALLERGEN_NAME);
  }

  @Test
  @Story("Consultation du référentiel")
  @Severity(SeverityLevel.NORMAL)
  @Description("Liste non vide retourne tous les allergènes disponibles.")
  @DisplayName("findAll retourne la liste complète des allergènes")
  void findAll_shouldReturnAllAllergens() {
    when(allergenRepository.findAll()).thenReturn(List.of(allergen));

    List<Allergen> result = allergenService.findAll();

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getName()).isEqualTo(ALLERGEN_NAME);
  }

  @Test
  @Story("Consultation du référentiel")
  @Severity(SeverityLevel.MINOR)
  @Description("Référentiel vide retourne une liste vide, jamais null.")
  @DisplayName("findAll retourne une liste vide si le référentiel est vide")
  void findAll_shouldReturnEmptyList_whenNoAllergens() {
    when(allergenRepository.findAll()).thenReturn(List.of());

    List<Allergen> result = allergenService.findAll();

    assertThat(result).isEmpty();
  }

  @Test
  @Story("Détail d'un allergène")
  @Severity(SeverityLevel.NORMAL)
  @Description("Allergène trouvé par son identifiant.")
  @DisplayName("findById retourne l'allergène correspondant")
  void findById_shouldReturnAllergen_whenFound() {
    when(allergenRepository.findById(ALLERGEN_ID)).thenReturn(Optional.of(allergen));

    Allergen result = allergenService.findById(ALLERGEN_ID);

    assertThat(result.getId()).isEqualTo(ALLERGEN_ID);
    assertThat(result.getName()).isEqualTo(ALLERGEN_NAME);
  }

  @Test
  @Story("Détail d'un allergène")
  @Severity(SeverityLevel.CRITICAL)
  @Description("Allergène introuvable lève AllergenNotFoundException avec l'id dans le message.")
  @DisplayName("findById lève AllergenNotFoundException si l'id est inconnu")
  void findById_shouldThrowException_whenNotFound() {
    when(allergenRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> allergenService.findById(UNKNOWN_ID))
        .isInstanceOf(AllergenNotFoundException.class)
        .hasMessageContaining("99");
  }
}