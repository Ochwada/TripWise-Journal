# syntax=docker/dockerfile:1

# ---------- build stage ----------
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# 1) Cache deps
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests dependency:go-offline

# 2) Build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests package

# ---------- runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR as root (then drop privileges)
ARG JAR_NAME=app.jar
# adjust pattern if you don't use -SNAPSHOT
COPY --from=builder /app/target/*-SNAPSHOT.jar /app/${JAR_NAME}

# Drop root
RUN useradd --no-create-home --uid 10001 appuser
USER 10001

# Set service port (change per service, see table below)
ENV SERVER_PORT=9094
EXPOSE 9094

ENTRYPOINT ["java","-jar","/app/app.jar"]
