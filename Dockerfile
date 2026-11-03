FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests && mvn -q dependency:copy-dependencies -DoutputDirectory=target/libs

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/worker-1.0-SNAPSHOT.jar app.jar
COPY --from=build /build/target/libs ./libs
COPY ["01. a 03. Chegadas de turistas internacionais ao Brasil__A2022-2021-V49-1ed.xlsx", "./"]
CMD ["java", "-cp", "app.jar:libs/*", "school.sptech.Main"]
