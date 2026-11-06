# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Baixa as dependências antes de copiar o código
COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline

# Copia o código depois
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/worker-jar-with-dependencies.jar app.jar

COPY ["01. a 03. Chegadas de turistas internacionais ao Brasil__A2022-2021-V49-1ed.xlsx", "./"]

CMD ["java", "-jar", "app.jar"]