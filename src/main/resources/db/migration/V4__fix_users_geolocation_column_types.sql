-- ============================================================
-- Migration V4 — Correction types géolocalisation table users
-- Auteur  : CDA Tortiki
-- Date    : 2026-06-16
-- Cause   : UserJpaEntity mappe Double → float(53) (DOUBLE PRECISION)
--           La V1 avait déclaré NUMERIC(10,7) sur users.latitude/longitude
--           La V3 n'avait corrigé que listings.pickup_lat/pickup_lng
-- Règle Flyway : V1, V2 et V3 ne sont jamais modifiés
-- ============================================================

ALTER TABLE users
ALTER COLUMN latitude  TYPE DOUBLE PRECISION USING latitude::double precision,
ALTER COLUMN longitude TYPE DOUBLE PRECISION USING longitude::double precision;