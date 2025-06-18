# 최신 올바른 이미지 태그 사용
FROM gradle:8.8-jdk17-alpine AS builder

WORKDIR /app

# Gradle 설정 파일들 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle/ gradle/

# 의존성 다운로드
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY src/ src/

# 애플리케이션 빌드
RUN gradle build --no-daemon -x test

# 실행 단계 - 이미지 태그 변경
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]