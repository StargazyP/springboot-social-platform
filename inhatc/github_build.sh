#!/bin/bash

# GitHub에 변경사항 커밋 및 푸시 스크립트 (현재 브랜치 기준으로 push)
# 사용법: ./github_build.sh [커밋 메시지]
# 예시: ./github_build.sh "댓글 기능 개선"

set -e  # 에러 발생 시 스크립트 중단

COMMIT_MSG=${1:-"Update: 자동 커밋"}
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=========================================="
echo "GitHub 커밋 및 푸시 시작"
echo "프로젝트 디렉토리: $PROJECT_DIR"
echo "커밋 메시지: $COMMIT_MSG"
echo "=========================================="

# # Git 저장소 확인
# cd "$PROJECT_DIR"
# if [ ! -d ".git" ]; then
#     echo "=========================================="
#     echo "[ERROR] Git 저장소가 아닙니다."
#     echo "=========================================="
#     exit 1
# fi

# 현재 브랜치 확인
CURRENT_BRANCH=$(git branch --show-current)
echo "현재 브랜치: $CURRENT_BRANCH"

# 변경사항 확인
if [ -z "$(git status --porcelain)" ]; then
    echo "=========================================="
    echo "[INFO] 커밋할 변경사항이 없습니다."
    echo "=========================================="
    exit 0
fi

# 변경사항 표시
echo ""
echo "변경된 파일:"
git status --short
echo ""

# 사용자 확인
read -p "위 변경사항을 커밋하고 푸시하시겠습니까? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "취소되었습니다."
    exit 0
fi

# Git add
echo "1. Git add 실행 중..."
git add .
echo "[OK] 모든 변경사항 스테이징 완료"

# Git commit
echo "2. Git commit 실행 중..."
git commit -m "$COMMIT_MSG"
echo "[OK] 커밋 완료"

# Git push
echo "3. Git push 실행 중..."
git push origin "$CURRENT_BRANCH"
echo "[OK] 푸시 완료"

echo "=========================================="
echo "[OK] GitHub 커밋 및 푸시 완료!"
echo "브랜치: $CURRENT_BRANCH"
echo "커밋 메시지: $COMMIT_MSG"
echo "=========================================="

