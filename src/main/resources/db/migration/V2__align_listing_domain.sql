-- V2__align_listing_domain.sql
-- ============================================================
-- Migration V2 — Alignement domaine Listing et rôles Spring Security
-- Auteur  : CDA Tortiki
-- Date    : 2026-06-12
-- Règle   : V1 jamais modifié — correctifs via V2
-- ============================================================

-- Ajout du statut MODERATED manquant dans listing_status
-- (utilisé par la modération ROLE_ADMIN)
ALTER TYPE listing_status ADD VALUE IF NOT EXISTS 'MODERATED';