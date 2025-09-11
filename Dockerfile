FROM amazoncorretto:17-alpine
WORKDIR /moyeorak
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY build/libs/auth-service-0.0.1-SNAPSHOT.jar auth-service.jar
RUN chown appuser:appgroup /moyeorak
USER appuser
ENTRYPOINT ["java", "-jar", "auth-service.jar"]
