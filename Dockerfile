# ---------- Build ----------
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src src
RUN mvn -B -q package -DskipTests && \
    java -Djarmode=tools -jar target/projetooficina-*.jar extract --layers --launcher --destination target/extracted

# ---------- Runtime ----------
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
# Aplica correções de segurança dos pacotes do sistema (ex.: libpng, zlib) e cria usuário sem privilégios
RUN apk upgrade --no-cache && addgroup -S oficina && adduser -S oficina -G oficina
COPY --from=build /app/target/extracted/dependencies/ ./
COPY --from=build /app/target/extracted/spring-boot-loader/ ./
COPY --from=build /app/target/extracted/snapshot-dependencies/ ./
COPY --from=build /app/target/extracted/application/ ./
USER oficina
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]
