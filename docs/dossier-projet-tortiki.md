
## Section 1 — Compétences mises en œuvre

### Objectif de la section

Cette section liste les 13 compétences du référentiel CDA réparties en 3 blocs, et précise pour chacune comment le projet Tortiki en apporte la preuve concrète. Chaque compétence sera ensuite illustrée dans le corps du dossier par des extraits de code, captures d'écran ou artefacts de gestion de projet.

### Bloc 1 — Développer une application sécurisée

| Compétence | Mise en œuvre dans Tortiki |
|---|---|
| Installer et configurer son environnement de travail | Setup Java 21, Spring Boot 3.5, Docker Compose (PostgreSQL 16, MinIO), profils YAML dev/prod/test |
| Développer des interfaces utilisateur | Maquettes PlantUML puis templates Thymeleaf/Bootstrap 5 pour les 8 écrans (accueil, fiche annonce, dashboard vendeur…) |
| Développer des composants métier | Services applicatifs `UserService`, `ListingService`, `SearchListingsService`, `SubmitReviewService` en couche `application/service` |
| Contribuer à la gestion d'un projet informatique | Découpage en Phase 0 + 4 sprints, suivi GitHub Projects/Issues, Gitflow avec Conventional Commits |

### Bloc 2 — Concevoir et développer une application sécurisée organisée en couches

| Compétence | Mise en œuvre dans Tortiki |
|---|---|
| Analyser les besoins et maquetter une application | Cahier des charges v3.0, personas Sofia/Théo, priorisation MoSCoW, 8 maquettes fonctionnelles |
| Définir l'architecture logicielle d'une application | Architecture Hexagonale stricte imposée : domain / application (port in/out) / infrastructure (adapter in/out) / config |
| Concevoir et mettre en place une base de données relationnelle | Modélisation des entités User, Role, Listing, CuisineType, ContactRequest, Review, Allergen ; migrations Flyway V1 à V6 |
| Développer des composants d'accès aux données SQL et NoSQL | Adaptateurs JPA (`UserRepositoryAdapter`, `ListingRepositoryAdapter`) avec Testcontainers PostgreSQL, `MinioStorageAdapter` pour le stockage objet |

<div style="page-break-inside: avoid;">

### Bloc 3 — Préparer le déploiement d'une application sécurisée

| Compétence | Mise en œuvre dans Tortiki |
|---|---|
| Préparer et exécuter les plans de tests d'une application | 146 tests JUnit 5 (unitaires + intégration), Mockito, rapports Allure, couverture JaCoCo ≥ 70% |
| Préparer et documenter le déploiement d'une application | Pipeline GitHub Actions (4 jobs : Checkstyle, Build/Test, Docker, Notify), documentation OpenAPI/Swagger |
| Contribuer à la mise en production dans une démarche DevOps | CI/CD shift-left, SonarCloud, Docker Compose reproductible, déploiement HTTPS prévu en Sprint 4 |

### Compétence transversale — Sécurité

La sécurité est intégrée à chaque bloc plutôt qu'isolée, conformément à l'esprit "secure by design" du référentiel : Spring Security 6 en sessions stateful, RBAC (ADMIN/SELLER/BUYER), BCrypt force 12, protection CSRF, et conformité RGPD/OWASP Top 10 documentées dès la conception. Cette approche permet de démontrer la compétence sécurité de façon récurrente plutôt qu'en un bloc isolé.

</div>

<div style="page-break-before: always;"></div>

## Section 2 — Cahier des charges et expression des besoins

### 2.1 Présentation du projet

Tortiki est une application web de type marketplace P2P Click & Collect, permettant à des cuisiniers amateurs de vendre des plats faits maison à une clientèle locale. Le projet répond à un vide laissé par les plateformes de livraison traditionnelles, orientées professionnels de la restauration, en proposant un cadre local, accessible et sécurisé, orienté découverte gastronomique.

### 2.2 Contexte et objectif général

L'objectif est de créer un MVP démontrable permettant à un vendeur de publier une annonce, à un client de la rechercher et d'exprimer son intérêt, puis au vendeur de confirmer la demande avant le retrait. Six mois après mise en ligne, les cibles fixées sont 50 vendeurs actifs, 200 acheteurs actifs, une note moyenne de satisfaction de 4,25, un temps de réponse API p95 de 500 ms et une disponibilité mensuelle de 99,5%.

### 2.3 Personas

| Persona | Profil | Besoin principal |
|---|---|---|
| Sofia (Vendeuse) | 38 ans, Nancy, cuisinière ukrainienne | Générer un revenu complémentaire et partager sa culture culinaire |
| Théo (Client) | 26 ans, étudiant, usage smartphone | Trouver rapidement un plat varié et abordable près de lui |

### 2.4 Périmètre fonctionnel MVP

Le MVP couvre l'inscription, la gestion des rôles vendeur/client, le CRUD d'annonces avec photo et créneau de retrait unique, la recherche géographique, le filtrage par origine culinaire et allergènes, l'expression d'intérêt, la confirmation/refus par le vendeur, la notation après retrait, ainsi que l'administration du référentiel des cuisines. Sont explicitement exclus de la v1 : la livraison à domicile, l'application mobile native, la messagerie instantanée, le panier multi-vendeurs, les notifications push et l'authentification JWT (reportée en v2).

<div style="page-break-inside: avoid;">

### 2.5 User stories principales et priorisation MoSCoW

| ID | User Story | Priorité |
|---|---|---|
| US-01 | Inscription visiteur (email, mot de passe fort, validation serveur) | Must |
| US-02 | Choix et cumul du rôle vendeur/client | Must |
| US-03 | Création/modification/suppression d'annonce  | Must |
| US-05 | Gestion des demandes d'intérêt reçues (confirmer/refuser) | Must |
| US-06 | Recherche géographique par ville/code postal | Must |
| US-07 | Filtres origine culinaire et allergènes | Must |
| US-08 | Expression d'intérêt et obtention des coordonnées vendeur | Must |
| US-09 | Notation du vendeur après retrait | Should |
| US-10 | Administration des origines culinaires et modération | Must |

</div>

### 2.6 Priorisation MoSCoW

La priorisation distingue les Must Have (les 10 US du MVP) des Should Have — notation vendeur, email transactionnel, historique des demandes, gestion étendue des utilisateurs. Les fonctionnalités Could Have (livraison, application mobile, JWT, messagerie temps réel) et Won't Have v1 sont clairement exclues du périmètre pour préserver la cohérence pédagogique du projet.

### 2.7 Exigences non fonctionnelles

