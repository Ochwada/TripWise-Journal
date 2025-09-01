# ---------- build stage ----------
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app


COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests dependency:go-offline


COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -DskipTests package

# ---------- runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Security: run as non-root
RUN useradd --no-create-home --uid 10001 appuser
USER appuser
.)
ARG JAR_NAME=app.jar
COPY --from=builder /app/target/*-SNAPSHOT.jar /app/${JAR_NAME}

# Spring Boot will read this as server.port
ENV SERVER_PORT=9096
EXPOSE 9091

ENTRYPOINT ["java","-jar","/app/app.jar"]