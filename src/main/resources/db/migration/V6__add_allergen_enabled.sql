-- ============================================================
-- Migration V6 — Ajout du champ enabled sur allergens
-- Auteur  : CDA Tortiki
-- Date    : 2026-07-03
-- Cause   : Allergen (domaine) et AllergenJpaEntity nécessitent
--           un champ enabled pour la désactivation ROLE_ADMIN
--           (Issue #53) — la V1 n'avait défini que id et name.
-- Règle Flyway : V1 à V5 ne sont jamais modifiés
-- ============================================================

ALTER TABLE allergens
    ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;