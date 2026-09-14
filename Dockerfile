# every module gets built once in the first stage, then each service image only carries
# its own jar. pick one with --target, the compose file does that already

FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
RUN ./mvnw -q -B package -DskipTests

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
RUN useradd --system --create-home app
USER app

FROM runtime AS order-service
COPY --from=build /src/order-service/target/order-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM runtime AS inventory-service
COPY --from=build /src/inventory-service/target/inventory-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM runtime AS payment-service
COPY --from=build /src/payment-service/target/payment-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM runtime AS notification-service
COPY --from=build /src/notification-service/target/notification-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