Le cahier des charges fixe des exigences précises en performance (500 ms p95), disponibilité (99,5%), sécurité (OWASP Top 10, BCrypt, TLS/HTTPS), accessibilité (RGAA 4.1 niveau AA) et RGPD (droit à l'effacement, durées de conservation documentées par entité). La maintenabilité est également exigée via une couverture de tests cible de 70% minimum et une analyse continue SonarCloud sans anomalie bloquante.

### 2.8 Stack technique retenue

| Domaine | Choix |
|---|---|
| Backend | Java 21, Spring Boot 3.5.x, Architecture Hexagonale |
| Frontend | Thymeleaf 3.1, Bootstrap 5, OpenFeign |
| Base de données | PostgreSQL 16, Flyway |
| Sécurité | Spring Security 6, sessions HTTP stateful (JWT différé en v2)  |
| Qualité | JUnit 5, Testcontainers, JaCoCo, SonarCloud, Checkstyle |

<div style="page-break-before: always;"></div>

## Section 4 — Gestion de projet

### 4.1 Planning global et milestones

Le projet Tortiki est découpé en une Phase 0 de cadrage suivie de 4 sprints, avec une deadline finale du MVP fixée au 17 juillet 2026. Le planning global se structure ainsi :

| Milestone | Période | Contenu | Statut |
|---|---|---|---|
| Phase 0 — Cadrage | 13/05 – 24/05/2026 | Socle technique, sécurité, CI/CD (issues 1-10) | Clôturée |
| Sprint 1 — Socle | 25/05 – 07/06/2026 | Domain, application, persistance, auth (issues 11-28) | Clôturée |
| Sprint 2 — Découverte | 08/06 – 21/06/2026 | Recherche géolocalisée, allergènes, contact (issues 29-39) | Clôturée |
| Sprint 3 — Confiance | 22/06 – 05/07/2026 | Notation, admin, dashboard vendeur (issues 40-48) | Clôturée |
| Sprint 4 — Livraison | 06/07 – 17/07/2026 | Déploiement, documentation finale (issues 49-58)  | En cours |

### 4.2 Suivi et outils de collaboration

Le pilotage repose sur deux dépôts GitHub distincts (tortiki-api et tortiki-frontend), avec GitHub Projects pour la gestion des issues, un Gitflow strict (branches feat/fix/test préfixées par sprint, merge sur develop) et des Conventional Commits systématiques. Chaque bloc de travail suit un cycle checkout → développement → tests → commit documenté → validation explicite avant merge.

### 4.3 Environnement humain

| Acteur | Rôle | Responsabilité |
|---|---|---|
| Porteur de projet | Maîtrise d'ouvrage | Définit les besoins, valide les livrables |
| Développeur CDA | Maîtrise d'œuvre | Conçoit, développe, teste et documente l'application |
| Utilisateurs pilotes | Testeurs fonctionnels | Vérifient les parcours métier (personas Sofia/Théo) |
| Jury CDA | Évaluateur | Évalue la conformité au référentiel CDA|

### 4.4 Objectifs de qualité

### Seuils contractuels retenus

La démarche qualité de Tortiki repose sur quatre seuils mesurables, vérifiés automatiquement à chaque commit via le pipeline CI/CD : zéro violation Checkstyle Google Style, couverture JaCoCo ≥ 70%, ensemble des tests JUnit 5 au vert, et analyse SonarCloud sans anomalie bloquante ou critique. Ces contrôles ont été posés dès la Phase 0, avant même l'écriture des premiers tests métier, selon une démarche Shift Left visant à détecter les problèmes le plus tôt possible dans le cycle de développement.

### Suivi chronologique des indicateurs

| Date | Périmètre | Tests JUnit 5 | Couverture JaCoCo | Checkstyle | SonarCloud |
|---|---|---|---|---|---|
| 09/06/2026 | Backend (domain/service) | 33 tests passants | 100% sur domain/service | 0 violation | Configuré, sans blocker |
| 24/06/2026 | Backend (API complète) | 86 tests, 0 échec | Seuil 70% atteint (all coverage checks met) | 0 violation | Aucun blocker/critical |
| 25/06/2026 | Backend (Sprint 3) | 94 tests, 0 échec | Seuil 70% atteint (all coverage checks met) | 0 violation  | Aucun blocker/critical |
| 13/07/2026 | Frontend (9 contrôleurs) | 68 tests, 0 échec | Seuil 70% atteint sur 38 classes  | 0 violation | Aucun blocker/critical |

<div style="page-break-before: always;"></div>

## Section 5 — Spécifications fonctionnelles

### 5.1 Acteurs du système

Tortiki repose sur trois rôles distincts gérés via RBAC dans Spring Security 6, chacun correspondant à un persona ou une fonction métier précise.

| Rôle | Persona | Périmètre fonctionnel |
|---|---|---|
| ROLE_BUYER | Théo, étudiant, usage mobile | Recherche, consultation fiche plat, demande de contact, avis |
| ROLE_SELLER | Sofia, cuisinière ukrainienne | Publication d'annonces, gestion des demandes reçues, tableau de bord |
| ROLE_ADMIN | Gestionnaire plateforme | Modération des annonces, gestion des types de cuisine, panel admin |

### 5.2 User stories principales

Les user stories couvrent le parcours complet Click & Collect, de la recherche géolocalisée jusqu'à l'avis post-transaction. Elles ont été traduites en issues GitHub réparties sur les quatre sprints, avec traçabilité directe vers les composants Spring Boot livrés.

- En tant que Théo, je veux rechercher des plats par ville et type de cuisine pour trouver une annonce proche de moi.
- En tant que Théo, je veux consulter la fiche d'un plat (prix, allergènes, avis) avant de faire une demande.
- En tant que Théo, je veux soumettre une demande de contact avec un nombre de portions et un message optionnel.
- En tant que Sofia, je veux publier une annonce avec photo, prix, portions et créneau de retrait unique.
- En tant que Sofia, je veux consulter mon tableau de bord pour confirmer ou refuser les demandes reçues.
- En tant qu'admin, je veux désactiver une annonce non conforme et gérer le référentiel des types de cuisine.

<div style="page-break-inside: avoid;">

### 5.3 Parcours utilisateur (flux)

Le parcours acheteur suit une séquence linéaire : recherche géolocalisée → fiche annonce → formulaire de contact → suivi des demandes, tandis que le parcours vendeur boucle sur la création d'annonce et la gestion des retours clients. Ce découpage en états (liste, détail, formulaire, confirmation) structure directement les huit écrans maquettés en Phase 0.

| Étape | Écran | Acteur |
|---|---|---|
| 1 | Accueil / Recherche géolocalisée | Théo |
| 2 | Fiche annonce (détail plat, allergènes, avis) | Théo |
| 3 | Formulaire de contact (portions, message) | Théo |
| 4 | Mes demandes (historique statuts) | Théo |
| 5 | Tableau de bord vendeur | Sofia |
| 6 | Créer une annonce | Sofia |
| 7 | Gérer une demande (confirmer/refuser) | Sofia |
| 8 | Inscription / Connexion | Commun |

Toutes ces données proviennent de la répartition des huit écrans validée en Phase 0.

</div>

### 5.4 Modélisation UML

Le diagramme de cas d'utilisation formalise les interactions entre les trois acteurs et le système autour de quatre grands cas : rechercher une annonce, publier une annonce, soumettre une demande de contact, et administrer la plateforme. Ce diagramme s'appuie directement sur les ports primaires de l'architecture hexagonale (`SearchListingsUseCase`, `ManageListingUseCase`, `SubmitContactRequestUseCase`), garantissant une cohérence totale entre la spécification fonctionnelle et l'implémentation Java.

<div style="page-break-before: always;"></div>

## Section 6 — Architecture & Base de données

### 6.1 Architecture Hexagonale retenue

L'architecture hexagonale a été imposée par le tuteur CDA dès la Phase 0, avec une règle fondamentale : les dépendances ne pointent que vers l'intérieur, le domaine ne connaissant jamais Spring ni JPA. Ce choix permet de tester le métier sans base de données, de remplacer PostgreSQL par une autre technologie sans toucher au domaine, et de produire une architecture justifiable devant le jury.

| Couche | Rôle | Exemple |
|---|---|---|
| domain/model | POJOs métier purs, zéro annotation | User.java, Listing.java |
| domain/exception | Exceptions métier | ListingNotFoundException |
| application/port/in | Interfaces cas d'usage | ManageListingUseCase |
| application/port/out | Interfaces Repository/Gateway | ListingRepository, StoragePort |
| application/service | Implémentation des ports in | ListingService |
| infrastructure/adapter/out/persistence | Entités JPA, JpaRepository | ListingEntity, ListingRepositoryAdapter |
| infrastructure/adapter/in/web | Contrôleurs REST, DTOs Records | ListingController |

La séparation stricte entre `domain/model/User.java` (POJO pur) et `infrastructure/adapter/out/persistence/UserEntity.java` (entité JPA annotée) illustre concrètement cette règle : si demain PostgreSQL est remplacé par MongoDB, seule `UserEntity` est modifiée, jamais `User` ni `UserService`.

### 6.2 Modèle Conceptuel et Logique de Données

Le MCD couvre huit entités métier avec leurs cardinalités exactes : User, Role, Listing, CuisineType, Allergen, ContactRequest, Review, ainsi que les tables d'association nécessaires. Deux types énumérés PostgreSQL natifs (`listingstatus`, `contactrequeststatus`) renforcent l'intégrité des données par rapport à un simple champ VARCHAR.

| Entité | Relations principales |
|---|---|
| User | 1,n vers Listing (vendeur), n,n vers Role via userroles |
| Listing | n,1 vers CuisineType, n,n vers Allergen via listingallergens |
| ContactRequest | n,1 vers Listing et User (acheteur), contrainte UNIQUE(listingid, buyerid) |
| Review | 1,1 vers ContactRequest, n,1 vers User (reviewer/seller) et Listing |

Le MLD a été formalisé sous forme de diagramme de classes hexagonal, organisé en cinq packages colorés (domain/model, port/in, port/out, service, adapter), incluant les ports anticipés des sprints suivants comme `ContactRequestRepository` et `StoragePort`.

### 6.3 Scripts Flyway et évolution du schéma

La règle Flyway appliquée sur le projet est absolue : un script déjà exécuté en base ne doit jamais être modifié, toute évolution passe par une nouvelle version. Cette discipline a été respectée dès la première dette technique rencontrée en Sprint 1.

| Version | Contenu | Déclencheur |
|---|---|---|
| V1__init_schema.sql | 7 tables, types ENUM, contraintes CHECK/UNIQUE, index, données initiales (rôles, allergènes, cuisines) | Schéma initial Phase 0 |
| V2__add_user_profile_fields.sql | Ajout phone, avatar_url, city, latitude, longitude sur users | Localisation vendeur manquante pour recherche de proximité |
| V3 | Table reviews et contraintes associées | Ajout du système d'évaluation Sprint 3 |
| V4 | Migrations complémentaires Sprint 3 | Enrichissements ContactRequest |

Le script V1 intègre des index ciblés comme `idx_listings_cuisine_status` (composite cuisine + statut) et `idx_listings_location` (recherche géographique approximative), reflétant une optimisation dès la conception plutôt qu'un ajout tardif.

### 6.4 Décisions techniques justifiées

| Décision | Choix retenu | Justification |
|---|---|---|
| Architecture | Hexagonale | Imposée par le tuteur CDA |
| Sécurité v1 | Sessions stateful | JWT reporté en v2, point de veille CDA documenté |
| Lombok | Version 1.18.38 | Seule version compatible sans erreur avec Java 21 |
| updatePhoto | Reçoit un InputStream | Inversion de dépendance : ListingService ne connaît pas MinIO |
| Latitude/longitude | BigDecimal | Cohérence avec le type NUMERIC(10,7) PostgreSQL  |

<div style="page-break-before: always;"></div>

## Section 7 — Sécurité

### 7.1 Stratégie d'authentification

Le MVP retient des sessions HTTP stateful plutôt qu'un JWT, ce dernier étant volontairement reporté en v2 pour maîtriser la complexité initiale et privilégier la lisibilité pédagogique. Spring Boot auto-configure `DaoAuthenticationProvider` dès que les beans `UserDetailsService` et `PasswordEncoder` sont présents dans le contexte, la déclaration manuelle du provider étant inutile et dépréciée depuis Spring Security 6.4.

| Aspect | Choix retenu | Justification |
|---|---|---|
| Type de session | Stateful, `SessionCreationPolicy.ALWAYS` | Cohérent avec une API consommée par un frontend Thymeleaf/Feign |
| Sessions concurrentes | `maximumSessions(1)`, `maxSessionsPreventsLogin(false)` | Une session active par utilisateur, la nouvelle remplace l'ancienne |
| Encodage mot de passe | BCrypt force 12 | Recommandation OWASP pour le stockage des mots de passe |
| AuthenticationProvider | Non déclaré manuellement | Auto-configuré par Spring Boot depuis 6.4, évite le code déprécié |

### 7.2 Contrôle d'accès par rôle (RBAC)

Le RBAC repose sur trois rôles fixes déclarés en constantes dans `SecurityConfig` : `ROLE_ADMIN`, `ROLE_SELLER`, `ROLE_BUYER`. Les routes sont ordonnées par niveau de criticité croissant, des endpoints publics jusqu'à l'administration.

| Niveau | Exemple de route | Règle |
|---|---|---|
| Public | `GET /api/v1/listings`, `/api/v1/reviews`, Swagger UI | `permitAll()` |
| Authentification | `POST /api/v1/auth/register`, `/login`, `/logout` | `permitAll()` (nécessaire avant connexion) |
| Acheteur | `POST /api/v1/contact-requests`, `POST /api/v1/reviews` | `hasRole(ROLE_BUYER)` |
| Vendeur | `POST/PUT/DELETE /api/v1/listings`, dashboard vendeur | `hasRole(ROLE_SELLER)` |
| Administration | `/api/v1/admin/**`, gestion cuisine-types | `hasRole(ROLE_ADMIN)` |
| Reste | Toute autre route | `anyRequest().authenticated()` |

Les constantes de routes (`SecurityConstants.ROUTE_LISTING_BY_ID`, `ROUTE_REVIEWS`, etc.) éliminent toute duplication de littéraux, une exigence Checkstyle/SonarCloud appliquée dès la Phase 0.

### 7.3 CSRF et RGPD

Le CSRF est désactivé uniquement sur le préfixe `/api/v1` car l'API est consommée exclusivement par des clients HTTP (OpenFeign, curl, HTTP Client) sans formulaires HTML, le vecteur d'attaque CSRF n'existant donc pas côté API pure ; cette décision est documentée en Javadoc et référencée OWASP dans le dossier CDA. Le CSRF reste actif sur les éventuelles routes non-API (formulaires Thymeleaf côté frontend).

Côté RGPD, deux décisions structurantes ont été prises : le DTO `ReviewResponse` n'expose jamais l'email du reviewer, uniquement son prénom (minimisation des données), et le `NominatimGateway` transmet uniquement des coordonnées géographiques à l'API OSM sans aucune donnée personnelle.

| Risque RGPD | Traitement appliqué |
|---|---|
| Exposition email acheteur | Jamais renvoyé dans les DTOs publics (`ReviewResponse`, `ListingCardResponse`) |
| Géolocalisation via Nominatim | Aucune donnée personnelle transmise, uniquement adresse/coordonnées |
| Mot de passe | Stocké exclusivement en hash BCrypt, jamais en clair |

### 7.4 Couverture OWASP Top 10

| Catégorie OWASP | Mesure Tortiki |
|---|---|
| A01 Broken Access Control | RBAC strict par `hasRole`, `@PreAuthorize` sur `submitReview` |
| A02 Cryptographic Failures | BCrypt force 12 sur tous les mots de passe |
| A03 Injection | Requêtes Spring Data JPA paramétrées, aucune requête SQL concatenée |
| A05 Security Misconfiguration | Swagger désactivé en prod, gestion JSON explicite des erreurs 401/403 |
| A07 Identification and Authentication Failures | Sessions limitées à 1 par utilisateur, `UserDetailsServiceImpl` filtre les comptes `enabled=false` |

Les erreurs d'authentification et d'autorisation renvoient des réponses JSON explicites (401 « Connexion requise », 403 « Droits insuffisants ») plutôt que les pages par défaut de Spring Security, une pratique alignée sur les exigences OWASP de gestion d'erreur contrôlée.

### 7.5 Veille et évolution documentée

La dépréciation de `setUserDetailsService` et `DaoAuthenticationProvider` avec Spring Security 6.4 a été traitée comme un exemple concret de veille technique pour la Section 8 du dossier CDA, avec migration vers l'auto-configuration par détection de beans. Le passage au JWT reste un point de veille documenté pour la v2, sans impact sur l'architecture hexagonale actuelle.

<div style="page-break-before: always;"></div>

## Section 8 — Réalisations

### 8.1 Couche Domain (backend)

Tous les POJOs métier sont purs, sans aucune annotation Spring ni JPA : `User`, `Role`, `RoleName`, `Listing`, `CuisineType`, `ListingStatus`, `Allergen`, `ContactRequest`, `ContactRequestStatus`, et `Review` ajouté au Sprint 3. Quinze exceptions métier couvrent l'ensemble des cas d'usage, dont `ReviewAlreadyExistsException`, `ReviewNotAllowedException` et `InvalidStatusTransitionException`.

| Entité | Sprint |
|---|---|
| Listing.java | Phase 0 |
| User.java | Phase 0 |
| ContactRequest.java + ContactRequestStatus.java | Sprint 1 |
| Review.java | Sprint 3 |

### 8.2 Couche Application (ports et services)

Douze ports primaires (in) couvrent tous les cas d'usage, incluant `SubmitReviewUseCase` et `FindReviewsUseCase`, ce dernier séparé du premier par respect strict du principe CQS déjà appliqué sur `ManageListingUseCase.findBySeller`. Dix ports secondaires (out) découplent le domaine des technologies externes, avec `StoragePort` et `GeolocationPort` démontrant une inversion de dépendance aboutie : MinIO et Nominatim restent interchangeables sans toucher au domaine.

| Service | Rôle |
|---|---|
| ListingService.java | Service principal, CRUD + upload photo |
| ContactRequestService.java | Soumission des demandes |
| SubmitReviewService.java | Orchestre 4 règles métier séquentielles (annonce existe, acheteur actif, demande confirmée, pas de doublon)|
| SearchListingsService.java | Moteur de recherche géolocalisé via Nominatim |

### 8.3 Couche Infrastructure (adapters backend)

Côté persistence, tous les adapters JPA sont livrés avec `AbstractIntegrationTest` (Testcontainers), `UserDetailsServiceImpl`, et un correctif `JOIN FETCH` sur `ListingJpaRepository` pour éliminer une `LazyInitializationException`. Côté web, six contrôleurs REST exposent l'API avec DTOs Records Java 21 : `AuthController`, `ListingController`, `CuisineTypeController`, `SearchListingController`, `ContactRequestController`, `SellerDashboardController`.

| Adapter | Détail |
|---|---|
| MinioStorageAdapter | Upload photo avec slug UUID |
| NominatimGateway | WebClient, conforme RGPD, aucune donnée personnelle transmise  |
| ReviewController | `GET /api/v1/reviews?listingId=` public, DTO enrichi de `reviewerFirstName` (jamais l'email) |

### 8.4 Réalisations frontend (tortiki-frontend)

Le frontend Thymeleaf compte dix contrôleurs MVC (jamais `@RestController`), dont `SellerListingController` reflétant la centralité de la gestion d'annonces. L'architecture applique strictement SOLID : SRP par contexte fonctionnel (Search/Listing/Dashboard), ISP via des clients Feign granulaires plutôt qu'un god-object, et DIP puisque les contrôleurs dépendent des interfaces Feign, jamais d'implémentations HTTP.

| Template | Statut | Rôle |
|---|---|---|
| fragments/layout.html | Confirmé | Head, navbar, footer mutualisés, Bootstrap 5 via WebJars  |
| home.html | Confirmé | Hero, recherche par ville, cuisines du monde |
| search-results.html | Confirmé | Grille de résultats, filtres, pagination |
| dashboard.html | Confirmé | Demandes reçues, actions Confirmer/Refuser |
| listing-detail.html | Confirmé | Fiche plat, avis, formulaire de contact inline |
| buyer-requests.html | Confirmé | Historique des demandes acheteur |

### 8.5 Écart API/frontend résolu — Reviews sur listing-detail

Trois écarts distincts ont été identifiés puis corrigés entre la livraison initiale du template et le contrat API réel, un cas d'école de traçabilité pour le dossier CDA.

| Écart détecté | Cause | Correction appliquée |
|---|---|---|
| `GET /api/v1/reviews` absent | Seul le POST existait initialement | Nouveau port `FindReviewsUseCase`, service, contrôleur public sans authentification |
| `ReviewResponse` sans identité auteur | DTO pensé uniquement pour la soumission | Ajout de `reviewerFirstName`, jamais l'email (minimisation RGPD) |
| `listing.reviews` imbriqué dans le DTO annonce | Confusion entre deux agrégats métier distincts | Deux appels Feign séparés (`ListingApiClient` + `ReviewApiClient`) agrégés dans le contrôleur MVC |

La dégradation progressive sur les avis (`fetchReviewsSafely` avec try/catch sur `FeignException`) illustre un principe de robustesse : une donnée secondaire ne doit jamais empêcher l'affichage de la donnée critique, le plat lui-même.

### 8.6 Tests associés aux réalisations

`BuyerRequestsControllerTest` couvre trois cas via `WebMvcTest` avec mock du client Feign : liste peuplée, liste vide, et redirection login pour un utilisateur non authentifié. Cette approche isole totalement le test de l'infrastructure réseau réelle, cohérente avec la stratégie de test appliquée sur l'ensemble des contrôleurs backend et frontend.

<div style="page-break-before: always;"></div>

## Section 9 — Tests

### 9.1 Stratégie de test à deux niveaux

Le projet sépare strictement tests unitaires (Surefire, `*Test.java`) et tests d'intégration (Failsafe, `*IT.java`), une convention Maven standard adoptée après plusieurs itérations sur une stratégie fragile à base de tags JUnit 5 et `excludedGroups`. Cette séparation par nommage évite les pièges d'héritage de `@Tag` entre classes, un problème rencontré concrètement quand `@Tag("integration")` sur `AbstractIntegrationTest` n'était pas propagé aux sous-classes faute d'annotation `@Inherited`.

| Type de test | Outil | Convention | Déclenchement |
|---|---|---|---|
| Unitaire | Surefire | `*Test.java` | Systématique, sans Docker |
| Intégration | Failsafe | `*IT.java` | Phases pre/post-integration-test, Testcontainers PostgreSQL |

### 9.2 Tests unitaires par couche

Les services applicatifs sont testés exclusivement via Mockito, sans base de données, chaque port secondaire étant mocké (`@Mock`, `@InjectMocks`). Les contrôleurs REST sont testés via `@WebMvcTest`, avec la règle stricte que toute dépendance du contrôleur doit être fournie en `@MockitoBean`, sous peine d'`UnsatisfiedDependencyException` — un piège rencontré concrètement sur `AuthController` et sa dépendance `FindUserUseCase` non mockée.

| Classe de test | Nombre de tests | Couverture |
|---|---|---|
| UserServiceTest | 7 | 100% |
| ListingServiceTest | 15 | 100% |
| CuisineTypeServiceTest | 10 | 100% |
| ContactRequestService | 5 | Cas nominal + doublon 409 |
| SearchListingsService | 5 | Ville inconnue Nominatim vide |
| MinioStorageAdapterTest | 3 | Bucket existant, bucket créé, échec MinIO |

Un piège technique a été résolu sur `MinioStorageAdapterTest` : la signature de `StoragePort.upload` attendait un `byte[]` et non un `InputStream`, corrigé en extrayant une constante `FILE_BYTES` partagée entre les trois scénarios.

### 9.3 Tests d'intégration Testcontainers

`ContactRequestRepositoryIT` valide les règles métier contre une vraie instance PostgreSQL 16 via Testcontainers, isolée du reste par la méta-annotation personnalisée `@IntegrationTest` (`@Inherited`, `@Tag("integration")`). Un test de contexte complet, `TortikiApiApplicationTests`, valide le démarrage Spring intégral (Flyway, Security, JPA, SpringDoc) et n'est exécuté qu'en CI via le profil `-P integration`.

### 9.4 Rapports Allure

Chaque test unitaire porte les annotations `@Epic`, `@Feature`, `@Story`, `@Severity` et `@Description`, générant un rapport HTML détaillé via `allure-maven` dans `target/allure-results`. Cette instrumentation permet de tracer chaque test à une fonctionnalité métier précise, utile pour la soutenance CDA — par exemple `@Story("Upload photo annonce")` avec `@Severity(CRITICAL)` sur le cas nominal de `MinioStorageAdapterTest`.

### 9.5 Seuil JaCoCo 70% et exclusions légitimes

Le seuil `COVEREDRATIO` minimum de 0.70 sur `LINE` s'applique au bundle fusionné (unitaires + intégration via `jacoco-merged.exec`), avec des exclusions justifiées dans le dossier CDA : `domain/model` (POJOs Lombok sans logique), `domain/exception` (pas de branche conditionnelle), `config` (contexte Spring non démarré en test unitaire), et le point d'entrée `TortikiApiApplication.class`. La règle du projet interdit formellement d'abaisser le seuil pour faire passer un build — soit on ajoute des tests, soit on exclut légitimement une classe non testable à ce stade.

| Sprint | Tests | Checkstyle | JaCoCo | Build |
|---|---|---|---|---|
| Sprint 1 | 33 | 0 violation | 100% | SUCCESS |
| Sprint 2 (clôture) | 86 | 0 violation | ≥ 70% (all coverage checks met) | SUCCESS |

### 9.6 Piège technique résolu — itArgLine JaCoCo/Failsafe

Un crash JVM (`Could not open '@{itArgLine}'`) a révélé que la propriété `itArgLine` doit être déclarée vide dans `<properties>` avant d'être enrichie par `jacoco:prepare-agent-integration`, faute de quoi le late-binding échoue et Failsafe reçoit la chaîne littérale non résolue. La correction finale sépare aussi proprement `argLine` (Surefire) et `itArgLine` (Failsafe) pour éviter tout conflit d'agent Mockito entre les deux phases.

<div style="page-break-before: always;"></div>

## Section 10 — Veille technologique et sécurité

### 10.1 Dépréciations API Spring Security 6.4

Les setters `setUserDetailsService()` et `setPasswordEncoder()` de `DaoAuthenticationProvider` ont été signalés dépréciés par SonarQube for IDE, une conséquence directe de l'évolution de l'API Spring Security 6.4 qui privilégie l'injection par constructeur. La correction remplace `new DaoAuthenticationProvider()` suivi des setters par le constructeur paramétré `new DaoAuthenticationProvider(passwordEncoder, userDetailsService)`, conforme à l'API non dépréciée.

| Avertissement détecté | Cause | Résolution appliquée |
|---|---|---|
| `setUserDetailsService` déprécié | API Spring Security 6.4 | Constructeur avec paramètres |
| `DaoAuthenticationProvider` déprécié | Idem | Auto-configuration Spring Boot via beans `UserDetailsService` + `PasswordEncoder` |
| Littéraux `/api/listings/{id}` et `SELLER` dupliqués | Code smell SonarCloud | Extraction en constantes `ROUTE_LISTING_BY_ID`, `ROLE_SELLER` |

Cette veille illustre une règle architecturale plus large actée pour tout le projet : ne jamais déclarer `DaoAuthenticationProvider` manuellement dans une version future si l'auto-configuration Spring Boot suffit, afin d'éviter tout breaking change lors des montées de version.

### 10.2 CVE transitives héritées de spring-boot-starter-parent

L'analyse Mend.io du `pom.xml` du frontend a signalé des alertes CVE sur Tomcat, Spring Security, Thymeleaf, Jackson et Logback. Ces vulnérabilités sont transitives, héritées de `spring-boot-starter-parent:3.5.3`, et ne proviennent pas d'une erreur de configuration du projet — elles relèvent d'un suivi de version amont plutôt que d'une correction immédiate.

La veille recommande de surveiller les release notes Spring Security (versions 6.4.10, 6.5.4, 6.5.8, 6.5.9, 7.0.3, 7.0.4) pour anticiper les correctifs de sécurité disponibles au fil des montées de version du starter parent. Aucune action corrective n'est requise tant que `spring-boot-starter-parent` reste la source de vérité des versions transitives, conformément à la stratégie de gestion centralisée des dépendances Spring Boot.

### 10.3 Migration sessions stateful vers JWT (v2)

Le choix d'authentification par sessions HTTP stateful est documenté comme un point de veille CDA explicite dès la Phase 0 : cette stratégie convient à l'échelle du MVP (99,5% de disponibilité visée, 250 utilisateurs cibles), mais nécessitera une migration vers JWT si Tortiki évolue vers une architecture multi-instances ou une API mobile dédiée exigeant une authentification sans état. Ce report est un choix pédagogique assumé : privilégier la lisibilité et la maîtrise de la complexité initiale plutôt que d'introduire prématurément la gestion de tokens, de refresh, et de révocation.

| Axe de veille | État actuel | Trigger de migration |
|---|---|---|
| Authentification | Sessions stateful, `HttpSessionSecurityContextRepository` | Architecture multi-instances ou API mobile dédiée |
| Scalabilité | Une session active par utilisateur (`maximumSessions(1)`) | Montée en charge horizontale |
| Sécurité transport | BCrypt force 12, CSRF activé sur mutations | Reste valable en v2 JWT |

### 10.4 Avertissements JVM à surveiller

Le démarrage de l'application sur Java 21 a révélé un avertissement JDK concernant l'usage de `sun.misc.Unsafe.allocateMemory` par Netty, une méthode terminellement dépréciée qui sera supprimée dans une version future du JDK. Ce point est à surveiller pour anticiper une éventuelle rupture lors d'une future montée de version de Netty ou du JDK, sans action immédiate requise puisque la dépendance est transitive à Spring WebFlux.
## Section 1 — Compétences mises en œuvre

### Objectif de la section

Cette section liste les 13 compétences du référentiel CDA réparties en 3 blocs, et précise pour chacune comment le projet Tortiki en apporte la preuve concrète. Chaque compétence sera ensuite illustrée dans le corps du dossier par des extraits de code, captures d'écran ou artefacts de gestion de projet.

### Bloc 1 — Développer une application sécurisée

| Compétence | Mise en œuvre dans Tortiki |
|---|---|
| Installer et configurer son environnement de travail | Setup Java 21, Spring Boot 3.5, Docker Compose (PostgreSQL 16, MinIO), profils YAML dev/prod/test |
| Développer des interfaces utilisateur | Maquettes PlantUML puis templates Thymeleaf/Bootstrap 5 pour les 8 écrans (accueil, fiche annonce, dashboard vendeur…) |
| Développer des composants métier | Services applicatifs `UserService`, `ListingService`, `SearchListingsService`, `SubmitReviewService` en couche `application/service` |
| Contribuer à la gestion d'un projet informatique | Découpage en Phase 0 + 4 sprints, suivi GitHub Projects/Issues, Gitflow avec Conventional Commits |

### Bloc 2 — Concevoir et développer une application sécurisée organisée en couches

| Compétence | Mise en œuvre dans Tortiki |
|---|---|
| Analyser les besoins et maquetter une application | Cahier des charges v3.0, personas Sofia/Théo, priorisation MoSCoW, 8 maquettes fonctionnelles |
| Définir l'architecture logicielle d'une application | Architecture Hexagonale stricte imposée : domain / application (port in/out) / infrastructure (adapter in/out) / config |
| Concevoir et mettre en place une base de données relationnelle | Modélisation des entités User, Role, Listing, CuisineType, ContactRequest, Review, Allergen ; migrations Flyway V1 à V6 |
| Développer des composants d'accès aux données SQL et NoSQL | Adaptateurs JPA (`UserRepositoryAdapter`, `ListingRepositoryAdapter`) avec Testcontainers PostgreSQL, `MinioStorageAdapter` pour le stockage objet |

<div style="page-break-inside: avoid;">

### Bloc 3 — Préparer le déploiement d'une application sécurisée

| Compétence | Mise en œuvre dans Tortiki |
|---|---|
| Préparer et exécuter les plans de tests d'une application | 146 tests JUnit 5 (unitaires + intégration), Mockito, rapports Allure, couverture JaCoCo ≥ 70% |
| Préparer et documenter le déploiement d'une application | Pipeline GitHub Actions (4 jobs : Checkstyle, Build/Test, Docker, Notify), documentation OpenAPI/Swagger |
| Contribuer à la mise en production dans une démarche DevOps | CI/CD shift-left, SonarCloud, Docker Compose reproductible, déploiement HTTPS prévu en Sprint 4 |

### Compétence transversale — Sécurité

La sécurité est intégrée à chaque bloc plutôt qu'isolée, conformément à l'esprit "secure by design" du référentiel : Spring Security 6 en sessions stateful, RBAC (ADMIN/SELLER/BUYER), BCrypt force 12, protection CSRF, et conformité RGPD/OWASP Top 10 documentées dès la conception. Cette approche permet de démontrer la compétence sécurité de façon récurrente plutôt qu'en un bloc isolé.

</div>

<div style="page-break-before: always;"></div>

## Section 2 — Cahier des charges et expression des besoins

### 2.1 Présentation du projet

Tortiki est une application web de type marketplace P2P Click & Collect, permettant à des cuisiniers amateurs de vendre des plats faits maison à une clientèle locale. Le projet répond à un vide laissé par les plateformes de livraison traditionnelles, orientées professionnels de la restauration, en proposant un cadre local, accessible et sécurisé, orienté découverte gastronomique.

### 2.2 Contexte et objectif général

L'objectif est de créer un MVP démontrable permettant à un vendeur de publier une annonce, à un client de la rechercher et d'exprimer son intérêt, puis au vendeur de confirmer la demande avant le retrait. Six mois après mise en ligne, les cibles fixées sont 50 vendeurs actifs, 200 acheteurs actifs, une note moyenne de satisfaction de 4,25, un temps de réponse API p95 de 500 ms et une disponibilité mensuelle de 99,5%.

### 2.3 Personas

| Persona | Profil | Besoin principal |
|---|---|---|
| Sofia (Vendeuse) | 38 ans, Nancy, cuisinière ukrainienne | Générer un revenu complémentaire et partager sa culture culinaire |
| Théo (Client) | 26 ans, étudiant, usage smartphone | Trouver rapidement un plat varié et abordable près de lui |

### 2.4 Périmètre fonctionnel MVP

Le MVP couvre l'inscription, la gestion des rôles vendeur/client, le CRUD d'annonces avec photo et créneau de retrait unique, la recherche géographique, le filtrage par origine culinaire et allergènes, l'expression d'intérêt, la confirmation/refus par le vendeur, la notation après retrait, ainsi que l'administration du référentiel des cuisines. Sont explicitement exclus de la v1 : la livraison à domicile, l'application mobile native, la messagerie instantanée, le panier multi-vendeurs, les notifications push et l'authentification JWT (reportée en v2).

<div style="page-break-inside: avoid;">

### 2.5 User stories principales et priorisation MoSCoW

| ID | User Story | Priorité |
|---|---|---|
| US-01 | Inscription visiteur (email, mot de passe fort, validation serveur) | Must |
| US-02 | Choix et cumul du rôle vendeur/client | Must |
| US-03 | Création/modification/suppression d'annonce  | Must |
| US-05 | Gestion des demandes d'intérêt reçues (confirmer/refuser) | Must |
| US-06 | Recherche géographique par ville/code postal | Must |
| US-07 | Filtres origine culinaire et allergènes | Must |
| US-08 | Expression d'intérêt et obtention des coordonnées vendeur | Must |
| US-09 | Notation du vendeur après retrait | Should |
| US-10 | Administration des origines culinaires et modération | Must |

</div>

### 2.6 Priorisation MoSCoW

La priorisation distingue les Must Have (les 10 US du MVP) des Should Have — notation vendeur, email transactionnel, historique des demandes, gestion étendue des utilisateurs. Les fonctionnalités Could Have (livraison, application mobile, JWT, messagerie temps réel) et Won't Have v1 sont clairement exclues du périmètre pour préserver la cohérence pédagogique du projet.

### 2.7 Exigences non fonctionnelles

Le cahier des charges fixe des exigences précises en performance (500 ms p95), disponibilité (99,5%), sécurité (OWASP Top 10, BCrypt, TLS/HTTPS), accessibilité (RGAA 4.1 niveau AA) et RGPD (droit à l'effacement, durées de conservation documentées par entité). La maintenabilité est également exigée via une couverture de tests cible de 70% minimum et une analyse continue SonarCloud sans anomalie bloquante.

### 2.8 Stack technique retenue

| Domaine | Choix |
|---|---|
| Backend | Java 21, Spring Boot 3.5.x, Architecture Hexagonale |
| Frontend | Thymeleaf 3.1, Bootstrap 5, OpenFeign |
| Base de données | PostgreSQL 16, Flyway |
| Sécurité | Spring Security 6, sessions HTTP stateful (JWT différé en v2)  |
| Qualité | JUnit 5, Testcontainers, JaCoCo, SonarCloud, Checkstyle |

<div style="page-break-before: always;"></div>

## Section 4 — Gestion de projet

### 4.1 Planning global et milestones

Le projet Tortiki est découpé en une Phase 0 de cadrage suivie de 4 sprints, avec une deadline finale du MVP fixée au 17 juillet 2026. Le planning global se structure ainsi :

| Milestone | Période | Contenu | Statut |
|---|---|---|---|
| Phase 0 — Cadrage | 13/05 – 24/05/2026 | Socle technique, sécurité, CI/CD (issues 1-10) | Clôturée |
| Sprint 1 — Socle | 25/05 – 07/06/2026 | Domain, application, persistance, auth (issues 11-28) | Clôturée |
| Sprint 2 — Découverte | 08/06 – 21/06/2026 | Recherche géolocalisée, allergènes, contact (issues 29-39) | Clôturée |
| Sprint 3 — Confiance | 22/06 – 05/07/2026 | Notation, admin, dashboard vendeur (issues 40-48) | Clôturée |
| Sprint 4 — Livraison | 06/07 – 17/07/2026 | Déploiement, documentation finale (issues 49-58)  | En cours |

### 4.2 Suivi et outils de collaboration

Le pilotage repose sur deux dépôts GitHub distincts (tortiki-api et tortiki-frontend), avec GitHub Projects pour la gestion des issues, un Gitflow strict (branches feat/fix/test préfixées par sprint, merge sur develop) et des Conventional Commits systématiques. Chaque bloc de travail suit un cycle checkout → développement → tests → commit documenté → validation explicite avant merge.

### 4.3 Environnement humain

| Acteur | Rôle | Responsabilité |
|---|---|---|
| Porteur de projet | Maîtrise d'ouvrage | Définit les besoins, valide les livrables |
| Développeur CDA | Maîtrise d'œuvre | Conçoit, développe, teste et documente l'application |
| Utilisateurs pilotes | Testeurs fonctionnels | Vérifient les parcours métier (personas Sofia/Théo) |
| Jury CDA | Évaluateur | Évalue la conformité au référentiel CDA|

### 4.4 Objectifs de qualité

### Seuils contractuels retenus

La démarche qualité de Tortiki repose sur quatre seuils mesurables, vérifiés automatiquement à chaque commit via le pipeline CI/CD : zéro violation Checkstyle Google Style, couverture JaCoCo ≥ 70%, ensemble des tests JUnit 5 au vert, et analyse SonarCloud sans anomalie bloquante ou critique. Ces contrôles ont été posés dès la Phase 0, avant même l'écriture des premiers tests métier, selon une démarche Shift Left visant à détecter les problèmes le plus tôt possible dans le cycle de développement.

### Suivi chronologique des indicateurs

| Date | Périmètre | Tests JUnit 5 | Couverture JaCoCo | Checkstyle | SonarCloud |
|---|---|---|---|---|---|
| 09/06/2026 | Backend (domain/service) | 33 tests passants | 100% sur domain/service | 0 violation | Configuré, sans blocker |
| 24/06/2026 | Backend (API complète) | 86 tests, 0 échec | Seuil 70% atteint (all coverage checks met) | 0 violation | Aucun blocker/critical |
| 25/06/2026 | Backend (Sprint 3) | 94 tests, 0 échec | Seuil 70% atteint (all coverage checks met) | 0 violation  | Aucun blocker/critical |
| 13/07/2026 | Frontend (9 contrôleurs) | 68 tests, 0 échec | Seuil 70% atteint sur 38 classes  | 0 violation | Aucun blocker/critical |

<div style="page-break-before: always;"></div>

## Section 5 — Spécifications fonctionnelles

### 5.1 Acteurs du système

Tortiki repose sur trois rôles distincts gérés via RBAC dans Spring Security 6, chacun correspondant à un persona ou une fonction métier précise.

| Rôle | Persona | Périmètre fonctionnel |
|---|---|---|
| ROLE_BUYER | Théo, étudiant, usage mobile | Recherche, consultation fiche plat, demande de contact, avis |
| ROLE_SELLER | Sofia, cuisinière ukrainienne | Publication d'annonces, gestion des demandes reçues, tableau de bord |
| ROLE_ADMIN | Gestionnaire plateforme | Modération des annonces, gestion des types de cuisine, panel admin |

### 5.2 User stories principales

Les user stories couvrent le parcours complet Click & Collect, de la recherche géolocalisée jusqu'à l'avis post-transaction. Elles ont été traduites en issues GitHub réparties sur les quatre sprints, avec traçabilité directe vers les composants Spring Boot livrés.

- En tant que Théo, je veux rechercher des plats par ville et type de cuisine pour trouver une annonce proche de moi.
- En tant que Théo, je veux consulter la fiche d'un plat (prix, allergènes, avis) avant de faire une demande.
- En tant que Théo, je veux soumettre une demande de contact avec un nombre de portions et un message optionnel.
- En tant que Sofia, je veux publier une annonce avec photo, prix, portions et créneau de retrait unique.
- En tant que Sofia, je veux consulter mon tableau de bord pour confirmer ou refuser les demandes reçues.
- En tant qu'admin, je veux désactiver une annonce non conforme et gérer le référentiel des types de cuisine.

<div style="page-break-inside: avoid;">

### 5.3 Parcours utilisateur (flux)

Le parcours acheteur suit une séquence linéaire : recherche géolocalisée → fiche annonce → formulaire de contact → suivi des demandes, tandis que le parcours vendeur boucle sur la création d'annonce et la gestion des retours clients. Ce découpage en états (liste, détail, formulaire, confirmation) structure directement les huit écrans maquettés en Phase 0.

| Étape | Écran | Acteur |
|---|---|---|
| 1 | Accueil / Recherche géolocalisée | Théo |
| 2 | Fiche annonce (détail plat, allergènes, avis) | Théo |
| 3 | Formulaire de contact (portions, message) | Théo |
| 4 | Mes demandes (historique statuts) | Théo |
| 5 | Tableau de bord vendeur | Sofia |
| 6 | Créer une annonce | Sofia |
| 7 | Gérer une demande (confirmer/refuser) | Sofia |
| 8 | Inscription / Connexion | Commun |

Toutes ces données proviennent de la répartition des huit écrans validée en Phase 0.

</div>

### 5.4 Modélisation UML

Le diagramme de cas d'utilisation formalise les interactions entre les trois acteurs et le système autour de quatre grands cas : rechercher une annonce, publier une annonce, soumettre une demande de contact, et administrer la plateforme. Ce diagramme s'appuie directement sur les ports primaires de l'architecture hexagonale (`SearchListingsUseCase`, `ManageListingUseCase`, `SubmitContactRequestUseCase`), garantissant une cohérence totale entre la spécification fonctionnelle et l'implémentation Java.

<div style="page-break-before: always;"></div>

## Section 6 — Architecture & Base de données

### 6.1 Architecture Hexagonale retenue

L'architecture hexagonale a été imposée par le tuteur CDA dès la Phase 0, avec une règle fondamentale : les dépendances ne pointent que vers l'intérieur, le domaine ne connaissant jamais Spring ni JPA. Ce choix permet de tester le métier sans base de données, de remplacer PostgreSQL par une autre technologie sans toucher au domaine, et de produire une architecture justifiable devant le jury.

| Couche | Rôle | Exemple |
|---|---|---|
| domain/model | POJOs métier purs, zéro annotation | User.java, Listing.java |
| domain/exception | Exceptions métier | ListingNotFoundException |
| application/port/in | Interfaces cas d'usage | ManageListingUseCase |
| application/port/out | Interfaces Repository/Gateway | ListingRepository, StoragePort |
| application/service | Implémentation des ports in | ListingService |
| infrastructure/adapter/out/persistence | Entités JPA, JpaRepository | ListingEntity, ListingRepositoryAdapter |
| infrastructure/adapter/in/web | Contrôleurs REST, DTOs Records | ListingController |

La séparation stricte entre `domain/model/User.java` (POJO pur) et `infrastructure/adapter/out/persistence/UserEntity.java` (entité JPA annotée) illustre concrètement cette règle : si demain PostgreSQL est remplacé par MongoDB, seule `UserEntity` est modifiée, jamais `User` ni `UserService`.

### 6.2 Modèle Conceptuel et Logique de Données

Le MCD couvre huit entités métier avec leurs cardinalités exactes : User, Role, Listing, CuisineType, Allergen, ContactRequest, Review, ainsi que les tables d'association nécessaires. Deux types énumérés PostgreSQL natifs (`listingstatus`, `contactrequeststatus`) renforcent l'intégrité des données par rapport à un simple champ VARCHAR.

| Entité | Relations principales |
|---|---|
| User | 1,n vers Listing (vendeur), n,n vers Role via userroles |
| Listing | n,1 vers CuisineType, n,n vers Allergen via listingallergens |
| ContactRequest | n,1 vers Listing et User (acheteur), contrainte UNIQUE(listingid, buyerid) |
| Review | 1,1 vers ContactRequest, n,1 vers User (reviewer/seller) et Listing |

Le MLD a été formalisé sous forme de diagramme de classes hexagonal, organisé en cinq packages colorés (domain/model, port/in, port/out, service, adapter), incluant les ports anticipés des sprints suivants comme `ContactRequestRepository` et `StoragePort`.

### 6.3 Scripts Flyway et évolution du schéma

La règle Flyway appliquée sur le projet est absolue : un script déjà exécuté en base ne doit jamais être modifié, toute évolution passe par une nouvelle version. Cette discipline a été respectée dès la première dette technique rencontrée en Sprint 1.

| Version | Contenu | Déclencheur |
|---|---|---|
| V1__init_schema.sql | 7 tables, types ENUM, contraintes CHECK/UNIQUE, index, données initiales (rôles, allergènes, cuisines) | Schéma initial Phase 0 |
| V2__add_user_profile_fields.sql | Ajout phone, avatar_url, city, latitude, longitude sur users | Localisation vendeur manquante pour recherche de proximité |
| V3 | Table reviews et contraintes associées | Ajout du système d'évaluation Sprint 3 |
| V4 | Migrations complémentaires Sprint 3 | Enrichissements ContactRequest |

Le script V1 intègre des index ciblés comme `idx_listings_cuisine_status` (composite cuisine + statut) et `idx_listings_location` (recherche géographique approximative), reflétant une optimisation dès la conception plutôt qu'un ajout tardif.

### 6.4 Décisions techniques justifiées

| Décision | Choix retenu | Justification |
|---|---|---|
| Architecture | Hexagonale | Imposée par le tuteur CDA |
| Sécurité v1 | Sessions stateful | JWT reporté en v2, point de veille CDA documenté |
| Lombok | Version 1.18.38 | Seule version compatible sans erreur avec Java 21 |
| updatePhoto | Reçoit un InputStream | Inversion de dépendance : ListingService ne connaît pas MinIO |
| Latitude/longitude | BigDecimal | Cohérence avec le type NUMERIC(10,7) PostgreSQL  |

<div style="page-break-before: always;"></div>

## Section 7 — Sécurité

### 7.1 Stratégie d'authentification

Le MVP retient des sessions HTTP stateful plutôt qu'un JWT, ce dernier étant volontairement reporté en v2 pour maîtriser la complexité initiale et privilégier la lisibilité pédagogique. Spring Boot auto-configure `DaoAuthenticationProvider` dès que les beans `UserDetailsService` et `PasswordEncoder` sont présents dans le contexte, la déclaration manuelle du provider étant inutile et dépréciée depuis Spring Security 6.4.

| Aspect | Choix retenu | Justification |
|---|---|---|
| Type de session | Stateful, `SessionCreationPolicy.ALWAYS` | Cohérent avec une API consommée par un frontend Thymeleaf/Feign |
| Sessions concurrentes | `maximumSessions(1)`, `maxSessionsPreventsLogin(false)` | Une session active par utilisateur, la nouvelle remplace l'ancienne |
| Encodage mot de passe | BCrypt force 12 | Recommandation OWASP pour le stockage des mots de passe |
| AuthenticationProvider | Non déclaré manuellement | Auto-configuré par Spring Boot depuis 6.4, évite le code déprécié |

### 7.2 Contrôle d'accès par rôle (RBAC)

Le RBAC repose sur trois rôles fixes déclarés en constantes dans `SecurityConfig` : `ROLE_ADMIN`, `ROLE_SELLER`, `ROLE_BUYER`. Les routes sont ordonnées par niveau de criticité croissant, des endpoints publics jusqu'à l'administration.

| Niveau | Exemple de route | Règle |
|---|---|---|
| Public | `GET /api/v1/listings`, `/api/v1/reviews`, Swagger UI | `permitAll()` |
| Authentification | `POST /api/v1/auth/register`, `/login`, `/logout` | `permitAll()` (nécessaire avant connexion) |
| Acheteur | `POST /api/v1/contact-requests`, `POST /api/v1/reviews` | `hasRole(ROLE_BUYER)` |
| Vendeur | `POST/PUT/DELETE /api/v1/listings`, dashboard vendeur | `hasRole(ROLE_SELLER)` |
| Administration | `/api/v1/admin/**`, gestion cuisine-types | `hasRole(ROLE_ADMIN)` |
| Reste | Toute autre route | `anyRequest().authenticated()` |

Les constantes de routes (`SecurityConstants.ROUTE_LISTING_BY_ID`, `ROUTE_REVIEWS`, etc.) éliminent toute duplication de littéraux, une exigence Checkstyle/SonarCloud appliquée dès la Phase 0.

### 7.3 CSRF et RGPD

Le CSRF est désactivé uniquement sur le préfixe `/api/v1` car l'API est consommée exclusivement par des clients HTTP (OpenFeign, curl, HTTP Client) sans formulaires HTML, le vecteur d'attaque CSRF n'existant donc pas côté API pure ; cette décision est documentée en Javadoc et référencée OWASP dans le dossier CDA. Le CSRF reste actif sur les éventuelles routes non-API (formulaires Thymeleaf côté frontend).

Côté RGPD, deux décisions structurantes ont été prises : le DTO `ReviewResponse` n'expose jamais l'email du reviewer, uniquement son prénom (minimisation des données), et le `NominatimGateway` transmet uniquement des coordonnées géographiques à l'API OSM sans aucune donnée personnelle.

| Risque RGPD | Traitement appliqué |
|---|---|
| Exposition email acheteur | Jamais renvoyé dans les DTOs publics (`ReviewResponse`, `ListingCardResponse`) |
| Géolocalisation via Nominatim | Aucune donnée personnelle transmise, uniquement adresse/coordonnées |
| Mot de passe | Stocké exclusivement en hash BCrypt, jamais en clair |

### 7.4 Couverture OWASP Top 10

| Catégorie OWASP | Mesure Tortiki |
|---|---|
| A01 Broken Access Control | RBAC strict par `hasRole`, `@PreAuthorize` sur `submitReview` |
| A02 Cryptographic Failures | BCrypt force 12 sur tous les mots de passe |
| A03 Injection | Requêtes Spring Data JPA paramétrées, aucune requête SQL concatenée |
| A05 Security Misconfiguration | Swagger désactivé en prod, gestion JSON explicite des erreurs 401/403 |
| A07 Identification and Authentication Failures | Sessions limitées à 1 par utilisateur, `UserDetailsServiceImpl` filtre les comptes `enabled=false` |

Les erreurs d'authentification et d'autorisation renvoient des réponses JSON explicites (401 « Connexion requise », 403 « Droits insuffisants ») plutôt que les pages par défaut de Spring Security, une pratique alignée sur les exigences OWASP de gestion d'erreur contrôlée.

### 7.5 Veille et évolution documentée

La dépréciation de `setUserDetailsService` et `DaoAuthenticationProvider` avec Spring Security 6.4 a été traitée comme un exemple concret de veille technique pour la Section 8 du dossier CDA, avec migration vers l'auto-configuration par détection de beans. Le passage au JWT reste un point de veille documenté pour la v2, sans impact sur l'architecture hexagonale actuelle.

<div style="page-break-before: always;"></div>

## Section 8 — Réalisations

### 8.1 Couche Domain (backend)

Tous les POJOs métier sont purs, sans aucune annotation Spring ni JPA : `User`, `Role`, `RoleName`, `Listing`, `CuisineType`, `ListingStatus`, `Allergen`, `ContactRequest`, `ContactRequestStatus`, et `Review` ajouté au Sprint 3. Quinze exceptions métier couvrent l'ensemble des cas d'usage, dont `ReviewAlreadyExistsException`, `ReviewNotAllowedException` et `InvalidStatusTransitionException`.

| Entité | Sprint |
|---|---|
| Listing.java | Phase 0 |
| User.java | Phase 0 |
| ContactRequest.java + ContactRequestStatus.java | Sprint 1 |
| Review.java | Sprint 3 |

### 8.2 Couche Application (ports et services)

Douze ports primaires (in) couvrent tous les cas d'usage, incluant `SubmitReviewUseCase` et `FindReviewsUseCase`, ce dernier séparé du premier par respect strict du principe CQS déjà appliqué sur `ManageListingUseCase.findBySeller`. Dix ports secondaires (out) découplent le domaine des technologies externes, avec `StoragePort` et `GeolocationPort` démontrant une inversion de dépendance aboutie : MinIO et Nominatim restent interchangeables sans toucher au domaine.

| Service | Rôle |
|---|---|
| ListingService.java | Service principal, CRUD + upload photo |
| ContactRequestService.java | Soumission des demandes |
| SubmitReviewService.java | Orchestre 4 règles métier séquentielles (annonce existe, acheteur actif, demande confirmée, pas de doublon)|
| SearchListingsService.java | Moteur de recherche géolocalisé via Nominatim |

### 8.3 Couche Infrastructure (adapters backend)

Côté persistence, tous les adapters JPA sont livrés avec `AbstractIntegrationTest` (Testcontainers), `UserDetailsServiceImpl`, et un correctif `JOIN FETCH` sur `ListingJpaRepository` pour éliminer une `LazyInitializationException`. Côté web, six contrôleurs REST exposent l'API avec DTOs Records Java 21 : `AuthController`, `ListingController`, `CuisineTypeController`, `SearchListingController`, `ContactRequestController`, `SellerDashboardController`.

| Adapter | Détail |
|---|---|
| MinioStorageAdapter | Upload photo avec slug UUID |
| NominatimGateway | WebClient, conforme RGPD, aucune donnée personnelle transmise  |
| ReviewController | `GET /api/v1/reviews?listingId=` public, DTO enrichi de `reviewerFirstName` (jamais l'email) |

### 8.4 Réalisations frontend (tortiki-frontend)

Le frontend Thymeleaf compte dix contrôleurs MVC (jamais `@RestController`), dont `SellerListingController` reflétant la centralité de la gestion d'annonces. L'architecture applique strictement SOLID : SRP par contexte fonctionnel (Search/Listing/Dashboard), ISP via des clients Feign granulaires plutôt qu'un god-object, et DIP puisque les contrôleurs dépendent des interfaces Feign, jamais d'implémentations HTTP.

| Template | Statut | Rôle |
|---|---|---|
| fragments/layout.html | Confirmé | Head, navbar, footer mutualisés, Bootstrap 5 via WebJars  |
| home.html | Confirmé | Hero, recherche par ville, cuisines du monde |
| search-results.html | Confirmé | Grille de résultats, filtres, pagination |
| dashboard.html | Confirmé | Demandes reçues, actions Confirmer/Refuser |
| listing-detail.html | Confirmé | Fiche plat, avis, formulaire de contact inline |
| buyer-requests.html | Confirmé | Historique des demandes acheteur |

### 8.5 Écart API/frontend résolu — Reviews sur listing-detail

Trois écarts distincts ont été identifiés puis corrigés entre la livraison initiale du template et le contrat API réel, un cas d'école de traçabilité pour le dossier CDA.

| Écart détecté | Cause | Correction appliquée |
|---|---|---|
| `GET /api/v1/reviews` absent | Seul le POST existait initialement | Nouveau port `FindReviewsUseCase`, service, contrôleur public sans authentification |
| `ReviewResponse` sans identité auteur | DTO pensé uniquement pour la soumission | Ajout de `reviewerFirstName`, jamais l'email (minimisation RGPD) |
| `listing.reviews` imbriqué dans le DTO annonce | Confusion entre deux agrégats métier distincts | Deux appels Feign séparés (`ListingApiClient` + `ReviewApiClient`) agrégés dans le contrôleur MVC |

La dégradation progressive sur les avis (`fetchReviewsSafely` avec try/catch sur `FeignException`) illustre un principe de robustesse : une donnée secondaire ne doit jamais empêcher l'affichage de la donnée critique, le plat lui-même.

### 8.6 Tests associés aux réalisations

`BuyerRequestsControllerTest` couvre trois cas via `WebMvcTest` avec mock du client Feign : liste peuplée, liste vide, et redirection login pour un utilisateur non authentifié. Cette approche isole totalement le test de l'infrastructure réseau réelle, cohérente avec la stratégie de test appliquée sur l'ensemble des contrôleurs backend et frontend.

<div style="page-break-before: always;"></div>

## Section 9 — Tests

### 9.1 Stratégie de test à deux niveaux

Le projet sépare strictement tests unitaires (Surefire, `*Test.java`) et tests d'intégration (Failsafe, `*IT.java`), une convention Maven standard adoptée après plusieurs itérations sur une stratégie fragile à base de tags JUnit 5 et `excludedGroups`. Cette séparation par nommage évite les pièges d'héritage de `@Tag` entre classes, un problème rencontré concrètement quand `@Tag("integration")` sur `AbstractIntegrationTest` n'était pas propagé aux sous-classes faute d'annotation `@Inherited`.

| Type de test | Outil | Convention | Déclenchement |
|---|---|---|---|
| Unitaire | Surefire | `*Test.java` | Systématique, sans Docker |
| Intégration | Failsafe | `*IT.java` | Phases pre/post-integration-test, Testcontainers PostgreSQL |

### 9.2 Tests unitaires par couche

Les services applicatifs sont testés exclusivement via Mockito, sans base de données, chaque port secondaire étant mocké (`@Mock`, `@InjectMocks`). Les contrôleurs REST sont testés via `@WebMvcTest`, avec la règle stricte que toute dépendance du contrôleur doit être fournie en `@MockitoBean`, sous peine d'`UnsatisfiedDependencyException` — un piège rencontré concrètement sur `AuthController` et sa dépendance `FindUserUseCase` non mockée.

| Classe de test | Nombre de tests | Couverture |
|---|---|---|
| UserServiceTest | 7 | 100% |
| ListingServiceTest | 15 | 100% |
| CuisineTypeServiceTest | 10 | 100% |
| ContactRequestService | 5 | Cas nominal + doublon 409 |
| SearchListingsService | 5 | Ville inconnue Nominatim vide |
| MinioStorageAdapterTest | 3 | Bucket existant, bucket créé, échec MinIO |

Un piège technique a été résolu sur `MinioStorageAdapterTest` : la signature de `StoragePort.upload` attendait un `byte[]` et non un `InputStream`, corrigé en extrayant une constante `FILE_BYTES` partagée entre les trois scénarios.

### 9.3 Tests d'intégration Testcontainers

`ContactRequestRepositoryIT` valide les règles métier contre une vraie instance PostgreSQL 16 via Testcontainers, isolée du reste par la méta-annotation personnalisée `@IntegrationTest` (`@Inherited`, `@Tag("integration")`). Un test de contexte complet, `TortikiApiApplicationTests`, valide le démarrage Spring intégral (Flyway, Security, JPA, SpringDoc) et n'est exécuté qu'en CI via le profil `-P integration`.

### 9.4 Rapports Allure

Chaque test unitaire porte les annotations `@Epic`, `@Feature`, `@Story`, `@Severity` et `@Description`, générant un rapport HTML détaillé via `allure-maven` dans `target/allure-results`. Cette instrumentation permet de tracer chaque test à une fonctionnalité métier précise, utile pour la soutenance CDA — par exemple `@Story("Upload photo annonce")` avec `@Severity(CRITICAL)` sur le cas nominal de `MinioStorageAdapterTest`.

### 9.5 Seuil JaCoCo 70% et exclusions légitimes

Le seuil `COVEREDRATIO` minimum de 0.70 sur `LINE` s'applique au bundle fusionné (unitaires + intégration via `jacoco-merged.exec`), avec des exclusions justifiées dans le dossier CDA : `domain/model` (POJOs Lombok sans logique), `domain/exception` (pas de branche conditionnelle), `config` (contexte Spring non démarré en test unitaire), et le point d'entrée `TortikiApiApplication.class`. La règle du projet interdit formellement d'abaisser le seuil pour faire passer un build — soit on ajoute des tests, soit on exclut légitimement une classe non testable à ce stade.

| Sprint | Tests | Checkstyle | JaCoCo | Build |
|---|---|---|---|---|
| Sprint 1 | 33 | 0 violation | 100% | SUCCESS |
| Sprint 2 (clôture) | 86 | 0 violation | ≥ 70% (all coverage checks met) | SUCCESS |

### 9.6 Piège technique résolu — itArgLine JaCoCo/Failsafe

Un crash JVM (`Could not open '@{itArgLine}'`) a révélé que la propriété `itArgLine` doit être déclarée vide dans `<properties>` avant d'être enrichie par `jacoco:prepare-agent-integration`, faute de quoi le late-binding échoue et Failsafe reçoit la chaîne littérale non résolue. La correction finale sépare aussi proprement `argLine` (Surefire) et `itArgLine` (Failsafe) pour éviter tout conflit d'agent Mockito entre les deux phases.

<div style="page-break-before: always;"></div>

## Section 10 — Veille technologique et sécurité

### 10.1 Dépréciations API Spring Security 6.4

Les setters `setUserDetailsService()` et `setPasswordEncoder()` de `DaoAuthenticationProvider` ont été signalés dépréciés par SonarQube for IDE, une conséquence directe de l'évolution de l'API Spring Security 6.4 qui privilégie l'injection par constructeur. La correction remplace `new DaoAuthenticationProvider()` suivi des setters par le constructeur paramétré `new DaoAuthenticationProvider(passwordEncoder, userDetailsService)`, conforme à l'API non dépréciée.

| Avertissement détecté | Cause | Résolution appliquée |
|---|---|---|
| `setUserDetailsService` déprécié | API Spring Security 6.4 | Constructeur avec paramètres |
| `DaoAuthenticationProvider` déprécié | Idem | Auto-configuration Spring Boot via beans `UserDetailsService` + `PasswordEncoder` |
| Littéraux `/api/listings/{id}` et `SELLER` dupliqués | Code smell SonarCloud | Extraction en constantes `ROUTE_LISTING_BY_ID`, `ROLE_SELLER` |

Cette veille illustre une règle architecturale plus large actée pour tout le projet : ne jamais déclarer `DaoAuthenticationProvider` manuellement dans une version future si l'auto-configuration Spring Boot suffit, afin d'éviter tout breaking change lors des montées de version.

### 10.2 CVE transitives héritées de spring-boot-starter-parent

L'analyse Mend.io du `pom.xml` du frontend a signalé des alertes CVE sur Tomcat, Spring Security, Thymeleaf, Jackson et Logback. Ces vulnérabilités sont transitives, héritées de `spring-boot-starter-parent:3.5.3`, et ne proviennent pas d'une erreur de configuration du projet — elles relèvent d'un suivi de version amont plutôt que d'une correction immédiate.

La veille recommande de surveiller les release notes Spring Security (versions 6.4.10, 6.5.4, 6.5.8, 6.5.9, 7.0.3, 7.0.4) pour anticiper les correctifs de sécurité disponibles au fil des montées de version du starter parent. Aucune action corrective n'est requise tant que `spring-boot-starter-parent` reste la source de vérité des versions transitives, conformément à la stratégie de gestion centralisée des dépendances Spring Boot.

### 10.3 Migration sessions stateful vers JWT (v2)

Le choix d'authentification par sessions HTTP stateful est documenté comme un point de veille CDA explicite dès la Phase 0 : cette stratégie convient à l'échelle du MVP (99,5% de disponibilité visée, 250 utilisateurs cibles), mais nécessitera une migration vers JWT si Tortiki évolue vers une architecture multi-instances ou une API mobile dédiée exigeant une authentification sans état. Ce report est un choix pédagogique assumé : privilégier la lisibilité et la maîtrise de la complexité initiale plutôt que d'introduire prématurément la gestion de tokens, de refresh, et de révocation.

| Axe de veille | État actuel | Trigger de migration |
|---|---|---|
| Authentification | Sessions stateful, `HttpSessionSecurityContextRepository` | Architecture multi-instances ou API mobile dédiée |
| Scalabilité | Une session active par utilisateur (`maximumSessions(1)`) | Montée en charge horizontale |
| Sécurité transport | BCrypt force 12, CSRF activé sur mutations | Reste valable en v2 JWT |

### 10.4 Avertissements JVM à surveiller

Le démarrage de l'application sur Java 21 a révélé un avertissement JDK concernant l'usage de `sun.misc.Unsafe.allocateMemory` par Netty, une méthode terminellement dépréciée qui sera supprimée dans une version future du JDK. Ce point est à surveiller pour anticiper une éventuelle rupture lors d'une future montée de version de Netty ou du JDK, sans action immédiate requise puisque la dépendance est transitive à Spring WebFlux.