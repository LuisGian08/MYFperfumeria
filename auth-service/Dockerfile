FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY gateway-service/pom.xml gateway-service/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY product-service/pom.xml product-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY sales-service/pom.xml sales-service/pom.xml
COPY alert-service/pom.xml alert-service/pom.xml
COPY report-service/pom.xml report-service/pom.xml
COPY auth-service/src auth-service/src
RUN mvn -q -pl auth-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
