@echo off
REM Spring Boot 프로젝트 빌드 스크립트 (Windows)
REM 사용법: build.bat [profile]
REM 예시: build.bat prod

set PROFILE=%1
if "%PROFILE%"=="" set PROFILE=prod

echo ==========================================
echo Spring Boot 프로젝트 빌드 시작
echo 프로파일: %PROFILE%
echo ==========================================

REM Maven 빌드 (Maven Wrapper 사용)
echo Maven 빌드 실행 중...
call mvnw.cmd clean package -DskipTests -P%PROFILE%

REM 빌드 결과 확인
if exist "target\inhatc-0.0.1-SNAPSHOT.jar" (
    echo ==========================================
    echo [OK] 빌드 성공!
    echo JAR 파일 위치: target\inhatc-0.0.1-SNAPSHOT.jar
    echo ==========================================
    exit /b 0
) else (
    echo ==========================================
    echo [ERROR] 빌드 실패: JAR 파일을 찾을 수 없습니다.
    echo ==========================================
    exit /b 1
)

