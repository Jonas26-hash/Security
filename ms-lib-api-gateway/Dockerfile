FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/ms-lib-api-gateway-1.0.0.jar app.jar

ENV JAVA_OPTS="-Xmx512m -Xms256m"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
