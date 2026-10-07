# Multi-stage build를 사용하여 이미지 크기 최적화
FROM eclipse-temurin:19-jdk-alpine AS builder

WORKDIR /app

# Maven wrapper와 pom.xml 복사
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# 의존성 다운로드 (캐시 활용)
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# 소스 코드 복사 및 빌드
COPY src ./src
RUN ./mvnw clean package -DskipTests

# 실행 단계
FROM eclipse-temurin:19-jre-alpine

WORKDIR /app

RUN apk add --no-cache wget

# 빌드된 JAR 파일 복사
COPY --from=builder /app/target/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

