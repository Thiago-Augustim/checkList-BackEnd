# Etapa 1: Build da aplicação com Maven e Java 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagem final leve com JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENV PORT=8080
ENV SPRING_DOCKER_COMPOSE_ENABLED=false
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]
