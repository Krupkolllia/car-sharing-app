FROM maven:3.9.16-eclipse-temurin-17-alpine AS build

WORKDIR /application

COPY pom.xml .

RUN mvn -B dependency:go-offline

COPY src ./src

RUN mvn -B clean package -DskipTests


FROM eclipse-temurin:17-jre-alpine

WORKDIR /application

RUN addgroup -S app \
    && adduser -S app -G app

COPY --from=build /application/target/*.jar application.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]