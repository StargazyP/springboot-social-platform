# 소셜 미디어 플랫폼 (X/Twitter 스타일)

Spring Boot 기반의 실시간 소셜 미디어 플랫폼입니다. 사용자 간 게시글 공유, 댓글, 좋아요, 실시간 채팅 등의 기능을 제공합니다.

## 구현 GIF
![Animation2](https://github.com/user-attachments/assets/50a801ca-3d45-4927-882c-017a88aa74b5)

---

## 목차

- [프로젝트 소개](#프로젝트-소개)
- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [데이터베이스 설계](#데이터베이스-설계)
- [실행 방법](#실행-방법)
- [API 명세서](#api-명세서)
- [주요 화면](#주요-화면)

---

## 프로젝트 소개

이 프로젝트는 **Spring Boot 3.3.5**와 **MySQL**을 활용하여 개발된 풀스택 소셜 미디어 애플리케이션입니다. X(Twitter)의 UI/UX를 참고하여 직관적인 인터페이스를 제공하며, **WebSocket**을 통한 실시간 채팅 기능을 구현했습니다.

### 핵심 특징

-   **실시간 통신**: WebSocket(STOMP) 기반 1:1 채팅
-   **소셜 기능**: 게시글 작성, 댓글, 좋아요, 알림
-   **이미지 처리**: 게시글 및 메시지 이미지 업로드/표시
-   **반응형 디자인**: X(Twitter) 스타일의 모던한 UI
-   **세션 관리**: HttpSession 기반 사용자 인증

---

## 주요 기능

### 1. 사용자 인증 및 프로필 관리
- 로그인/로그아웃
- 프로필 이미지 업로드 및 수정
- 마이페이지에서 본인 게시글 조회
- 다른 사용자 프로필 조회

### 2. 게시글 기능
- 게시글 작성 (텍스트 + 이미지)
- 게시글 조회 (전체/개별/사용자별/팔로잉)
- 게시글 삭제 (Soft Delete)
- 게시글 상세 모달 뷰
- 페이징 지원

### 3. 소셜 상호작용
- 좋아요 (토글 방식)
- 댓글 작성, 수정, 삭제 및 조회
- 대댓글 구조 지원
- 알림 시스템 (좋아요/댓글 알림)
- 팔로우/언팔로우 기능

### 4. 실시간 채팅
- WebSocket 기반 1:1 채팅
- 대화 목록 조회 (최신 메시지 기준 정렬)
- 채팅 내역 조회
- 이미지 전송 기능
- 실시간 메시지 수신

### 5. 알림 시스템
- 좋아요 알림
- 댓글 알림
- 알림 목록 조회 (읽음/안읽음)
- 알림 읽음 처리 (개별/전체)
- 알림 클릭 시 해당 게시글 모달 표시

---

## 기술 스택

### Backend
- **Framework**: Spring Boot 3.3.5
- **Language**: Java 19
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA / Hibernate
- **WebSocket**: Spring WebSocket (STOMP)
- **Template Engine**: Thymeleaf
- **Build Tool**: Maven
- **Security**: Spring Security (BCrypt PasswordEncoder)
- **Validation**: Bean Validation

### Frontend
- **HTML5 / CSS3**
- **JavaScript (Vanilla)**
- **SockJS / STOMP.js** (WebSocket 클라이언트)

### 주요 라이브러리
- **Lombok**: Boilerplate 코드 감소
- **Commons IO**: 파일 처리
- **JSON**: JSON 파싱

---

## 프로젝트 구조

```
inhatc/
├── src/main/java/kr/co/inhatc/inhatc/
│   ├── controller/          # REST API 및 페이지 컨트롤러
│   │   ├── PostController.java
│   │   ├── CommentController.java
│   │   ├── MemberController.java
│   │   ├── MemberPageController.java
│   │   ├── FollowController.java
│   │   ├── ChatController.java
│   │   ├── MessageController.java
│   │   ├── NotificationController.java
│   │   ├── ImageController.java
│   │   └── HomeController.java
│   ├── service/             # 비즈니스 로직
│   │   ├── PostService.java
│   │   ├── CommentService.java
│   │   ├── MemberService.java
│   │   ├── FollowService.java
│   │   ├── MessageService.java
│   │   └── NotificationService.java
│   ├── repository/          # JPA Repository
│   │   ├── PostRepository.java
│   │   ├── CommentRepository.java
│   │   ├── MemberRepository.java
│   │   ├── FollowRepository.java
│   │   ├── MessageRepository.java
│   │   └── NotificationRepository.java
│   ├── entity/              # JPA 엔티티
│   │   ├── PostEntity.java
│   │   ├── MemberEntity.java
│   │   ├── CommentEntity.java
│   │   ├── FollowEntity.java
│   │   ├── LikeEntity.java
│   │   ├── MessageEntity.java
│   │   └── NotificationEntity.java
│   ├── dto/                 # 데이터 전송 객체
│   │   ├── PostRequestDTO.java
│   │   ├── PostResponseDTO.java
│   │   ├── CommentRequestDTO.java
│   │   ├── CommentResponseDTO.java
│   │   ├── MemberDTO.java
│   │   ├── FollowDTO.java
│   │   ├── FollowStatsDTO.java
│   │   ├── MessageDTO.java
│   │   └── NotificationDTO.java
│   ├── WebSocketConfig.java # WebSocket 설정
│   ├── ChatWebSocketHandler.java # WebSocket 메시지 핸들러
│   ├── exception/           # 예외 처리
│   │   ├── CustomException.java
│   │   ├── ErrorCode.java
│   │   └── GlobalExceptionHandler.java
│   └── constants/           # 상수 정의
│       └── AppConstants.java
├── src/main/resources/
│   ├── templates/           # Thymeleaf 템플릿
│   │   ├── main.html        # 메인 피드 페이지
│   │   ├── mypage.html      # 마이페이지
│   │   ├── message.html     # 채팅 페이지
│   │   ├── login.html       # 로그인 페이지
│   │   ├── post.html        # 게시글 상세 페이지
│   │   └── notification.html
│   ├── static/              # 정적 리소스
│   │   ├── css/             # 스타일시트
│   │   ├── js/              # JavaScript 파일
│   │   └── images/          # 아이콘 이미지
│   └── application.properties
└── pom.xml
```

---

## 데이터베이스 설계

### 주요 엔티티

#### 1. MemberEntity (회원)
- `id`: 회원 ID (PK, Auto Increment)
- `member_email`: 이메일 (UK, 로그인 ID)
- `member_password`: 비밀번호 (BCrypt 암호화)
- `member_name`: 이름
- `profile_picture_path`: 프로필 이미지 경로

#### 2. PostEntity (게시글)
- `id`: 게시글 ID (PK, Auto Increment)
- `content`: 게시글 내용 (최대 2000자)
- `imgsource`: 이미지 경로
- `member_email`: 작성자 이메일 (FK)
- `love`: 좋아요 수
- `hits`: 조회수
- `delete_yn`: 삭제 여부 (Y/N, Soft Delete)
- `created_date`: 작성일시
- `modified_date`: 수정일시

#### 3. CommentEntity (댓글)
- `id`: 댓글 ID (PK, Auto Increment)
- `comment`: 댓글 내용
- `post_id`: 게시글 ID (FK)
- `member_email`: 작성자 이메일 (FK)
- `parent_id`: 부모 댓글 ID (대댓글 지원, NULL 가능)
- `create_date`: 작성일시

#### 4. LikeEntity (좋아요)
- `post_id`: 게시글 ID (FK, 복합 키)
- `member_email`: 좋아요한 사용자 이메일 (FK, 복합 키)
- 복합 키로 중복 방지

#### 5. FollowEntity (팔로우)
- `id`: 팔로우 ID (PK, Auto Increment)
- `follower_email`: 팔로워 이메일 (FK)
- `following_email`: 팔로잉 이메일 (FK)
- `created_at`: 팔로우 일시

#### 6. MessageEntity (메시지)
- `id`: 메시지 ID (PK, Auto Increment)
- `sender`: 발신자 이메일 (FK)
- `receiver`: 수신자 이메일 (FK)
- `content`: 메시지 내용
- `image_path`: 이미지 경로
- `timestamp`: 전송 시간

#### 7. NotificationEntity (알림)
- `id`: 알림 ID (PK, Auto Increment)
- `post_id`: 게시글 ID (FK)
- `notification_type`: 알림 타입 (LIKE/COMMENT)
- `actor_email`: 행위자 이메일 (FK)
- `recipient_email`: 수신자 이메일 (FK)
- `is_read`: 읽음 여부 (Boolean)
- `created_at`: 생성일시

---

## 실행 방법

### 사전 요구사항
- Java 19 이상
- Maven 3.6 이상
- MySQL 8.0 이상

### 1. 데이터베이스 설정

MySQL에서 데이터베이스 생성:
```sql
CREATE DATABASE member CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. application.properties 설정

`src/main/resources/application.properties` 파일을 확인하고 데이터베이스 연결 정보를 수정하세요:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/member?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
```

### 3. 프로젝트 실행

```bash
# 프로젝트 디렉토리로 이동
cd inhatc

# Maven 빌드
mvn clean install

# 애플리케이션 실행
mvn spring-boot:run
```

또는 IDE에서 `InhatcApplication.java`를 실행하세요.

### 4. 접속

브라우저에서 `http://localhost:8080` 접속

---

## API 명세서

### 인증 및 회원 관리 API

#### 1. 로그인
- **Method**: `POST`
- **Endpoint**: `/api/members/login`
- **Content-Type**: `application/x-www-form-urlencoded`
- **Request Parameters**:
  - `email` (String, 필수): 사용자 이메일
  - `password` (String, 필수): 비밀번호
- **Response**: 
  - 성공: `302 Redirect` → `/main`
  - 실패: `login.html` 템플릿 반환 (에러 메시지 포함)
- **설명**: 세션 고정 공격 방지를 위해 로그인 성공 시 기존 세션 무효화 후 새 세션 생성

#### 2. 로그아웃
- **Method**: `POST`
- **Endpoint**: `/api/members/logout`
- **Response**: `302 Redirect` → `/login`
- **설명**: 세션 무효화 처리

#### 3. 현재 로그인 사용자 조회
- **Method**: `GET`
- **Endpoint**: `/api/members/session`
- **Response**: 
```json
{
  "loginEmail": "user@example.com"
}
```

#### 4. 마이페이지 조회 (이메일 기반)
- **Method**: `GET`
- **Endpoint**: `/api/members/{email}/mypage`
- **Response**: 
```json
{
  "id": 1,
  "memberEmail": "user@example.com",
  "memberName": "사용자명",
  "profilePicturePath": "/images/user@example.com/profile.png"
}
```

#### 5. 프로필 이미지 업로드
- **Method**: `POST`
- **Endpoint**: `/api/members/{email}/upload-profile`
- **Content-Type**: `multipart/form-data`
- **Request Parameters**:
  - `file` (MultipartFile, 필수): 이미지 파일
- **Response**: 
  - 성공: `200 OK` - "프로필 이미지가 업로드되었습니다."
  - 실패: `400 Bad Request` - 에러 메시지

---

### 게시글 API

#### 1. 전체 게시글 조회 (페이징 지원)
- **Method**: `GET`
- **Endpoint**: `/api/posts`
- **Query Parameters**:
  - `page` (int, 기본값: 0): 페이지 번호
  - `size` (int, 기본값: 20): 페이지 크기
  - `sort` (String, 기본값: "createdDate,desc"): 정렬 기준
- **Response**: 
```json
{
  "content": [
    {
      "id": 1,
      "content": "게시글 내용",
      "imgsource": "/posts/user@example.com/20251213191915833.png",
      "writerEmail": "user@example.com",
      "writerName": "사용자명",
      "love": 5,
      "hits": 10,
      "createdDate": "2025-12-13T19:19:15",
      "isLiked": false
    }
  ],
  "totalElements": 100,
  "totalPages": 5,
  "number": 0,
  "size": 20
}
```

#### 2. 게시글 상세 조회
- **Method**: `GET`
- **Endpoint**: `/api/posts/{id}`
- **Response**: 
```json
{
  "id": 1,
  "content": "게시글 내용",
  "imgsource": "/posts/user@example.com/image.png",
  "writerEmail": "user@example.com",
  "writerName": "사용자명",
  "love": 5,
  "hits": 10,
  "createdDate": "2025-12-13T19:19:15",
  "isLiked": false
}
```

#### 3. 사용자별 게시글 조회 (ID 기준)
- **Method**: `GET`
- **Endpoint**: `/api/posts/user/id/{memberId}`
- **Response**: `PostResponseDTO[]` 배열

#### 4. 사용자별 게시글 조회 (이름 기준)
- **Method**: `GET`
- **Endpoint**: `/api/posts/user/name/{memberName}`
- **Response**: `PostResponseDTO[]` 배열

#### 5. 삭제 여부별 게시글 조회
- **Method**: `GET`
- **Endpoint**: `/api/posts/deleted/{deleteYn}`
- **Path Variable**: `deleteYn` (char: 'Y' 또는 'N')
- **Response**: `PostResponseDTO[]` 배열

#### 6. 팔로잉 게시글 조회
- **Method**: `GET`
- **Endpoint**: `/api/posts/following`
- **Query Parameters**:
  - `email` (String, 필수): 사용자 이메일
- **Response**: `PostResponseDTO[]` 배열

#### 7. 게시글 작성
- **Method**: `POST`
- **Endpoint**: `/api/posts`
- **Content-Type**: `multipart/form-data`
- **Request Parameters**:
  - `file` (MultipartFile, 선택): 이미지 파일
  - `content` (String, 필수, 최대 2000자): 게시글 내용
- **Response**: 
  - 성공: `201 Created` - "게시글이 업로드되었습니다."
  - 실패: `400 Bad Request` 또는 `401 Unauthorized`

#### 8. 게시글 삭제
- **Method**: `DELETE`
- **Endpoint**: `/api/posts/{id}`
- **Response**: `204 No Content`

#### 9. 좋아요 토글
- **Method**: `POST`
- **Endpoint**: `/api/posts/{postId}/likes`
- **Query Parameters**:
  - `email` (String, 필수): 사용자 이메일
- **Response**: 
```json
{
  "liked": true,
  "postId": 1
}
```

---

### 댓글 API

#### 1. 댓글 목록 조회
- **Method**: `GET`
- **Endpoint**: `/api/posts/{postId}/comments`
- **Query Parameters**:
  - `all` (boolean, 기본값: false): 전체 조회 여부
  - `page` (int, 기본값: 0): 페이지 번호 (all=false일 때)
  - `size` (int, 기본값: 20): 페이지 크기 (all=false일 때)
- **Response (페이징 모드)**:
```json
{
  "content": [
    {
      "id": 1,
      "comment": "댓글 내용",
      "postId": 1,
      "userEmail": "user@example.com",
      "userName": "사용자명",
      "createDate": "2025-12-13T19:20:00",
      "parentId": null
    }
  ],
  "totalElements": 10,
  "totalPages": 1
}
```
- **Response (전체 조회 모드)**: `CommentResponseDTO[]` 배열

#### 2. 댓글 작성
- **Method**: `POST`
- **Endpoint**: `/api/posts/{postId}/comments`
- **Content-Type**: `application/json`
- **Request Body**:
```json
{
  "comment": "댓글 내용",
  "user": "user@example.com",
  "article": 1,
  "parentId": null
}
```
- **Response**: `201 Created` - `CommentResponseDTO` 객체

#### 3. 댓글 수정
- **Method**: `PUT`
- **Endpoint**: `/api/posts/{postId}/comments/{commentId}`
- **Content-Type**: `application/json`
- **Request Body**: `CommentRequestDTO`
- **Response**: `200 OK` - "댓글이 수정되었습니다."

#### 4. 댓글 삭제
- **Method**: `DELETE`
- **Endpoint**: `/api/posts/{postId}/comments/{commentId}`
- **Response**: `204 No Content`

---

### 팔로우 API

#### 1. 팔로우하기
- **Method**: `POST`
- **Endpoint**: `/api/members/me/following/{followingEmail}`
- **설명**: 세션에서 로그인 사용자 정보를 가져와 팔로우 처리
- **Response**: 
  - 성공: `200 OK` - "팔로우 성공"
  - 실패: `400 Bad Request` 또는 `401 Unauthorized`

#### 2. 언팔로우하기
- **Method**: `DELETE`
- **Endpoint**: `/api/members/me/following/{followingEmail}`
- **Response**: 
  - 성공: `200 OK` - "언팔로우 성공"
  - 실패: `404 Not Found` 또는 `401 Unauthorized`

#### 3. 팔로우 여부 확인
- **Method**: `GET`
- **Endpoint**: `/api/members/me/following/{followingEmail}/status`
- **Response**: `boolean` (true: 팔로우 중, false: 팔로우 안 함)

#### 4. 팔로워 목록 조회
- **Method**: `GET`
- **Endpoint**: `/api/members/{memberEmail}/followers`
- **Response**: 
```json
[
  {
    "email": "follower@example.com",
    "name": "팔로워명",
    "profilePicturePath": "/images/follower@example.com/profile.png",
    "isFollowing": false
  }
]
```

#### 5. 팔로잉 목록 조회
- **Method**: `GET`
- **Endpoint**: `/api/members/{memberEmail}/following`
- **Response**: `FollowDTO[]` 배열

#### 6. 팔로우 통계 조회
- **Method**: `GET`
- **Endpoint**: `/api/members/{memberEmail}/follow-stats`
- **Response**: 
```json
{
  "followerCount": 10,
  "followingCount": 5,
  "isFollowing": false
}
```

---

### 메시지 API

#### 1. 채팅 내역 조회
- **Method**: `GET`
- **Endpoint**: `/api/messages`
- **Query Parameters**:
  - `receiver` (String, 필수): 수신자 이메일
- **Response**: 
```json
[
  {
    "id": 1,
    "sender": "user1@example.com",
    "receiver": "user2@example.com",
    "content": "메시지 내용",
    "imagePath": "/images/user1@example.com/messages/image.png",
    "timestamp": "2025-12-13T19:30:00"
  }
]
```

#### 2. 대화 목록 조회
- **Method**: `GET`
- **Endpoint**: `/api/messages/conversations`
- **설명**: 세션의 로그인 사용자를 기준으로 대화 목록 조회
- **Response**: 
```json
[
  {
    "otherUserEmail": "user2@example.com",
    "otherUserName": "사용자2",
    "lastMessage": "마지막 메시지 내용",
    "lastMessageTime": "2025-12-13T19:30:00",
    "unreadCount": 2
  }
]
```

#### 3. 메시지 이미지 업로드
- **Method**: `POST`
- **Endpoint**: `/api/messages/images`
- **Content-Type**: `multipart/form-data`
- **Request Parameters**:
  - `file` (MultipartFile, 필수): 이미지 파일
  - `userEmail` (String, 필수): 사용자 이메일
- **Response**: 
```json
{
  "imagePath": "/images/user@example.com/messages/20251213193000.png",
  "message": "이미지가 업로드되었습니다."
}
```

---

### 알림 API

#### 1. 알림 목록 조회 (읽지 않은 알림만)
- **Method**: `GET`
- **Endpoint**: `/api/notifications`
- **설명**: 세션에서 로그인 사용자 정보를 가져와 알림 조회
- **Response**: 
```json
[
  {
    "id": 1,
    "postId": 1,
    "notificationType": "LIKE",
    "actorEmail": "user2@example.com",
    "actorName": "사용자2",
    "recipientEmail": "user1@example.com",
    "isRead": false,
    "createdAt": "2025-12-13T19:30:00"
  }
]
```

#### 2. 모든 알림 조회 (읽음/안읽음 모두)
- **Method**: `GET`
- **Endpoint**: `/api/notifications/all`
- **Response**: `NotificationDTO[]` 배열

#### 3. 알림 읽음 처리
- **Method**: `POST`
- **Endpoint**: `/api/notifications/{notificationId}/read`
- **Response**: `204 No Content`

#### 4. 모든 알림 읽음 처리
- **Method**: `POST`
- **Endpoint**: `/api/notifications/read-all`
- **Response**: `204 No Content`

#### 5. 알림 페이지 (HTML)
- **Method**: `GET`
- **Endpoint**: `/posts/check`
- **Response**: `notification.html` 템플릿

#### 6. 알림 페이지 (전체)
- **Method**: `GET`
- **Endpoint**: `/posts/notifications/page`
- **Response**: `notifications.html` 템플릿

---

### 이미지 API

#### 1. 게시글 이미지 조회
- **Method**: `GET`
- **Endpoint**: `/images/api/image/{id}`
- **Response**: 이미지 파일 (Content-Type: image/png, image/jpeg 등)

#### 2. 프로필 이미지 조회
- **Method**: `GET`
- **Endpoint**: `/member/Userimgsource/{email}`
- **Response**: 이미지 파일 (Content-Type: image/png, image/jpeg 등)
- **설명**: 프로필 이미지가 없으면 기본 이미지 반환

---

### 페이지 컨트롤러 (HTML 반환)

#### 1. 로그인 페이지
- **Method**: `GET`
- **Endpoint**: `/`
- **Response**: `login.html` 템플릿

#### 2. 메인 피드 페이지
- **Method**: `GET`
- **Endpoint**: `/main`
- **Response**: `main.html` 템플릿

#### 3. 게시글 상세 페이지
- **Method**: `GET`
- **Endpoint**: `/posts/{id}`
- **Response**: `post.html` 템플릿

#### 4. 마이페이지
- **Method**: `GET`
- **Endpoint**: `/member/mypage`
- **Response**: `mypage.html` 템플릿
- **설명**: 세션에서 로그인 사용자 정보를 가져와 본인 마이페이지 표시

#### 5. 사용자 프로필 페이지
- **Method**: `GET`
- **Endpoint**: `/member/profile/{email}`
- **Response**: `mypage.html` 템플릿
- **설명**: 다른 사용자의 프로필 페이지 (URL 인코딩 지원)

#### 6. 채팅 페이지
- **Method**: `GET`
- **Endpoint**: `/chat/message`
- **Query Parameters**:
  - `receiverEmail` (String, 선택): 수신자 이메일 (없으면 대화 목록 모드)
- **Response**: `message.html` 템플릿

---

### WebSocket API

#### 연결 설정
- **엔드포인트**: `/ws`
- **프로토콜**: STOMP over SockJS
- **Fallback**: SockJS를 통한 다양한 전송 방식 지원

#### 메시지 전송
- **Destination**: `/app/chat.sendMessage`
- **Request Body**:
```json
{
  "sender": "user1@example.com",
  "receiver": "user2@example.com",
  "content": "메시지 내용",
  "imagePath": "/images/user1@example.com/messages/image.png"
}
```

#### 메시지 구독
- **구독 경로**: `/topic/chat/{roomName}`
- **roomName 형식**: 두 사용자 이메일을 정렬하여 `{email1}_{email2}` 형식으로 생성
- **예시**: `user1@example.com`과 `user2@example.com`의 방 이름은 `user1@example.com_user2@example.com`

#### 메시지 수신
- **형식**: JSON
```json
{
  "sender": "user1@example.com",
  "receiver": "user2@example.com",
  "content": "메시지 내용",
  "imagePath": null,
  "timestamp": "2025-12-13T19:30:00"
}
```

---

## 주요 화면

### 1. 메인 피드 (`/main`)
- 게시글 작성 폼 (텍스트 + 이미지)
- 전체 게시글 피드 (페이징)
- 좋아요/댓글 기능
- 알림 아이콘 (읽지 않은 알림 수 표시)
- 프로필 클릭 시 사용자 프로필 페이지 이동

### 2. 마이페이지 (`/member/mypage`)
- 프로필 정보 (이름, 이메일, 프로필 이미지)
- 본인 게시글 목록
- 프로필 이미지 수정
- 팔로워/팔로잉 수 표시
- 팔로우/언팔로우 버튼 (다른 사용자 프로필일 때)

### 3. 채팅 페이지 (`/chat/message`)
- 대화 목록 모드 (receiverEmail 없을 때)
  - 최신 메시지 기준 정렬
  - 읽지 않은 메시지 수 표시
- 1:1 채팅 화면 (receiverEmail 있을 때)
  - 실시간 메시지 수신
  - 텍스트 메시지 전송
  - 이미지 전송
  - 메시지 히스토리 로드

### 4. 알림 페이지 (`/posts/check`)
- 좋아요/댓글 알림 목록
- 읽음/안읽음 상태 표시
- 알림 클릭 시 해당 게시글 모달 표시
- 전체 읽음 처리 기능

### 5. 게시글 상세 페이지 (`/posts/{id}`)
- 게시글 상세 정보
- 댓글 목록 (페이징 지원)
- 댓글 작성 폼
- 좋아요 기능

---

## 주요 구현 사항

### 1. WebSocket 실시간 채팅
- **STOMP 프로토콜** 사용
- **SockJS** Fallback 지원
- 방(Room) 기반 메시지 라우팅
- 메시지 영구 저장 (DB)
- 실시간 양방향 통신

### 2. 이미지 업로드
- 사용자별 폴더 구조로 관리
- 프로필 이미지: `{email}/profile.png`
- 게시글 이미지: `{email}/{timestamp}.{ext}`
- 메시지 이미지: `{email}/messages/{timestamp}.{ext}`
- 파일 확장자 검증 (png, jpg, jpeg, gif)
- 파일 크기 제한

### 3. 알림 시스템
- 좋아요/댓글 시 자동 알림 생성
- 읽음/안읽음 상태 관리
- 실시간 알림 카운트 (폴링 방식)
- 알림 타입별 분류

### 4. 세션 관리
- `HttpSession` 기반 인증
- 로그인 상태 확인 미들웨어
- 세션 만료 시 로그인 페이지 리다이렉트
- 세션 고정 공격 방지 (로그인 시 세션 재생성)

### 5. 보안 기능
- BCrypt 비밀번호 암호화
- 세션 기반 인증
- 파일 업로드 검증
- SQL Injection 방지 (JPA 사용)
- XSS 방지 (Thymeleaf 자동 이스케이프)

### 6. 페이징 처리
- Spring Data JPA의 `Pageable` 사용
- 게시글 목록 페이징
- 댓글 목록 페이징
- 기본 페이지 크기: 20개

### 7. 대댓글 구조
- `parent_id` 필드를 통한 계층 구조 지원
- 댓글과 대댓글 구분 표시
- 재귀적 댓글 조회

---

## 개발 이슈 및 해결

### 1. 순환 참조 문제
- **문제**: `PostService`와 `NotificationService` 간 순환 참조
- **해결**: `@Lazy` 어노테이션으로 지연 초기화

### 2. Thymeleaf URL 인코딩
- **문제**: 동적 URL 파라미터에서 특수문자(@ 등) 오류
- **해결**: `th:href` 사용으로 서버 사이드 URL 생성, URLDecoder 사용

### 3. WebSocket 연결 안정성
- **문제**: 네트워크 불안정 시 연결 끊김
- **해결**: SockJS Fallback 및 재연결 로직 구현

### 4. 파일 업로드 경로 관리
- **문제**: 절대 경로 하드코딩
- **해결**: `application.properties`에서 경로 설정, `@Value`로 주입

### 5. 세션 관리
- **문제**: 세션 고정 공격 취약점
- **해결**: 로그인 성공 시 기존 세션 무효화 후 새 세션 생성

---

## 구현 내용

- Spring Boot 3.x 기반 RESTful API 설계
- JPA를 활용한 데이터베이스 연동
- WebSocket을 통한 실시간 통신 구현
- Thymeleaf 템플릿 엔진 활용
- 세션 기반 인증 및 권한 관리
- 파일 업로드/다운로드 처리
- 예외 처리 및 글로벌 핸들러 구현
- Bean Validation을 통한 입력 검증
- 페이징 및 정렬 기능 구현
- 대댓글 구조 지원
- 알림 시스템 구현

---

## 향후 개선 사항

- [ ] JWT 기반 인증으로 전환
- [ ] Redis를 활용한 세션 관리
- [ ] 이미지 리사이징 및 최적화
- [ ] 무한 스크롤 구현
- [ ] 검색 기능 추가
- [ ] 해시태그 기능
- [ ] 공유 기능 (URL 공유)
- [ ] 다국어 지원
- [ ] 단위 테스트 및 통합 테스트 작성
- [ ] CI/CD 파이프라인 구축

---

## 라이선스

이 프로젝트는 개인 포트폴리오 프로젝트입니다.
