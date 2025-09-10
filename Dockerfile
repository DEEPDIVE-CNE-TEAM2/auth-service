# Runtime-only Dockerfile: Actions에서 build/libs/*.jar 를 만들어 놓으면 이 JAR만 복사합니다.
FROM eclipse-temurin:17-jre
WORKDIR /app

# Actions가 생성한 JAR 파일을 복사
COPY build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

