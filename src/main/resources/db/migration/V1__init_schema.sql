-- ============================================================
-- Migration V1 — Schéma initial de Tortiki
-- Auteur : CDA Tortiki
-- Date   : 2026-05-26
-- Entités : User, Role, Listing, CuisineType,
--           Allergen, ContactRequest, Review
-- ============================================================

-- ===== TYPES ÉNUMÉRÉS =====

-- Statut d'une demande de contact
CREATE TYPE contact_request_status AS ENUM (
    'PENDING',
    'CONFIRMED',
    'REFUSED'
);

-- Statut d'une annonce
CREATE TYPE listing_status AS ENUM (
    'ACTIVE',
    'INACTIVE',
    'DELETED'
);

-- ===== TABLE : roles =====
-- Référentiel des rôles disponibles sur la plateforme
CREATE TABLE roles
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE -- ex: ROLE_ADMIN, ROLE_SELLER, ROLE_BUYER
);

-- ===== TABLE : users =====
-- Compte utilisateur de la plateforme
CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL, -- BCrypt
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    phone         VARCHAR(20),
    avatar_url    VARCHAR(500),
    city          VARCHAR(100),
    latitude      NUMERIC(10, 7),
    longitude     NUMERIC(10, 7),
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ===== TABLE : user_roles =====
-- Association ManyToMany entre users et roles
CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- ===== TABLE : cuisine_types =====
-- Référentiel des origines culinaires (géré par l'admin)
CREATE TABLE cuisine_types
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE, -- ex: Ukrainienne, Italienne
    description TEXT,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ===== TABLE : allergens =====
-- Référentiel des allergènes (14 allergènes réglementaires EU)
CREATE TABLE allergens
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE -- ex: Gluten, Lactose, Arachides
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
    photo_url       VARCHAR(500),            -- URL MinIO
    pickup_address  VARCHAR(255)   NOT NULL, -- Adresse de retrait
    pickup_lat      NUMERIC(10, 7),          -- Latitude (Nominatim)
    pickup_lng      NUMERIC(10, 7),          -- Longitude (Nominatim)
    pickup_datetime TIMESTAMP      NOT NULL, -- Créneau unique v1
    status          listing_status NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- ===== TABLE : listing_allergens =====
-- Association ManyToMany entre listings et allergens
CREATE TABLE listing_allergens
(
    listing_id  BIGINT NOT NULL REFERENCES listings (id) ON DELETE CASCADE,
    allergen_id BIGINT NOT NULL REFERENCES allergens (id) ON DELETE CASCADE,
    PRIMARY KEY (listing_id, allergen_id)
);

-- ===== TABLE : contact_requests =====
-- Demande d'intérêt d'un client pour une annonce
CREATE TABLE contact_requests
(
    id         BIGSERIAL PRIMARY KEY,
    listing_id BIGINT                 NOT NULL REFERENCES listings (id) ON DELETE CASCADE,
    buyer_id   BIGINT                 NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    portions   INTEGER                NOT NULL DEFAULT 1 CHECK (portions > 0),
    status     contact_request_status NOT NULL DEFAULT 'PENDING',
    message    TEXT,              -- Message optionnel du client
    created_at TIMESTAMP              NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP              NOT NULL DEFAULT NOW(),
    UNIQUE (listing_id, buyer_id) -- Un client = une demande par annonce
);

-- ===== TABLE : reviews =====
-- Notation d'un vendeur par un client après retrait
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
-- Optimisation des recherches les plus fréquentes

-- Recherche des annonces par vendeur
CREATE INDEX idx_listings_seller_id
    ON listings (seller_id);

-- Recherche des annonces actives par type de cuisine
CREATE INDEX idx_listings_cuisine_status
    ON listings (cuisine_type_id, status);

-- Recherche géographique approximative par coordonnées
CREATE INDEX idx_listings_location
    ON listings (pickup_lat, pickup_lng);

-- Recherche des demandes reçues par un vendeur (via listing)
CREATE INDEX idx_contact_requests_listing_id
    ON contact_requests (listing_id);

-- Recherche des demandes envoyées par un client
CREATE INDEX idx_contact_requests_buyer_id
    ON contact_requests (buyer_id);

-- ===== DONNÉES INITIALES =====

-- Rôles de base (obligatoires au démarrage)
INSERT INTO roles (name)
VALUES ('ROLE_ADMIN'),
       ('ROLE_SELLER'),
       ('ROLE_BUYER');

-- Allergènes réglementaires EU (14 allergènes majeurs)
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

-- Origines culinaires initiales
INSERT INTO cuisine_types (name, description)
VALUES ('Ukrainienne', 'Cuisine traditionnelle d''Ukraine'),
       ('Française', 'Cuisine traditionnelle de France'),
       ('Italienne', 'Cuisine traditionnelle d''Italie'),
       ('Marocaine', 'Cuisine traditionnelle du Maroc'),
       ('Japonaise', 'Cuisine traditionnelle du Japon'),
       ('Mexicaine', 'Cuisine traditionnelle du Mexique'),
       ('Indienne', 'Cuisine traditionnelle d''Inde'),
       ('Libanaise', 'Cuisine traditionnelle du Liban');