# Imagen de la API de SEGUIMORBI para desplegar en Render (u otro servicio con Docker).
# Ver docs/despliegue-render.md

# 1) Compilación
FROM maven:3.9-eclipse-temurin-17 AS compilacion
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package && cp target/seguimorbi-*.jar app.jar

# 2) Ejecución (solo JRE, sin código fuente ni Maven)
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 1001 seguimorbi
COPY --from=compilacion /app/app.jar app.jar
USER seguimorbi

# Ajustado al plan gratuito de Render (512 MB de RAM, CPU muy pequeña): limita la memoria y
# acelera el arranque. Se puede cambiar con la variable JAVA_OPTS.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
