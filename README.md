# HériConsent — Backend Spring Boot

Plateforme de gestion du **consentement des héritiers** sur plusieurs générations.

---

## Stack technique

| Composant       | Technologie              |
|-----------------|--------------------------|
| Backend         | Spring Boot 3.2 (Java 21)|
| Base de données | PostgreSQL 16            |
| Migrations      | Flyway                   |
| Auth            | JWT (jjwt 0.12)          |
| Stockage docs   | MinIO (S3 compatible)    |
| Emails          | Spring Mail + MailHog    |
| Documentation   | Swagger / OpenAPI 3      |
| Conteneurs      | Docker + Docker Compose  |

---

## Lancer le projet en local

### Prérequis
- Docker & Docker Compose
- Java 21 (optionnel, pour dev sans Docker)

### Démarrage rapide

```bash
# 1. Cloner le projet
git clone https://github.com/votre-org/hericonsent-backend.git
cd hericonsent-backend

# 2. Lancer tous les services
docker-compose up --build

# 3. L'API est disponible sur :
#    http://localhost:8080/api
#    Swagger UI : http://localhost:8080/api/swagger-ui.html
#    MailHog    : http://localhost:8025
#    MinIO      : http://localhost:9001  (admin/minioadmin)
```

### Développement sans Docker

```bash
# Lancer seulement la base + minio
docker-compose up db minio mailhog

# Lancer Spring Boot
./mvnw spring-boot:run
```

---

## Structure du projet

```
src/main/java/com/hericonsent/
├── HeriConsentApplication.java
├── config/
│   ├── SecurityConfig.java      # JWT, CORS, RBAC
│   └── OpenApiConfig.java       # Swagger
├── controller/
│   └── Controllers.java         # AuthController, DossierController,
│                                #   HeritierController, ConsentementController
├── dto/
│   └── Dtos.java                # Tous les DTOs (Request/Response)
├── entity/                      # Entités JPA
│   ├── User.java
│   ├── Personne.java
│   ├── Dossier.java
│   ├── Heritier.java
│   ├── Consentement.java
│   ├── ConsentementReponse.java
│   ├── Document.java
│   └── AuditLog.java
├── exception/
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java
├── repository/
│   └── Repositories.java        # Tous les repositories JPA
├── security/
│   ├── JwtService.java
│   └── JwtAuthFilter.java
└── service/
    ├── AuthService.java
    ├── AuditService.java
    ├── ConsentementService.java
    ├── DossierService.java
    ├── HeritierService.java
    └── NotificationService.java

src/main/resources/
├── application.yml
└── db/migration/
    └── V1__init_schema.sql      # Schéma complet PostgreSQL
```

---

## API Endpoints

### Authentification
| Méthode | Route             | Description            |
|---------|-------------------|------------------------|
| POST    | `/auth/register`  | Créer un compte        |
| POST    | `/auth/login`     | Se connecter (JWT)     |
| POST    | `/auth/refresh`   | Rafraîchir le token    |

### Dossiers
| Méthode | Route                      | Rôle requis      |
|---------|----------------------------|------------------|
| GET     | `/dossiers`                | Tous             |
| POST    | `/dossiers`                | Tous             |
| GET     | `/dossiers/{id}`           | Tous             |
| PATCH   | `/dossiers/{id}/statut`    | NOTAIRE / ADMIN  |

### Héritiers
| Méthode | Route                                       | Rôle requis     |
|---------|---------------------------------------------|-----------------|
| GET     | `/dossiers/{id}/heritiers`                  | Tous            |
| POST    | `/dossiers/{id}/heritiers`                  | Tous            |
| DELETE  | `/dossiers/{id}/heritiers/{heritierId}`     | NOTAIRE / ADMIN |
| PATCH   | `/dossiers/{id}/heritiers/{heritierId}/statut-contact` | Tous |

### Consentements
| Méthode | Route                                        | Rôle requis      |
|---------|----------------------------------------------|------------------|
| POST    | `/dossiers/{id}/consentements`               | NOTAIRE / ADMIN  |
| GET     | `/dossiers/{id}/consentements`               | Tous             |
| GET     | `/consentements/{id}`                        | Tous             |
| POST    | `/consentements/{id}/repondre`               | HERITIER (connecté) |
| POST    | `/consentements/{id}/relancer`               | NOTAIRE / ADMIN  |
| POST    | `/consentements/repondre/token/{token}`      | **Public** (lien email) |

---

## Workflow de consentement

```
Notaire crée consentement
         ↓
Tokens générés pour chaque héritier
         ↓
Emails envoyés avec lien sécurisé
         ↓
Héritier clique le lien → répond ACCEPTE/REJETE
         ↓
Statut recalculé automatiquement :
  - Toutes parts ≥ seuil = VALIDE ✅
  - Au moins 1 REJETE       = REJETE ❌
  - Partiellement répondu   = PARTIEL 🔄
         ↓
Si VALIDE → notification à tous les héritiers
```

---

## Rôles utilisateurs

| Rôle          | Droits                                      |
|---------------|---------------------------------------------|
| `ROLE_ADMIN`  | Accès total                                 |
| `ROLE_NOTAIRE`| Créer dossiers, consentements, relancer     |
| `ROLE_HEIR`   | Voir ses dossiers, répondre aux consentements |

---

## Comptes de test

| Email                    | Mot de passe | Rôle     |
|--------------------------|--------------|----------|
| admin@hericonsent.fr     | admin123     | ADMIN    |
| notaire@hericonsent.fr   | admin123     | NOTAIRE  |

---

## Variables d'environnement

| Variable          | Défaut                    | Description          |
|-------------------|---------------------------|----------------------|
| `DB_HOST`         | localhost                 | Hôte PostgreSQL      |
| `DB_NAME`         | hericonsent               | Nom de la base       |
| `DB_USER`         | postgres                  | Utilisateur BDD      |
| `DB_PASSWORD`     | postgres                  | Mot de passe BDD     |
| `JWT_SECRET`      | (voir config)             | Clé secrète JWT      |
| `MINIO_ENDPOINT`  | http://localhost:9000     | URL MinIO            |
| `MAIL_HOST`       | localhost                 | Serveur SMTP         |
| `CORS_ORIGINS`    | http://localhost:4200     | Origines Angular     |

---

## Prochaines étapes (Frontend Angular)

Le frontend Angular viendra se connecter à cette API.  
Modules prévus :
- `auth/` — login, register, guards JWT
- `dossier/` — liste, détail, création
- `heritier/` — gestion des héritiers
- `consentement/` — vote, suivi, signature
- `notaire/` — dashboard, relances

---

*HériConsent — Débloquez votre héritage.*
