# Runtime-only Dockerfile: Actions에서 build/libs/*.jar 를 만들어 놓으면 이 JAR만 복사합니다.
FROM eclipse-temurin:17-jre
WORKDIR /app

# Actions가 생성한 JAR 파일을 복사
COPY build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

FROM amazoncorretto:17-alpine
WORKDIR /moyeorak
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY build/libs/auth-service-0.0.1-SNAPSHOT.jar auth-service.jar
#COPY keys/private_pkcs8.pem /moyeorak/config/private_pkcs8.pem
#COPY keys/public.pem /moyeorak/config/public.pem
RUN chown appuser:appgroup /moyeorak
USER appuser
ENTRYPOINT ["java", "-jar", "auth-service.jar"]
