FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn clean verify -DskipTests

FROM maven:3.9.4-eclipse-temurin-21
WORKDIR /app
COPY --from=build /app /app
EXPOSE 8080
ENTRYPOINT ["mvn", "exec:java", "-Denvironment=prod"]