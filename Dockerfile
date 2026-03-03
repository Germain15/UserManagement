# --- STAGE 1: Builder ---
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Optimisation du cache : on copie le pom d'abord
COPY pom.xml .
RUN mvn dependency:go-offline

# Copie du code et compilation
COPY src ./src
RUN mvn clean package -DskipTests

# --- STAGE 2: Runner ---
FROM eclipse-temurin:21-jre-alpine AS runner
# Nettoyage pour réduire la surface d'attaque
RUN apk upgrade --no-cache && \
    apk add --no-cache wget
    
WORKDIR /app

# Sécurité : Création d'un utilisateur non-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Récupération du JAR depuis le builder
COPY --from=builder /app/target/*.jar app.jar

# Permissions pour l'utilisateur non-root
RUN chown appuser:appgroup app.jar

# On passe sur l'utilisateur dédié
USER appuser

EXPOSE 8080

# Utilisation de variables d'env pour la flexibilité
ENTRYPOINT ["java", "-XX:+UseParallelGC", "-Xmx512m", "-jar", "app.jar"]