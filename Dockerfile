FROM amazoncorretto:17-alpine
WORKDIR /moyeorak
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY build/libs/enrollment-service-0.0.1-SNAPSHOT.jar enrollment-service.jar
RUN chown appuser:appgroup /moyeorak
USER appuser
ENTRYPOINT ["java", "-jar", "enrollment-service.jar"]
