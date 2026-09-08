package com.tortiki.api.infrastructure.adapter.out.persistence;

import com.tortiki.api.domain.model.Allergen;
import com.tortiki.api.domain.model.Listing;
import com.tortiki.api.domain.model.ListingStatus;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de non-régression par réflexion pour {@link ListingPersistenceMapper}.
 *
 * <p>Objectif : détecter automatiquement tout champ scalaire présent à la
 * fois dans {@link Listing} et {@link ListingJpaEntity} mais oublié par
 * {@code toEntity()} ou {@code toDomain()} — exactement la classe de bug
 * qui a provoqué la violation {@code NOT NULL} sur {@code listings.city}
 * en production : le champ existait des deux côtés, mais n'était copié
 * par aucune des deux méthodes du mapper.</p>
 *
 * <p>La liste des champs comparés est calculée dynamiquement par
 * intersection des champs déclarés des deux classes — aucune liste
 * manuelle à maintenir. Si un futur champ est ajouté aux deux classes
 * mais oublié dans le mapper, ce test échoue automatiquement, sans
 * modification requise ici.</p>
 *
 * <p>Complète {@code ListingServiceTest} (comportement métier) sans le
 * remplacer : ce test ne vérifie aucune règle métier, uniquement la
 * complétude structurelle du mapping champ par champ.</p>
 */
@Epic("Annonces")
@Feature("Mapping persistance listings")
@DisplayName("ListingPersistenceMapper — Tests de non-régression par réflexion")
class ListingPersistenceMapperTest {

    /**
     * Champs d'association, exclus de la comparaison scalaire, car mappés
     * vers des types différents entre {@link Listing} et
     * {@link ListingJpaEntity} — vérifiés séparément par des tests dédiés.
     */
    private static final Set<String> ASSOCIATION_FIELDS = Set.of("seller", "cuisineType", "allergens");

    /** Champs scalaires communs, calculés dynamiquement, jamais codés en dur. */
    private static final Set<String> SCALAR_FIELDS = computeCommonScalarFieldNames();

    private final ListingPersistenceMapper mapper = new ListingPersistenceMapper(
            new CuisineTypePersistenceMapper(), new AllergenPersistenceMapper());

