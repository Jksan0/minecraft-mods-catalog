FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN mvn -B dependency:go-offline

COPY src src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
