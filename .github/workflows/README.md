# GitHub Actions CI/CD 설정 가이드

## 필요한 GitHub Secrets 설정

이 워크플로우를 사용하려면 다음 Secrets를 GitHub 저장소에 설정해야 합니다:

### 1. Docker Hub 인증 정보
- 아래 둘 중 **한 세트만** 설정하면 됩니다(워크플로우가 둘 다 지원).
  - **권장(기존 워크플로우 표기)**: `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`
  - **대체(일반 표기)**: `DOCKER_USERNAME`, `DOCKER_PASSWORD`

### 2. SSH 배포 정보 (선택사항)
- `SSH_HOST`: 배포 서버 호스트 주소 (예: `jangdonggun.iptime.org`)
- `SSH_USERNAME`: SSH 사용자명 (예: `ubuntu` 또는 `jangdonggun`)
- `SSH_PRIVATE_KEY`: SSH 개인 키
- `SSH_PORT`: SSH 포트 (선택, 기본값: 22)

### 3. Webhook 보안 시크릿 (권장)
- `WEBHOOK_SECRET`: 서버의 `WEBHOOK_SECRET`과 동일한 값
  - 설정하면 GitHub Actions가 `X-Hub-Signature-256`(HMAC-SHA256) 서명 헤더를 붙여 호출합니다.

## Secrets 설정 방법

1. GitHub 저장소로 이동
2. Settings → Secrets and variables → Actions 클릭
3. "New repository secret" 버튼 클릭
4. 위의 각 Secret을 추가

### SSH Secrets 설정 예시

- `SSH_HOST`: `jangdonggun.iptime.org`
- `SSH_USERNAME`: `ubuntu` (또는 실제 사용자명)
- `SSH_PRIVATE_KEY`: SSH 개인 키 전체 내용 (-----BEGIN OPENSSH PRIVATE KEY----- 부터 끝까지)
- `SSH_PORT`: `22` (또는 사용하는 포트)

### SSH 개인 키 생성 방법 (없는 경우)

```bash
# 서버에서 SSH 키 생성
ssh-keygen -t rsa -b 4096 -C "github-actions"

# 공개 키를 서버의 authorized_keys에 추가
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys

# 개인 키 내용 복사 (GitHub Secrets에 추가)
cat ~/.ssh/id_rsa
```

## 워크플로우 설명

### 1. Build and Test Job
- 코드 체크아웃
- Java 19 설정
- Maven 빌드 및 테스트 실행
- JAR 파일 아티팩트 업로드

### 2. Build Docker Image Job
- Docker 이미지 빌드
- Docker Hub에 푸시
- main/develop 브랜치에 push 시에만 실행

### 3. Deploy Job
- SSH를 통한 서버 배포
- docker-compose를 사용한 컨테이너 재시작
- main 브랜치에 push 시에만 실행

## 트리거 조건

- `main` 또는 `develop` 브랜치에 push
- `main` 또는 `develop` 브랜치로의 Pull Request

## Docker 이미지 태그

- 브랜치명 태그 (예: `main`, `develop`)
- SHA 태그 (예: `main-abc1234`)
- `latest` 태그 (main 브랜치만)