    @Test
    @Story("Non-régression mapping")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Chaque champ scalaire présent dans Listing et ListingJpaEntity doit être "
            + "recopié par toEntity() — régression exacte du bug NOT NULL sur listings.city.")
    @DisplayName("toEntity — recopie tous les champs scalaires communs")
    void toEntityShouldCopyAllSharedScalarFields() {
        Listing listing = new Listing();
        populateScalarFields(listing, Listing.class);

        ListingJpaEntity entity = mapper.toEntity(listing);

        for (String fieldName : SCALAR_FIELDS) {
            Object expected = readField(listing, Listing.class, fieldName);
            Object actual = readField(entity, ListingJpaEntity.class, fieldName);
            assertThat(actual)
                    .as("Le champ '%s' doit être recopié par toEntity()", fieldName)
                    .isEqualTo(expected);
        }
    }

    @Test
    @Story("Non-régression mapping")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Chaque champ scalaire présent dans ListingJpaEntity et Listing doit être "
            + "recopié par toDomain() — sens inverse du même risque de régression.")
    @DisplayName("toDomain — recopie tous les champs scalaires communs")
    void toDomainShouldCopyAllSharedScalarFields() {
        ListingJpaEntity entity = new ListingJpaEntity();
        populateScalarFields(entity, ListingJpaEntity.class);

        Listing domain = mapper.toDomain(entity);

        for (String fieldName : SCALAR_FIELDS) {
            Object expected = readField(entity, ListingJpaEntity.class, fieldName);
            Object actual = readField(domain, Listing.class, fieldName);
            assertThat(actual)
                    .as("Le champ '%s' doit être recopié par toDomain()", fieldName)
                    .isEqualTo(expected);
        }
    }

    @Test
    @Story("Non-régression mapping")
    @Severity(SeverityLevel.CRITICAL)
    @Description("toDomain() doit déléguer à CuisineTypePersistenceMapper et récupérer "
            + "description et enabled, oubliés avant la factorisation du mapper.")
    @DisplayName("toDomain — recopie description et enabled de l'origine culinaire")
    void toDomainShouldCopyFullCuisineTypeViaDelegation() {
        ListingJpaEntity entity = new ListingJpaEntity();
        populateScalarFields(entity, ListingJpaEntity.class);

        CuisineTypeJpaEntity cuisineTypeEntity = new CuisineTypeJpaEntity();
        cuisineTypeEntity.setId(10L);
        cuisineTypeEntity.setName("Ukrainienne");
        cuisineTypeEntity.setDescription("Cuisine d'Europe de l'Est");
        cuisineTypeEntity.setEnabled(true);
        entity.setCuisineType(cuisineTypeEntity);

        Listing domain = mapper.toDomain(entity);

        assertThat(domain.getCuisineType().getDescription()).isEqualTo("Cuisine d'Europe de l'Est");
        assertThat(domain.getCuisineType().isEnabled()).isTrue();
    }

    @Test
    @Story("Non-régression mapping")
    @Severity(SeverityLevel.CRITICAL)
    @Description("toDomain() doit déléguer à AllergenPersistenceMapper et récupérer "
            + "enabled, oublié avant la factorisation du mapper.")
    @DisplayName("toDomain — recopie enabled sur chaque allergène")
    void toDomainShouldCopyFullAllergensViaDelegation() {
        ListingJpaEntity entity = new ListingJpaEntity();
        populateScalarFields(entity, ListingJpaEntity.class);

        AllergenJpaEntity glutenEntity = new AllergenJpaEntity();
        glutenEntity.setId(3L);
        glutenEntity.setName("Gluten");
        glutenEntity.setEnabled(true);
        entity.setAllergens(List.of(glutenEntity));

        Listing domain = mapper.toDomain(entity);

        assertThat(domain.getAllergens())
                .extracting(Allergen::isEnabled)
                .containsExactly(true);
    }

    // ── Utilitaires de réflexion ──────────────────────────────────────────────

    /**
     * Calcule l'intersection des noms de champs déclarés par {@link Listing}
     * et {@link ListingJpaEntity}, hors champs d'association.
     *
     * @return l'ensemble des noms de champs scalaires communs aux deux classes
     */
    private static Set<String> computeCommonScalarFieldNames() {
        Set<String> listingFields = fieldNames(Listing.class);
        Set<String> entityFields = fieldNames(ListingJpaEntity.class);
        Set<String> common = new HashSet<>(listingFields);
        common.retainAll(entityFields);
        common.removeAll(ASSOCIATION_FIELDS);
        return common;
    }

    private static Set<String> fieldNames(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Renseigne par réflexion tous les champs scalaires communs d'un objet,
     * avec une valeur déterministe et distincte par nom de champ.
     *
     * @param target objet à peupler, doit exposer un constructeur sans argument
     * @param type   classe déclarant les champs à peupler
     */
    private void populateScalarFields(Object target, Class<?> type) {
        for (String fieldName : SCALAR_FIELDS) {
            writeField(target, type, fieldName, generateValue(fieldName, type));
        }
    }

    /**
     * Génère une valeur déterministe pour un champ, à partir de son nom
     * et de son type déclaré.
     *
     * @param fieldName nom du champ, utilisé comme graine
     * @param ownerType classe déclarant le champ, pour résoudre son type
     * @return une valeur non nulle compatible avec le type du champ
     */
    private Object generateValue(String fieldName, Class<?> ownerType) {
        Class<?> fieldType = resolveFieldType(ownerType, fieldName);
        int seed = Math.abs(fieldName.hashCode());

        if (fieldType.equals(Long.class)) {
            return (long) (seed % 1000) + 1;
        }
        if (fieldType.equals(String.class)) {
            return "value-" + fieldName;
        }
        if (fieldType.equals(BigDecimal.class)) {
            return BigDecimal.valueOf(seed % 100).add(BigDecimal.valueOf(0.5));
        }
        if (fieldType.equals(Integer.class)) {
            return seed % 100 + 1;
        }
        if (fieldType.equals(Double.class)) {
            return (double) (seed % 100) + 0.5;
        }
        if (fieldType.equals(LocalDateTime.class)) {
            return LocalDateTime.of(2026, Month.JANUARY, 1, 0, 0).plusDays(seed % 300);
        }
        if (fieldType.equals(ListingStatus.class)) {
            return ListingStatus.ACTIVE;
        }
        throw new IllegalStateException(
                "Type de champ non pris en charge par ce test de réflexion : " + fieldType);
    }

    private Class<?> resolveFieldType(Class<?> type, String fieldName) {
        try {
            return type.getDeclaredField(fieldName).getType();
        } catch (NoSuchFieldException ex) {
            throw new IllegalStateException(
                    "Champ '" + fieldName + "' introuvable sur " + type.getSimpleName(), ex);
        }
    }

    private void writeField(Object target, Class<?> type, String fieldName, Object value) {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(
                    "Impossible d'écrire le champ '" + fieldName + "' sur " + type.getSimpleName(), ex);
        }
    }

    private Object readField(Object source, Class<?> type, String fieldName) {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(source);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(
                    "Impossible de lire le champ '" + fieldName + "' sur " + type.getSimpleName(), ex);
        }
    }
}