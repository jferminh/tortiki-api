-- ============================================================
-- Migration V1 — Schéma initial de Tortiki
-- Auteur  : CDA Tortiki
-- Date    : 2026-05-26
-- Entités : User · Role · Listing · CuisineType
--           Allergen · ContactRequest · Review
-- ============================================================
-- RÈGLE FLYWAY : ce script ne sera JAMAIS modifié après
-- application en base. Toute évolution → V2__xxx.sql
-- ============================================================

-- ===== TYPES ÉNUMÉRÉS =====

-- Statut d'une annonce publiée par un vendeur
CREATE TYPE listing_status AS ENUM (
  'ACTIVE',
  'INACTIVE',
  'DELETED'
);

-- Statut d'une demande d'intérêt d'un client
CREATE TYPE contact_request_status AS ENUM (
  'PENDING',
  'CONFIRMED',
  'REFUSED'
);

-- ===== TABLE : roles =====
-- Référentiel des rôles disponibles (géré en base, pas en ENUM natif)
-- Permet d'ajouter un rôle sans migration de type PostgreSQL
CREATE TABLE roles
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
    -- Valeurs attendues : ROLE_ADMIN · ROLE_SELLER · ROLE_BUYER
);

-- ===== TABLE : users =====
-- Compte utilisateur — identifiant de connexion = email
CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL, -- BCrypt force 12, jamais en clair
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    phone         VARCHAR(20),           -- Partagé client après confirmation seulement
    avatar_url    VARCHAR(500),          -- URL MinIO
    city          VARCHAR(100),
    latitude      NUMERIC(10, 7),        -- Nominatim · précision ~1 cm
    longitude     NUMERIC(10, 7),
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ===== TABLE : user_roles =====
-- Association ManyToMany User ↔ Role (cumul possible : SELLER + BUYER)
CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ===== TABLE : cuisine_types =====
-- Référentiel des origines culinaires — administré par ROLE_ADMIN
CREATE TABLE cuisine_types
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ===== TABLE : allergens =====
-- 14 allergènes réglementaires EU (Règlement (UE) n°1169/2011)
CREATE TABLE allergens
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- ===== TABLE : listings =====
-- Annonce de plat publiée par un vendeur
CREATE TABLE listings
(
    id              BIGSERIAL PRIMARY KEY,
    seller_id       BIGINT         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    cuisine_type_id BIGINT         NOT NULL REFERENCES cuisine_types (id),
    title           VARCHAR(255)   NOT NULL,
    description     TEXT,
    price           NUMERIC(10, 2) NOT NULL CHECK (price > 0),
    portions        INTEGER        NOT NULL CHECK (portions > 0),
    photo_url       VARCHAR(500),            -- URL objet MinIO
    pickup_address  VARCHAR(255)   NOT NULL, -- Adresse saisie par le vendeur
    pickup_lat      NUMERIC(10, 7),          -- Géocodé via Nominatim
    pickup_lng      NUMERIC(10, 7),
    pickup_datetime TIMESTAMP      NOT NULL, -- Créneau unique en v1
    status          listing_status NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- ===== TABLE : listing_allergens =====
-- Association ManyToMany Listing ↔ Allergen
CREATE TABLE listing_allergens
(
    listing_id  BIGINT NOT NULL REFERENCES listings (id) ON DELETE CASCADE,
    allergen_id BIGINT NOT NULL REFERENCES allergens (id) ON DELETE CASCADE,
    PRIMARY KEY (listing_id, allergen_id)
);

-- ===== TABLE : contact_requests =====
-- Demande d'intérêt d'un client pour une annonce
-- RGPD : les coordonnées vendeur ne sont partagées qu'après CONFIRMED
CREATE TABLE contact_requests
(
    id         BIGSERIAL PRIMARY KEY,
    listing_id BIGINT                 NOT NULL REFERENCES listings (id) ON DELETE CASCADE,
    buyer_id   BIGINT                 NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    portions   INTEGER                NOT NULL DEFAULT 1 CHECK (portions > 0),
    status     contact_request_status NOT NULL DEFAULT 'PENDING',
    message    TEXT,
    created_at TIMESTAMP              NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP              NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_contact_request_listing_buyer UNIQUE (listing_id, buyer_id)
    -- Un client = une seule demande par annonce
);

-- ===== TABLE : reviews =====
-- Notation du vendeur par le client après retrait confirmé
-- La contrainte UNIQUE sur contact_request_id garantit 1 avis par transaction
CREATE TABLE reviews
(
    id                 BIGSERIAL PRIMARY KEY,
    contact_request_id BIGINT    NOT NULL UNIQUE REFERENCES contact_requests (id) ON DELETE CASCADE,
    reviewer_id        BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    seller_id          BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    listing_id         BIGINT    NOT NULL REFERENCES listings (id) ON DELETE CASCADE,
    rating             SMALLINT  NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment            TEXT,
    created_at         TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ===== INDEX =====
-- Optimisation des requêtes les plus fréquentes

-- Mes annonces (tableau de bord vendeur)
CREATE INDEX idx_listings_seller_id
    ON listings (seller_id);

-- Catalogue public filtré par origine + statut actif
CREATE INDEX idx_listings_cuisine_status
    ON listings (cuisine_type_id, status);

-- Recherche géographique approximative (bounding box)
CREATE INDEX idx_listings_location
    ON listings (pickup_lat, pickup_lng);

-- Demandes reçues par le vendeur (via ses annonces)
CREATE INDEX idx_contact_requests_listing_id
    ON contact_requests (listing_id);

-- Historique des demandes envoyées par le client
CREATE INDEX idx_contact_requests_buyer_id
    ON contact_requests (buyer_id);

-- Avis reçus par un vendeur (calcul note moyenne)
CREATE INDEX idx_reviews_seller_id
    ON reviews (seller_id);

-- ===== DONNÉES INITIALES =====

-- Rôles de base (obligatoires au démarrage de l'application)
INSERT INTO roles (name)
VALUES ('ADMIN'),
       ('SELLER'),
       ('BUYER');

-- 14 allergènes réglementaires EU (Règlement 1169/2011)
INSERT INTO allergens (name)
VALUES ('Gluten'),
       ('Crustacés'),
       ('Œufs'),
       ('Poisson'),
       ('Arachides'),
       ('Soja'),
       ('Lait'),
       ('Fruits à coque'),
       ('Céleri'),
       ('Moutarde'),
       ('Graines de sésame'),
       ('Anhydride sulfureux et sulfites'),
       ('Lupin'),
       ('Mollusques');

-- Origines culinaires initiales (administrables via ROLE_ADMIN)
INSERT INTO cuisine_types (name, description)
VALUES ('Ukrainienne', 'Cuisine traditionnelle d''Ukraine'),
       ('Française', 'Cuisine traditionnelle de France'),
       ('Italienne', 'Cuisine traditionnelle d''Italie'),
       ('Marocaine', 'Cuisine traditionnelle du Maroc'),
       ('Japonaise', 'Cuisine traditionnelle du Japon'),
       ('Mexicaine', 'Cuisine traditionnelle du Mexique'),
       ('Indienne', 'Cuisine traditionnelle d''Inde'),
       ('Libanaise', 'Cuisine traditionnelle du Liban');
