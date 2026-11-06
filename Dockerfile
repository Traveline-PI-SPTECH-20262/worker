
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/worker-1.0-SNAPSHOT.jar app.jar
COPY "01. a 03. Chegadas de turistas internacionais ao Brasil__A2022-2021-V49-1ed.xlsx" ./

CMD ["java", "-jar", "app.jar"]
