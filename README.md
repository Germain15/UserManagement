# User Management API

API REST de gestion d'utilisateurs · Spring Boot 3.5.11 · Java 21 · PostgreSQL 16 · Docker · GitHub Actions

---

## Table des matières
- [User Management API](#user-management-api)
  - [Table des matières](#table-des-matières)
  - [Présentation du Projet](#présentation-du-projet)
  - [Lancer le projet](#lancer-le-projet)
    - [Avec Docker (recommandé)](#avec-docker-recommandé)
    - [Depuis Docker Hub](#depuis-docker-hub)
    - [En local sans Docker](#en-local-sans-docker)
    - [POST /users](#post-users)
    - [GET /users](#get-users)
  - [Tests](#tests)
  - [Architecture](#architecture)
  - [Docker](#docker)
  - [Pipeline CI/CD](#pipeline-cicd)
  - [10. Versionnement des images](#10-versionnement-des-images)
    - [Créer une nouvelle version](#créer-une-nouvelle-version)
    - [Rollback](#rollback)
  - [Variables d'environnement](#variables-denvironnement)
  - [Questions Techniques](#questions-techniques)
    - [Q1 — Image Docker vs Container](#q1--image-docker-vs-container)
    - [Q2 — CMD vs ENTRYPOINT](#q2--cmd-vs-entrypoint)
    - [Q3 — Sécuriser un pipeline CI/CD](#q3--sécuriser-un-pipeline-cicd)
    - [Q4 — Gérer dev / staging / production](#q4--gérer-dev--staging--production)
    - [Q5 — Container en boucle de redémarrage](#q5--container-en-boucle-de-redémarrage)
  - [Ce que j'aurais fait avec plus de temps](#ce-que-jaurais-fait-avec-plus-de-temps)

---
## Présentation du Projet
Ce projet consiste en une API REST de gestion d'utilisateurs développée avec Spring Boot 3. L'objectif principal est de démontrer la mise en place d'une infrastructure moderne incluant la conteneurisation, l'automatisation des tests et un déploiement continu (CI/CD) vers Docker Hub.
## Lancer le projet

### Avec Docker (recommandé)

```bash
git clone https://github.com/Germain15/UserManagement.git
cd userManagement
cp .env.example .env
docker compose up --build
```

Vérifier que les deux services sont up :

```bash
docker compose ps
# user-management-db   running (healthy)
# user-management-api  running (healthy)
```

### Depuis Docker Hub

L'image est disponible sur `germain2004/user-management-api`.  
Copier le `docker-compose.yml` depuis la description de l'image, puis :

```bash
docker compose up -d
```

### En local sans Docker

```bash
git clone https://github.com/Germain15/UserManagement.git
cd userManagement
# Démarrer uniquement la base
crée et configuré le fichier .env en se basant sur le .env.example

# Lancer l'API Spring Boot
>mvn spring-boot

---

## Endpoints

### GET /health

```bash
curl http://localhost:8080/health
```

```json
{
  "status": "ok",
  "timestamp": "2026-03-02T09:45:00.000Z",
  "version": "1.0.0",
  "db_status": "connected"
}
```

Retourne `503` avec `"status": "degraded"` si la base est inaccessible.

---

### POST /users

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"ZOUNON","email":"zounongermain@gmail.com"}'
```

| Cas | Code retour |
|---|---|
| Données valides | 201 Created |
| Champ manquant ou email invalide | 400 Bad Request |
| Email déjà utilisé | 409 Conflict |

Réponse 201 :
```json
{ "id": 1, "name": "ZOUNON", "email": "zounongermain@gmail.com", "createdAt": "..." }
```

---

### GET /users

```bash
curl http://localhost:8080/users
```

Retourne la liste des utilisateurs ou `[]` si vide — ce n'est pas une erreur.

---

## Tests

```bash
mvn test
```

Les tests utilisent H2 en mémoire — aucune base externe nécessaire.  
Scénarios couverts : 201, 400 (body vide), 400 (email invalide), 409 (doublon), 200 liste vide, 200 health.

---

## Architecture

```
controller/   →  reçoit HTTP, retourne le bon code
service/      →  logique métier (vérification doublons, création)
repository/   →  accès JPA
model/        →  entité User
dto/          →  CreateUserRequest, UserResponse, ApiError
exception/    →  GlobalExceptionHandler (@RestControllerAdvice)
config/       →  DatabaseHealthIndicator
```

L'entité JPA n'est jamais exposée directement — les réponses passent toujours par `UserResponse`.

---

## Docker

**Dockerfile multi-stage** : stage builder (`maven:3.9-eclipse-temurin-21-alpine`) qui compile et produit le JAR, stage runner (`eclipse-temurin:21-jre-alpine`) qui exécute avec un utilisateur non-root. L'image finale fait ~220MB.

**docker-compose** : volume nommé `postgres_data` pour la persistance, réseau custom `user-net`,nom du contenaire `user-management-api, healthcheck `pg_isready` sur la base, `depends_on: condition: service_healthy` pour que l'API attende que PostgreSQL soit vraiment prêt.

---

## Pipeline CI/CD

```
push → [tests] → [build-and-push] → [deploy-staging]     sur main
                                 → [deploy-production]   sur tag v*.*.*
```

- Les tests tournent avec un vrai PostgreSQL (service GitHub Actions)
- Si un test échoue, le pipeline s'arrête — on ne publie jamais d'image cassée
- Le push vers Docker Hub est conditionné au succès des tests
- Staging se déclenche automatiquement sur `main`, production uniquement sur tag

Secrets à configurer : `DOCKER_USERNAME` et `DOCKER_PASSWORD` dans `Settings → Secrets → Actions`.

## 10. Versionnement des images

### Créer une nouvelle version

```bash
git tag v1.0.0
git push origin v1.0.0
```

Le pipeline détecte le tag et publie automatiquement **3 tags** sur Docker Hub :

| Tag | Exemple | Quand |
|---|---|---|
| SHA court | `sha-abc1234` | Chaque commit traçabilité exacte |
| `latest` | `latest` | Push sur `main` uniquement |
| Version sémantique | `1.0.0` | Tag Git `v1.0.0` |

### Rollback

```bash
# Modifier l'image vers une version précédente en présisant la version 1.0.1
# Puis relancer le compose
docker compose up -d
```

---

## Variables d'environnement

```bash
cp .env.example .env
```

| Variable | Exemple |
|---|---|
| `API_PORT` | `8080` |
| `DB_HOST` | `db` |
| `DB_PORT` | `5432` |
| `DB_NAME` | `users_db` |
| `DB_USER` | `appuser` |
| `DB_PASSWORD` | à changer en prod |
| `HIBERNATE_DDL_AUTO` | `update` |

`.env` est dans `.gitignore` et ne sera jamais commité.

---

## Questions Techniques

### Q1 — Image Docker vs Container

Une image est un artefact immuable — une pile de couches en lecture seule construites par le Dockerfile. Elle ne tourne pas, elle ne change pas.

Un container est une instance en cours d'exécution de cette image. Docker lui ajoute une couche d'écriture éphémère : quand on supprime le container, cette couche disparaît. C'est pourquoi les données PostgreSQL doivent vivre dans un volume nommé, pas dans le container lui-même.

```bash
docker images                    # liste les images locales
docker run -d postgres:16-alpine # crée un container
docker ps                        # containers actifs
docker ps -a                     # tous les containers
docker rm <id>                   # supprime le container (couche éphémère perdue)
```

---

### Q2 — CMD vs ENTRYPOINT

ENTRYPOINT définit l'exécutable principal — il n'est pas remplacé par ce qu'on passe à `docker run`. CMD définit les arguments par défaut — eux peuvent être remplacés.

```dockerfile
ENTRYPOINT ["java"]
CMD ["-jar", "app.jar"]
```

```bash
docker run myapp                       # → java -jar app.jar
docker run myapp -Xmx256m -jar app.jar # → java -Xmx256m -jar app.jar (CMD remplacé)
docker run --entrypoint sh myapp       # → sh (debug)
```

La forme exec (`["java", "-jar"]`) est obligatoire en production : le process est PID 1 et reçoit les signaux Unix directement. `docker stop` envoie SIGTERM, Spring Boot l'intercepte et s'arrête proprement. Avec la forme shell (`java -jar`), c'est `/bin/sh` qui est PID 1 — les signaux ne remontent pas à Java, et l'arrêt finit en SIGKILL brutal.

---

### Q3 — Sécuriser un pipeline CI/CD

**Secrets dans les variables CI, jamais dans le YAML.**
```yaml
password: ${{ secrets.DOCKER_PASSWORD }}
```
Les valeurs sont masquées dans les logs et inaccessibles aux forks.

**Tokens à périmètre minimal.** Un Access Token Docker Hub limité au seul repository concerné, pas le mot de passe principal.

**Scan de vulnérabilités avec Trivy.**
```yaml
uses: aquasecurity/trivy-action@master
with:
  severity: 'CRITICAL,HIGH'
  exit-code: '1'
```
Le push est bloqué si une CVE critique est détectée.

**Audit des dépendances.**
```bash
mvn org.owasp:dependency-check-maven:check
```

**Images de base épinglées par digest.**
```dockerfile
FROM eclipse-temurin:21-jre-alpine@sha256:abc123...
```
Un tag flottant peut pointer vers une image différente domaine. Le digest garantit la reproductibilité.

**Approbation manuelle avant la production.** En configurant un environnement GitHub sur le job `deploy-production`, un bouton d'approbation s'affiche avant l'exécution.

---

### Q4 — Gérer dev / staging / production

La stratégie est simple : `feature/*` → `main` → tag.

Les features partent de `main`, sont mergées via PR après validation des tests. Chaque merge sur `main` déclenche un déploiement automatique en staging. Un tag `v1.0.0` déclenche la production.

Les configurations par profil Spring Boot :
```
application.yml        # commun
application-dev.yml    # H2, show-sql: true
application-test.yml   # H2 mémoire, create-drop
application-prod.yml   # PostgreSQL, show-sql: false, ddl-auto: validate
```

La même image Docker tourne partout — seules les variables d'environnement changent entre les contextes. C'est ce qui garantit qu'on ne découvre pas de surprise en production que le dev n'a pas vue.

---

### Q5 — Container en boucle de redémarrage

```bash
docker compose ps                          # identifier le container
docker compose logs api --tail=100         # logs de la dernière tentative
docker logs <id> --previous --tail=50      # logs des tentatives précédentes
docker inspect <id> --format='{{.State.ExitCode}}'
docker events --filter container=<id> --since 30m
```

Exit codes courants :

| Code | Cause |
|---|---|
| 1 | Erreur applicative (variable manquante, DB inaccessible) |
| 137 | OOM — mémoire épuisée (SIGKILL) |
| 143 | Arrêt demandé par Docker (SIGTERM) |

Trois causes que j'ai rencontrées :

**Variable manquante.** Logs : `Could not resolve placeholder 'DB_PASSWORD'`. Vérifier avec `docker inspect` que la variable apparaît bien dans `Env`. Fix : s'assurer que `env_file` pointe vers un `.env` complet.

**Base pas encore prête.** Logs : `Connection refused` vers le port 5432. Fix : `depends_on: condition: service_healthy` avec un vrai healthcheck `pg_isready` — c'est pour ça que cette configuration est en place dans ce projet.

**Out of Memory.** Exit code 137, événement `oom` dans `docker events`. Fix : ajuster `-Xmx` et ajouter des limites mémoire dans le compose :
```yaml
deploy:
  resources:
    limits:
      memory: 512m
environment:
  JAVA_OPTS: "-Xmx400m"
```

---

## Ce que j'aurais fait avec plus de temps

- Authentification JWT — la structure est prête, il manque le filtre
- Scan Trivy dans le pipeline avec rapport en artefact
- Migrations Flyway en remplacement de `ddl-auto: update`
- Déploiement réel sur VPS avec HTTPS (la structure SSH est déjà dans le pipeline)
