# ============================================
# Étape 1 : Build avec Maven
# ============================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# ============================================
# Étape 2 : Image finale légère
# ============================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Créer utilisateur non-root
RUN addgroup -S hericonsent && adduser -S hericonsent -G hericonsent
USER hericonsent

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
