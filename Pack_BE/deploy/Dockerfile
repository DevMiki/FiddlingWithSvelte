# Stage 1: Build the pack-application
FROM eclipse-temurin:17-jdk-jammy AS builder
RUN adduser --system --no-create-home appuser
COPY .mvn/ .mvn
COPY mvnw ../pom.xml ./
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-jammy

ARG APP_USER=appuser
ARG APP_GROUP=appgroup
RUN groupadd -r ${APP_GROUP} && useradd --no-log-init -r -g ${APP_GROUP} ${APP_USER}

WORKDIR /pack-app
COPY --from=builder --chown=${APP_USER}:${APP_GROUP} /target/incredible-pack-app.jar incredible-pack-app.jar
USER ${APP_USER}
# Expose the port the pack-app runs on
EXPOSE 8080

# Command to run the pack-application
# Add JAVA_OPTS for memory configuration if needed, e.g. -Xmx512m
ENTRYPOINT ["java", "-jar", "incredible-pack-app.jar"]