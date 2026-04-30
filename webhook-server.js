// webhook-server.js
const express = require('express');
const { exec } = require('child_process');
const crypto = require('crypto');

const app = express();
const PORT = 3000;
const SECRET = process.env.WEBHOOK_SECRET;

// 로깅 미들웨어
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.path}`);
  next();
});

app.use(express.json({
  verify: (req, res, buf) => {
    const signature = req.headers['x-hub-signature-256'];
    if (!SECRET) throw new Error('WEBHOOK_SECRET is not configured on server');
    if (!signature) throw new Error('Missing X-Hub-Signature-256 header');

    const hmac = crypto.createHmac('sha256', SECRET);
    const digest = 'sha256=' + hmac.update(buf).digest('hex');
    if (signature !== digest) throw new Error('Invalid signature');
  }
}));

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

app.post('/webhook', (req, res) => {
  const event = req.headers['x-github-event'];
  console.log(`Webhook event received: ${event}`);
  
  if (event === 'push') {
    console.log('Push event received, updating Spring Boot container...');
    
    // 여러 가능한 디렉토리 경로 시도 (Git 저장소 경로와 Docker Compose 경로)
    const possiblePaths = [
      {
        gitPath: '/home/jangdonggun/포트폴리오/spring_sns_git',
        composePath: '/home/jangdonggun/포트폴리오/spring_sns_git/inhatc'
      },
      {
        gitPath: '/home/jangdonggun/spring_sns_git',
        composePath: '/home/jangdonggun/spring_sns_git/inhatc'
      },
      {
        gitPath: '/home/jangdonggun/포트폴리오/Spring_sns',
        composePath: '/home/jangdonggun/포트폴리오/Spring_sns'
      }
    ];
    
    let deployCommand = 'set -euo pipefail\n';
    for (const paths of possiblePaths) {
      deployCommand += `
        if [ -d "${paths.gitPath}" ] && [ -d "${paths.composePath}" ]; then
          echo "📂 Git 저장소: ${paths.gitPath}" &&
          echo "📂 Docker Compose 디렉토리: ${paths.composePath}" &&
          cd "${paths.gitPath}" &&
          echo "📥 최신 코드 가져오기..." &&
          git fetch origin main &&
          echo "🧹 배포 전 작업트리 정리..." &&
          git reset --hard origin/main &&
          git clean -fd &&
          echo "🐳 원격 Docker 이미지 업데이트..." &&
          cd "${paths.composePath}" &&
          # docker-compose.prod.yml 사용하여 원격 이미지 pull 및 실행
          (docker compose -f docker-compose.prod.yml pull app || docker-compose -f docker-compose.prod.yml pull app) &&
          (docker compose -f docker-compose.prod.yml up -d || docker-compose -f docker-compose.prod.yml up -d) &&
          (docker compose -f docker-compose.prod.yml ps || docker-compose -f docker-compose.prod.yml ps) &&
          echo "✅ 원격 이미지 배포 완료!" &&
          exit 0
        fi
      `;
    }
    deployCommand += 'echo "프로젝트 디렉토리를 찾을 수 없습니다" && exit 1';
    
    exec(deployCommand, { shell: '/bin/bash', maxBuffer: 1024 * 1024 * 10 }, (err, stdout, stderr) => {
      if (err) {
        console.error('배포 오류:', err);
        console.error('stderr:', stderr);
        res.status(500).json({ error: 'Deployment failed', message: err.message, stderr, stdout });
      } else {
        console.log('배포 성공!');
        console.log('stdout:', stdout);
        if (stderr) console.log('stderr:', stderr);
        res.status(200).json({ 
          success: true, 
          message: 'Deployment completed',
          output: stdout,
          stderr
        });
      }
    });
  } else {
    console.log(`ℹEvent ${event}는 처리하지 않습니다.`);
    res.status(200).json({ message: `Event ${event} received but not processed` });
  }
});

// Error handler (signature verification, etc.)
// eslint-disable-next-line no-unused-vars
app.use((err, req, res, next) => {
  const message = err?.message || 'Unknown error';
  const status =
    message.includes('Missing X-Hub-Signature-256') ? 403 :
    message.includes('Invalid signature') ? 403 :
    message.includes('WEBHOOK_SECRET is not configured') ? 500 :
    500;

  console.error('Request failed:', message);
  res.status(status).json({ success: false, error: message });
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Webhook server running on port ${PORT}`);
  console.log(`Listening on http://0.0.0.0:${PORT}/webhook`);
  console.log(`Health check: http://0.0.0.0:${PORT}/health`);
});

