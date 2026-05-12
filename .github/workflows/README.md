# spring_sns_git GitHub Actions 가이드

## 배포 방식 (중요)

이 저장소는 자체 webhook 서버를 사용하지 않고, 중앙 배포 게이트를 사용합니다.

- 중앙 게이트: `/home/jangdonggun/포트폴리오/portfolio/webhook-server.js`
- 액션 호출 주소: `http://<SSH_HOST>:3000/webhook`
- `spring_sns_git` 리포 push 이벤트를 중앙 게이트가 식별해 `portfolio/docker-compose.yml`의 `spring` 서비스만 재배포합니다.

## 필요한 GitHub Secrets

### 1) Docker Hub 인증
- 아래 두 세트 중 하나만 필요
  - `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`
  - `DOCKER_USERNAME`, `DOCKER_PASSWORD`

### 2) 배포 대상 서버 정보
- `SSH_HOST` (예: `jangdonggun.duckdns.org` 또는 서버 IP)
- `SSH_USERNAME`
- `SSH_PRIVATE_KEY`
- `SSH_PORT` (선택, 기본 22)

### 3) Webhook 보안
- `WEBHOOK_SECRET`
  - 중앙 게이트 서버의 `WEBHOOK_SECRET`와 완전히 동일해야 함
  - Actions가 `X-Hub-Signature-256`(HMAC SHA256) 헤더를 붙여 호출

## 워크플로우 동작 요약

1. Build/Test: Maven 빌드 + 테스트
2. Docker Build/Push: 브랜치 기준 이미지 태깅 후 푸시
3. Deploy:
   - 우선 중앙 webhook 게이트 호출
   - webhook 실패 시 SSH fallback 실행

## 트리거 조건

- `push`: `main`, `develop`
- `pull_request`: `main`, `develop`

## 운영 체크 포인트

- 서버에서 `pm2 status`에 `portfolio-webhook-gateway`가 `online`인지 확인
- `curl http://127.0.0.1:3000/health` 응답에서 프로젝트 목록에 `spring_sns_git` 포함 여부 확인
- webhook 실패 시 Actions 로그에서 서명/시크릿 불일치 여부 확인

