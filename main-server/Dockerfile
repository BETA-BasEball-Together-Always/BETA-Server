# Build Stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Gradle Wrapper 실행을 위한 도구 설치
RUN apk add --no-cache bash curl unzip

# 루트 전체 복사 (settings.gradle 포함)
COPY .. .

# Gradle Wrapper 권한 부여 + 빌드
RUN chmod +x ./gradlew && ./gradlew :main-server:bootJar --no-daemon

# Run Stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /workspace/main-server/build/libs/*.jar /app/main-server.jar

# 컨테이너 환경 설정
EXPOSE 8080
ENV TZ=Asia/Seoul
ENV JAVA_TOOL_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTS -jar /app/main-server.jar"]
