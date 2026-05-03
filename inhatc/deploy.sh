#!/bin/bash

set -e

SERVER=${1:-localhost}
USER=${2:-ubuntu}
DEPLOY_PATH=${3:-/home/jangdonggun/포트폴리오/spring_sns_git/inhatc}

BRANCH=${4:-main}

echo "=========================================="
echo "🐳 Docker 기반 배포 시작"
echo "서버: $USER@$SERVER"
echo "경로: $DEPLOY_PATH"
echo "브랜치: $BRANCH"
echo "=========================================="

# 브랜치 → 이미지 태그 변환
if [ "$BRANCH" = "main" ]; then
  DOCKER_IMAGE_TAG="latest"
else
  DOCKER_IMAGE_TAG=$(echo "$BRANCH" | tr '/' '-')
fi

echo "👉 DOCKER_IMAGE_TAG=$DOCKER_IMAGE_TAG"

ssh $USER@$SERVER << EOF
  set -e

  echo "📂 배포 디렉토리 이동"
  cd $DEPLOY_PATH || exit 1

  echo "🐳 환경 변수 설정"
  export DOCKER_IMAGE_TAG=$DOCKER_IMAGE_TAG

  echo "📥 최신 이미지 pull"
  docker compose -f docker-compose.prod.yml pull app

  echo "🚀 컨테이너 재시작"
  docker compose -f docker-compose.prod.yml up -d

  echo "📊 상태 확인"
  docker compose -f docker-compose.prod.yml ps

  echo "📜 최근 로그"
  docker compose -f docker-compose.prod.yml logs --tail=50 app
EOF

echo "=========================================="
echo "[OK] Docker 배포 완료!"
echo "=========================================="