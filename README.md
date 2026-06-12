# Tortiki API

[![CI — Tortiki API](https://github.com/jferminh/tortiki-api/actions/workflows/ci-cd.yml/badge.svg?branch=develop)](https://github.com/jferminh/tortiki-api/actions/workflows/ci-cd.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=tortiki-api&metric=alert_status)](https://sonarcloud.io/project/overview?id=tortiki-api)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=tortiki-api&metric=coverage)](https://sonarcloud.io/project/overview?id=tortiki-api)

> Marketplace P2P Click & Collect de plats cuisinés maison.
> Projet CDA — Titre Professionnel Concepteur Développeur d\'Applications (Niveau 6, France).

***

## Sommaire

- [Présentation](#présentation)
- [Architecture](#architecture)
- [Stack technique](#stack-technique)
- [Prérequis](#prérequis)
- [Installation et démarrage](#installation-et-démarrage)
- [Configuration](#configuration)
- [Documentation API](#documentation-api)
- [Tests](#tests)
- [Pipeline CI/CD](#pipeline-cicd)
- [Structure du projet](#structure-du-projet)
- [Conventions de développement](#conventions-de-développement)
- [Contribuer](#contribuer)

***

## Présentation

**Tortiki** est une marketplace P2P permettant à des cuisiniers amateurs (vendeurs) de proposer leurs plats faits maison à des clients locaux, selon un modèle **Click & Collect**.

### Personas

| Persona | Rôle | Besoin principal |
|---|---|---|
| **Sofia** | `ROLE_SELLER` — cuisinière ukrainienne | Publier ses plats, gérer ses commandes |
| **Théo** | `ROLE_BUYER` — étudiant smartphone | Trouver des plats proches, commander rapidement |
| **Admin** | `ROLE_ADMIN` — gestionnaire plateforme | Modérer les annonces, gérer les référentiels |

***

## Architecture

Le projet adopte une **Architecture Hexagonale** (Ports & Adapters).

```
com.tortiki.api/
├── domain/
│   ├── model/              ← POJOs métier purs (zéro annotation Spring/JPA)
│   └── exception/          ← Exceptions métier
├── application/
│   ├── port/
│   │   ├── in/             ← Ports primaires : interfaces des cas d\'usage
│   │   └── out/            ← Ports secondaires : interfaces Repository/Gateway
│   └── service/            ← Implémentations des ports in/ (@Service)
├── infrastructure/
│   └── adapter/
│       ├── in/web/         ← @RestController, DTOs (Records), mappers
│       └── out/
│           ├── persistence/ ← @Entity JPA, JpaRepository, mappers
│           ├── email/       ← JavaMailSender
│           ├── storage/     ← MinIO (photos des plats)
│           └── geolocation/ ← WebClient + Nominatim OSM
└── config/                 ← SecurityConfig, OpenApiConfig, MinIOConfig,
                               WebClientConfig, NominatimProperties
```

### Principe de dépendance

```
[HTTP / MinIO / PostgreSQL / Nominatim]
         │  implémente
         ▼
infrastructure/adapter/out/     ← détails techniques
         │  via interface
         ▼
application/port/out/           ← contrats (ports secondaires)
         │  utilisé par
         ▼
application/service/            ← logique métier pure
         │  via interface
         ▼
application/port/in/            ← contrats (ports primaires)
         │  appelé par
         ▼
infrastructure/adapter/in/web/  ← contrôleurs REST
```

**Règle absolue** : le `domain/` et `application/` ne contiennent **aucune annotation Spring, JPA
ou framework**. Toutes les dépendances techniques sont injectées via les ports.

### Flux géolocalisation — exemple concret

```
[Client HTTP]  POST /listings?city=Nancy
     │
     ▼
SearchListingController          infrastructure/adapter/in/web/
     │  appelle →
     ▼
SearchListingsUseCase            application/port/in/           (interface)
     │  implémenté par →
     ▼
SearchListingsService            application/service/
     │  utilise →                │  utilise →
     ▼                           ▼
GeolocationPort             SearchListingRepository            (interfaces port/out/)
     │  implémenté par →         │  implémenté par →
     ▼                           ▼
NominatimGateway           ListingSearchRepositoryAdapter      infrastructure/adapter/out/
     │                           │
     ▼                           ▼
[Nominatim OSM API]         [PostgreSQL 16]
```

***

## Stack technique

### Backend

| Technologie | Version | Usage |
|---|---|---|
| Java | 21 LTS (Temurin) | Langage principal |
| Spring Boot | 3.5.x | Framework applicatif |
| Spring Web | — | API REST |
| Spring Data JPA | — | Accès base de données |
| Hibernate | 6.6.x | ORM |
| Spring Security | 6 | Authentification, RBAC (sessions stateful) |
| Flyway | — | Migrations SQL versionnées |
| PostgreSQL | 16 | Base de données principale |
| MinIO SDK Java | — | Stockage photos (S3-compatible) |
| JavaMailSender | — | Envoi d\'e-mails transactionnels |
| WebClient | — | Géocodage Nominatim OSM (géolocalisation) |
| SpringDoc OpenAPI | 2.8.x | Documentation API auto-générée |
| Lombok | 1.18.38 | Réduction boilerplate (`annotationProcessorPaths`) |

### Qualité & Tests

| Outil | Usage |
|---|---|
| JUnit 5 | Tests unitaires et d\'intégration |
| Mockito | Mocks — `ExchangeFunction` pour WebClient |
| Testcontainers | PostgreSQL réel en tests d\'intégration |
| JaCoCo | Couverture de code ≥ 70 % |
| Allure Reports | Rapports visuels de tests (Epic / Feature / Story) |
| SonarCloud | Analyse statique — 0 blocker/critical |
| Checkstyle | Google Java Style Guide |
| GitHub Actions | CI/CD automatisé |

***

## Prérequis

- **Java 21 LTS** — [Adoptium Temurin](https://adoptium.net/)
- **Docker Desktop** — [docker.com](https://www.docker.com/products/docker-desktop/)
- **Maven 3.9+** — inclus via `./mvnw` (Maven Wrapper)
- **Git** — convention [Conventional Commits](https://www.conventionalcommits.org/)

> ⚠️ Le Maven Wrapper (`./mvnw`) est inclus dans le dépôt. **Ne pas utiliser `mvn` directement**
> pour garantir la version correcte de Maven.

***

## Installation et démarrage

### 1. Cloner le dépôt

```bash
git clone https://github.com/jferminh/tortiki-api.git
cd tortiki-api
git checkout develop
```

### 2. Configurer l\'environnement local

```bash
# Copier le template de configuration développement
cp src/main/resources/application-dev.yml.example \\
   src/main/resources/application-dev.yml

# Éditer application-dev.yml avec tes credentials locaux
# (voir section Configuration ci-dessous)
```

> ⚠️ `application-dev.yml` est dans `.gitignore` — ne jamais le commiter.
> Utiliser uniquement `application-dev.yml.example` pour partager la structure.

### 3. Démarrer l\'infrastructure Docker

```bash
# Démarre PostgreSQL 16 + MinIO
docker compose up -d

# Vérifier que les services sont sains
docker compose ps
```

### 4. Démarrer l\'application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 5. Vérifier le démarrage

```
✅ Flyway    : "Successfully applied N migrations to schema public"
✅ Security  : "Global AuthenticationManager configured with UserDetailsService"
✅ Tomcat    : "Started TortikiApiApplication in X seconds"
```

### Arrêter l\'application

```bash
# Ctrl+C dans le terminal Spring Boot, puis :
docker compose down
```

### Réinitialiser la base de données (dev uniquement)

```bash
docker compose down -v   # supprime les volumes
docker compose up -d     # recrée la base vierge
```

> ⚠️ **Règle Flyway** : ne jamais modifier un script déjà appliqué (`V1__init_schema.sql`).
> Toute évolution de schéma → créer `V2__nom_evolution.sql`.

***

## Configuration

### Fichiers YAML par profil

| Fichier | Commité | Profil | Usage |
|---|---|---|---|
| `application.yml` | ✅ Oui | Tous | Config commune : JPA, Flyway, SpringDoc, Nominatim |
| `application-dev.yml.example` | ✅ Oui | — | Template à copier pour le dev local |
| `application-dev.yml` | ❌ Non | `dev` | Credentials locaux (ignoré par `.gitignore`) |
| `application-prod.yml` | ✅ Oui | `prod` | Variables d\'environnement, Swagger désactivé |
| `src/test/resources/application-test.yml` | ✅ Oui | `test` | Datasource CI/CD GitHub Actions |

### Configuration locale — `application-dev.yml`

```bash
# Copier le template fourni
cp src/main/resources/application-dev.yml.example \\
   src/main/resources/application-dev.yml
```

Renseigner les valeurs dans `application-dev.yml` :

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tortiki_db
    username: VOTRE_USER
    password: VOTRE_PASSWORD

minio:
  endpoint: http://localhost:9000
  access-key: VOTRE_ACCESS_KEY
  secret-key: VOTRE_SECRET_KEY
  bucket-name: tortiki-photos
```

> Les propriétés Nominatim (`base-url`, `user-agent`, `timeout-seconds`) sont définies dans
> `application.yml` — elles ne sont **pas sensibles** et ne nécessitent pas d\'override local.

### Variables d\'environnement (production)

| Variable | Description |
|---|---|
| `DATABASE_URL` | URL JDBC PostgreSQL |
| `DATABASE_USERNAME` | Utilisateur PostgreSQL |
| `DATABASE_PASSWORD` | Mot de passe PostgreSQL |
| `MINIO_ENDPOINT` | URL endpoint MinIO |
| `MINIO_ACCESS_KEY` | Clé d\'accès MinIO |
| `MINIO_SECRET_KEY` | Clé secrète MinIO |
| `NOMINATIM_BASE_URL` | URL Nominatim (optionnel, défaut OSM) |
| `NOMINATIM_USER_AGENT` | User-Agent Nominatim (optionnel) |
| `NOMINATIM_TIMEOUT` | Timeout secondes Nominatim (optionnel, défaut 5) |

***

## Documentation API

L\'API REST est documentée automatiquement via **SpringDoc OpenAPI 3**.

| URL | Description | Disponible |
|---|---|---|
| `http://localhost:8080/swagger-ui.html` | Interface Swagger UI | Profil `dev` uniquement |
| `http://localhost:8080/api-docs` | Spec OpenAPI JSON | Profil `dev` uniquement |

> Le Swagger UI est **désactivé en production** (`application-prod.yml`).

***

## Tests

### Lancer les tests localement

```bash
# Prérequis : Docker Desktop démarré (Testcontainers)
./mvnw verify
```

### Rapport JaCoCo (couverture ≥ 70 %)

```bash
./mvnw verify
# Rapport HTML généré dans :
open target/site/jacoco/index.html   # macOS / Linux
# Windows : ouvrir target\\site\\jacoco\\index.html dans le navigateur
```

> **Périmètre JaCoCo** : seules les classes `application/service/` et `application/port/`
> sont mesurées. Les adapters infrastructure, modèles domaine et classes de configuration
> sont exclus du seuil.

### Rapport Allure

```bash
./mvnw verify
./mvnw allure:serve
# Ouvre automatiquement le rapport dans le navigateur
```

Les tests sont organisés selon la hiérarchie Allure :

```
Epic : domaine fonctionnel  (ex. Géolocalisation, Annonces)
  └── Feature : composant   (ex. NominatimGateway, ListingService)
        └── Story : scénario (ex. Recherche par ville, Validation entrées)
```

### Checkstyle Google Style

```bash
./mvnw checkstyle:check
# 0 violation obligatoire avant tout commit
```

### Résultats actuels (Sprint 2)

```
Tests run: 66  |  Failures: 0  |  Errors: 0  |  Skipped: 0
Checkstyle violations: 0
```

***

## Pipeline CI/CD

Le pipeline GitHub Actions se déclenche à chaque `push` et `pull_request` sur `develop` et `main`.

### Jobs

```
checkstyle ──► build-and-test ──► docker (main uniquement)
                                └──► notify (toujours)
```

| Job | Description | Déclencheur |
|---|---|---|
| `checkstyle` | Google Style — 0 violation | `develop`, `main` |
| `build-and-test` | Tests JUnit 5, JaCoCo, Allure, SonarCloud | `develop`, `main` |
| `docker` | Build & Push image Docker Hub | `main` push uniquement |
| `notify` | Notification Teams (optionnelle) | Toujours |

### Secrets requis

| Secret GitHub | Description |
|---|---|
| `SONAR_TOKEN` | Token SonarCloud |
| `SONAR_PROJECT_KEY` | Clé projet SonarCloud |
| `SONAR_ORGANIZATION` | Organisation SonarCloud |
| `DOCKERHUB_USERNAME` | Username Docker Hub |
| `DOCKERHUB_TOKEN` | Token Docker Hub |
| `TEAMS_WEBHOOK` | URL webhook Teams (optionnel) |

### Rapport Allure en ligne

Le rapport Allure est publié automatiquement sur GitHub Pages à chaque push sur `main` :

```
https://jferminh.github.io/tortiki-api/
```

***

## Structure du projet

```
tortiki-api/
├── .github/
│   └── workflows/
│       └── ci-cd.yml                    ← Pipeline GitHub Actions
├── src/
│   ├── main/
│   │   ├── java/com/tortiki/api/
│   │   │   ├── domain/
│   │   │   │   ├── model/               ← POJOs métier purs
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Role.java
│   │   │   │   │   ├── Listing.java
│   │   │   │   │   ├── CuisineType.java
│   │   │   │   │   ├── ContactRequest.java
│   │   │   │   │   ├── Review.java
│   │   │   │   │   └── Allergen.java
│   │   │   │   └── exception/           ← Exceptions métier
│   │   │   ├── application/
│   │   │   │   ├── port/
│   │   │   │   │   ├── in/              ← Ports primaires (cas d\'usage)
│   │   │   │   │   │   ├── SearchCriteria.java
│   │   │   │   │   │   ├── SearchListingsUseCase.java
│   │   │   │   │   │   └── SubmitContactRequestUseCase.java
│   │   │   │   │   └── out/             ← Ports secondaires
│   │   │   │   │       ├── GeolocationPort.java
│   │   │   │   │       ├── SearchListingRepository.java
│   │   │   │   │       └── ContactRequestRepository.java
│   │   │   │   └── service/             ← Logique métier (@Service)
│   │   │   │       ├── UserService.java
│   │   │   │       ├── ListingService.java
│   │   │   │       ├── CuisineTypeService.java
│   │   │   │       └── SearchListingsService.java
│   │   │   ├── infrastructure/
│   │   │   │   └── adapter/
│   │   │   │       ├── in/web/          ← Contrôleurs REST + DTOs
│   │   │   │       │   ├── AuthController.java
│   │   │   │       │   ├── ListingController.java
│   │   │   │       │   ├── CuisineTypeController.java
│   │   │   │       │   ├── GlobalExceptionHandler.java
│   │   │   │       │   └── dto/
│   │   │   │       └── out/
│   │   │   │           ├── persistence/ ← Entités JPA + Repositories
│   │   │   │           ├── storage/     ← MinioStorageAdapter.java
│   │   │   │           └── geolocation/ ← NominatimGateway.java
│   │   │   └── config/                  ← SecurityConfig, OpenApiConfig,
│   │   │                                   MinIOConfig, WebClientConfig,
│   │   │                                   NominatimProperties
│   │   └── resources/
│   │       ├── application.yml          ← Config commune (Nominatim inclus)
│   │       ├── application-dev.yml.example  ← Template à copier ✅ commité
│   │       ├── application-dev.yml          ← Credentials locaux ❌ gitignored
│   │       ├── application-prod.yml
│   │       └── db/migration/
│   │           └── V1__init_schema.sql
│   └── test/
│       ├── java/com/tortiki/api/
│       │   ├── application/service/     ← Tests unitaires services
│       │   └── infrastructure/adapter/
│       │       ├── in/web/              ← Tests @WebMvcTest contrôleurs
│       │       └── out/
│       │           ├── geolocation/     ← NominatimGatewayTest (ExchangeFunction)
│       │           ├── storage/         ← MinioStorageAdapterTest
│       │           └── persistence/     ← UserDetailsServiceImplTest
│       └── resources/
│           └── application-test.yml
├── .gitignore                           ← application-dev.yml ignoré
├── docker-compose.yml                   ← PostgreSQL 16 + MinIO
├── sonar-project.properties             ← Config SonarCloud
├── checkstyle.xml                       ← Règles Google Style
├── pom.xml
└── README.md
```

***

## Conventions de développement

### Branches

```
main        ← production stable
develop     ← intégration continue
feat/sprint{N}-{description}
fix/sprint{N}-{description}
chore/{phase}-{description}
docs/{description}
```

### Dépendances entre branches (Sprint 2)

Quand une branche B dépend du code d\'une branche A non encore mergée,
créer B **à partir de** A — pas à partir de `develop` :

```bash
git checkout feat/sprint2-search-usecase      # branche A (GeolocationPort)
git checkout -b feat/sprint2-nominatim-gateway # branche B part de A
```

Les PR sont ensuite chaînées :
```
PR 1 : feat/sprint2-search-usecase       → develop
PR 2 : feat/sprint2-nominatim-gateway    → feat/sprint2-search-usecase
       (redirigée vers develop automatiquement après merge de PR 1)
```

### Commits — Conventional Commits

```
feat(domain): ajout POJO User et Role
chore(ci): pipeline GitHub Actions CI/CD
fix(security): correction configuration BCrypt
refactor(adapter): extraction mapper ListingEntity
test(service): tests unitaires ListingService
docs(readme): mise à jour configuration et structure projet
```

### Langue

| Élément | Langue |
|---|---|
| Code Java (classes, méthodes, variables) | **Anglais** |
| Javadoc et commentaires | **Français formel** |
| Messages de commit | **Anglais** (type) + **Français** (description) |
| Documentation CDA | **Français** |

### Checkstyle — Règles clés

- Indentation : **2 espaces** (pas de tabulations)
- Imports : pas de star imports (`jakarta.persistence.*` interdit)
- Ordre imports : `static` → `com` → `jakarta` → `lombok` → `net` → `org` → `java` → `javax`
- Opérateur `+` en **début** de ligne (OperatorWrap)
- Javadoc **obligatoire** sur toutes les classes publiques
- IntelliJ : _Class count to use import \'*\'_ → `999`

***

## Contribuer

1. Vérifier les dépendances entre branches avant de créer la vôtre
2. Créer la branche depuis la bonne base : `git checkout -b feat/sprint2-ma-feature`
3. Vérifier Checkstyle avant le commit : `./mvnw checkstyle:check`
4. Lancer les tests : `./mvnw verify`
5. Pousser la branche : `git push origin feat/sprint2-ma-feature`
6. Ouvrir une Pull Request vers `develop` (ou la branche parente si chaînée)
7. Attendre que le pipeline CI passe (**badge vert obligatoire**)

***

*Projet réalisé dans le cadre du titre professionnel CDA — Niveau 6 — France*