#!/bin/bash
set -e

COMMIT_MSG=${1:-"Update: auto commit"}

echo "=========================================="
echo "🚀 Git 자동 커밋 & 푸시"
echo "=========================================="

CURRENT_BRANCH=$(git branch --show-current)
echo "브랜치: $CURRENT_BRANCH"

if [ -z "$(git status --porcelain)" ]; then
    echo "[INFO] 변경사항 없음"
    exit 0
fi

echo "📦 변경사항:"
git status --short

echo "👉 자동 커밋 진행..."

git add .
git commit -m "$COMMIT_MSG"
git push origin "$CURRENT_BRANCH"

echo "=========================================="
echo "[OK] 푸시 완료 → CI/CD 트리거됨"
echo "=========================================="