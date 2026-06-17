-- Migration V3 : alignement des types de colonnes géolocalisation
-- Auteur : CDA Tortiki
-- Cause  : ListingJpaEntity mappe Double → float(53), Flyway avait NUMERIC(10,7)
-- Règle Flyway : V1 et V2 ne sont jamais modifiés

ALTER TABLE listings
ALTER COLUMN pickup_lat TYPE DOUBLE PRECISION,
  ALTER COLUMN pickup_lng TYPE DOUBLE PRECISION;