# Tortiki API

[![CI — Tortiki API](https://github.com/jferminh/tortiki-api/actions/workflows/ci-cd.yml/badge.svg?branch=develop)](https://github.com/jferminh/tortiki-api/actions/workflows/ci-cd.yml)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=tortiki-api&metric=alert_status)](https://sonarcloud.io/project/overview?id=tortiki-api)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=tortiki-api&metric=coverage)](https://sonarcloud.io/project/overview?id=tortiki-api)

> Marketplace P2P Click & Collect de plats cuisinés maison.
> Projet CDA — Titre Professionnel Concepteur Développeur d'Applications (Niveau 6, France).

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
│   │   ├── in/             ← Ports primaires : interfaces des cas d'usage
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
└── config/                 ← SecurityConfig, OpenApiConfig, MinIOConfig
```

**Principe clé** : le `domain/` ne connaît ni Spring, ni JPA, ni aucun framework. Les dépendances techniques sont injectées via les ports.

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
| Spring Security | 6 | Authentification, RBAC |
| Flyway | — | Migrations SQL versionnées |
| PostgreSQL | 16 | Base de données principale |
| MinIO SDK Java | — | Stockage photos (S3-compatible) |
| JavaMailSender | — | Envoi d'e-mails |
| WebClient | — | Géocodage Nominatim OSM |
| SpringDoc OpenAPI | 2.8.x | Documentation API auto-générée |
| Lombok | 1.18.38 | Réduction boilerplate |

### Qualité & Tests

| Outil | Usage |
|---|---|
| JUnit 5 | Tests unitaires et d'intégration |
| Mockito | Mocks |
| Testcontainers | PostgreSQL réel en tests |
| JaCoCo | Couverture de code ≥ 70% |
| Allure Reports | Rapports visuels de tests |
| SonarCloud | Analyse statique de la qualité |
| Checkstyle | Google Java Style Guide |
| GitHub Actions | CI/CD automatisé |

***

## Prérequis

- **Java 21 LTS** — [Adoptium Temurin](https://adoptium.net/)
- **Docker Desktop** — [docker.com](https://www.docker.com/products/docker-desktop/)
- **Maven 3.9+** — inclus via `./mvnw` (Maven Wrapper)
- **Git** — convention [Conventional Commits](https://www.conventionalcommits.org/)

> ⚠️ Le Maven Wrapper (`./mvnw`) est inclus dans le dépôt. **Ne pas utiliser `mvn` directement** pour garantir la version correcte de Maven.

***

## Installation et démarrage

### 1. Cloner le dépôt

```bash
git clone https://github.com/jferminh/tortiki-api.git
cd tortiki-api
git checkout develop
```

### 2. Démarrer l'infrastructure Docker

```bash
# Démarre PostgreSQL 16 + MinIO
docker compose up -d

# Vérifier que les services sont sains
docker compose ps
```

### 3. Démarrer l'application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 4. Vérifier le démarrage

```
✅ Flyway : "Successfully applied 1 migration to schema public"
✅ Security : "Global AuthenticationManager configured with UserDetailsService"
✅ Tomcat : "Started TortikiApiApplication in X seconds"
```

### Arrêter l'application

```bash
# Ctrl+C dans le terminal Spring Boot, puis :
docker compose down
```

### Réinitialiser la base de données (dev uniquement)

```bash
docker compose down -v   # supprime les volumes
docker compose up -d     # recrée la base vierge
```

> ⚠️ **Règle Flyway** : ne jamais modifier `V1__init_schema.sql` après application. Toute évolution de schéma → créer `V2__nom_evolution.sql`.

***

## Configuration

### Fichiers YAML par profil

| Fichier | Profil | Usage |
|---|---|---|
| `application.yml` | Tous | Config commune : JPA, Flyway, SpringDoc |
| `application-dev.yml` | `dev` | Datasource PostgreSQL Docker locale |
| `application-prod.yml` | `prod` | Variables d'environnement, Swagger désactivé |
| `src/test/resources/application-test.yml` | `test` | Datasource CI/CD GitHub Actions |

### Variables d'environnement (production)

| Variable | Description |
|---|---|
| `DATABASE_URL` | URL JDBC PostgreSQL |
| `DATABASE_USERNAME` | Utilisateur PostgreSQL |
| `DATABASE_PASSWORD` | Mot de passe PostgreSQL |
| `MINIO_ENDPOINT` | URL endpoint MinIO |
| `MINIO_ACCESS_KEY` | Clé d'accès MinIO |
| `MINIO_SECRET_KEY` | Clé secrète MinIO |

***

## Documentation API

L'API REST est documentée automatiquement via **SpringDoc OpenAPI 3**.

| URL | Description | Disponible |
|---|---|---|
| `http://localhost:8080/swagger-ui.html` | Interface Swagger UI | Profils `dev` uniquement |
| `http://localhost:8080/api-docs` | Spec OpenAPI JSON | Profils `dev` uniquement |

> Le Swagger UI est **désactivé en production** (`application-prod.yml`).

***

## Tests

### Lancer les tests localement

```bash
# Prérequis : Docker Desktop démarré (Testcontainers)
./mvnw verify
```

### Rapport JaCoCo (couverture ≥ 70%)

```bash
./mvnw verify
# Rapport HTML généré dans :
open target/site/jacoco/index.html
```

### Rapport Allure

```bash
./mvnw verify
./mvnw allure:serve
# Ouvre automatiquement le rapport dans le navigateur
```

### Checkstyle Google Style

```bash
./mvnw checkstyle:check
# 0 violation obligatoire avant tout commit
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
│       └── ci-cd.yml               ← Pipeline GitHub Actions
├── src/
│   ├── main/
│   │   ├── java/com/tortiki/api/
│   │   │   ├── domain/             ← Métier pur
│   │   │   ├── application/        ← Ports + Services
│   │   │   ├── infrastructure/     ← Adapters
│   │   │   └── config/             ← Configuration Spring
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/
│   │           └── V1__init_schema.sql
│   └── test/
│       ├── java/com/tortiki/api/
│       └── resources/
│           └── application-test.yml
├── docker-compose.yml              ← PostgreSQL 16 + MinIO
├── sonar-project.properties        ← Config SonarCloud
├── checkstyle.xml                  ← Règles Google Style
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

### Commits — Conventional Commits

```
feat(domain): ajout POJO User et Role
chore(ci): pipeline GitHub Actions CI/CD
fix(security): correction configuration BCrypt
refactor(adapter): extraction mapper ListingEntity
test(service): tests unitaires ListingService
docs(readme): mise à jour prérequis Java 21
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

***

## Contribuer

1. Créer une branche depuis `develop` : `git checkout -b feat/sprint1-ma-feature`
2. Vérifier Checkstyle avant le commit : `./mvnw checkstyle:check`
3. Lancer les tests : `./mvnw verify`
4. Pousser la branche : `git push origin feat/sprint1-ma-feature`
5. Ouvrir une Pull Request vers `develop`
6. Attendre que le pipeline CI passe (**badge vert obligatoire**)

***

*Projet réalisé dans le cadre du titre professionnel CDA — Niveau 6 — France*