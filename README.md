# project-pigeon

메시지 발송 전용 마이크로서비스. FCM(앱 푸시 알림)을 지원하며, 전략 패턴 기반으로 SMS / 이메일 / 카카오 채널을 쉽게 추가할 수 있도록 설계되어 있습니다.

---

## 패키지 구조

```
com.woopi.pigeon
├── channel/                  # 채널 전략 패턴
│   ├── ChannelType.kt        # 채널 enum (FCM, SMS, EMAIL, KAKAO)
│   ├── MessageChannel.kt     # 채널 인터페이스
│   └── fcm/
│       └── FcmChannel.kt     # FCM 구현체
├── config/
│   ├── FirebaseConfig.kt     # Firebase 초기화
│   └── SwaggerConfig.kt      # Swagger(OpenAPI) 설정
├── controller/
│   └── MessageController.kt  # POST /api/messages/send
├── dto/
│   ├── SendMessageRequest.kt # 발송 요청 DTO
│   └── ApiResponse.kt        # 공통 응답 래퍼
├── global/
│   └── GlobalExceptionHandler.kt
├── messagelog/               # 발송 로그 영속화
│   ├── MessageLogEntity.kt
│   ├── MessageLogRepository.kt
│   ├── MessageLogService.kt
│   ├── MessageLogStatus.kt   # SUCCESS / FAIL
│   └── README.md             # DB 전략 및 마이그레이션 가이드
└── service/
    └── MessageService.kt     # 채널 라우팅
```

---

## 로컬 실행

```bash
./gradlew bootRun   # http://localhost:8081
```

### 프로파일

| 프로파일 | DB | Swagger | Sentry |
|----------|-----|---------|--------|
| `local` (기본) | H2 in-memory | ✅ 활성 | ❌ 비활성 |
| `dev` | PostgreSQL (Docker) | ❌ 비활성 | ✅ 활성 |

### 주요 엔드포인트 (로컬)

| 용도 | URL |
|------|-----|
| Swagger UI | http://localhost:8081/swagger-ui.html |
| H2 Console | http://localhost:8081/h2-console |
| 헬스체크 | http://localhost:8081/actuator/health |

> H2 Console JDBC URL: `jdbc:h2:mem:pigeon`

### Docker로 로컬 실행 (PostgreSQL 포함)

```bash
cd docker/dev
cp .env.sample .env          # .env 편집 후 값 채우기
docker compose up -d
```

---

## API

### 메시지 발송

```
POST /api/messages/send
```

```json
{
  "channel": "FCM",
  "to": "fcm-device-token",
  "title": "분석 완료",
  "body": "등기부등본 분석이 완료되었습니다.",
  "data": {
    "jobId": "abc123"
  }
}
```

지원 채널: `FCM` | `SMS` | `EMAIL` | `KAKAO` (현재 FCM만 구현)

---

## 발송 로그 DB

발송 요청과 결과를 `message_logs` 테이블에 영속화합니다.  
스키마 관리는 **Flyway**로 처리합니다. (`src/main/resources/db/migration/`)  
DB 전략 및 대용량 마이그레이션 가이드 → [`messagelog/README.md`](src/main/kotlin/com/woopi/pigeon/messagelog/README.md)

---

## 새 채널 추가 방법

1. `ChannelType` enum에 값 추가
2. `MessageChannel` 인터페이스를 구현하는 클래스 작성 후 `@Component` 등록
3. 끝 — `MessageService`가 자동으로 채널을 탐색합니다

```kotlin
@Component
class SmsChannel : MessageChannel {
    override val channelType = ChannelType.SMS
    override fun send(request: SendMessageRequest) { /* 구현 */ }
}
```

---

## Firebase 서비스 계정 키 관리

> **주의: 서비스 계정 키는 민감한 자격증명입니다. 절대 git에 커밋하지 마세요.**

### 로컬 개발

1. Firebase 콘솔 → 프로젝트 설정 → 서비스 계정 탭
2. "새 비공개 키 생성" 클릭 → JSON 다운로드
3. 파일명을 `firebase-service-account.json`으로 변경
4. `src/main/resources/firebase-service-account.json`에 배치

팀원 간 공유는 노션, 1Password 등 안전한 채널을 이용하세요.

### 서버 배포 (CI/CD)

GitHub Actions가 `FIREBASE_SERVICE_ACCOUNT_JSON` 시크릿을 서버에 자동으로 배치합니다.  
`docker-compose.yml`에서 해당 파일을 컨테이너에 read-only 볼륨으로 마운트합니다.

```
GOOGLE_APPLICATION_CREDENTIALS=/app/config/firebase-service-account.json
```

---

## Sentry

에러 모니터링에 **Sentry**를 사용합니다.

| 환경 | 동작 |
|------|------|
| `local` | `sentry.dsn: ""` — 비활성 |
| `dev` | `SENTRY_DSN` 환경변수로 활성 |

### 빌드 시 소스 업로드 (Source Context)

Sentry Gradle 플러그인이 빌드 시 소스코드를 Sentry에 업로드합니다.  
GitHub Actions에서 `SENTRY_AUTH_TOKEN`이 설정되어 있을 때만 동작합니다.

**토큰 발급:** [https://woopii.sentry.io/settings/auth-tokens/](https://woopii.sentry.io/settings/auth-tokens/)  
**필요 권한:** Project `Read & Write`, Release `Read & Write`, Organization `Read`

---

## 배포

GitHub Actions (`deploy-pigeon-dev.yml`) — `develop` 브랜치 push 시 자동 배포.

### GitHub Secrets 등록 필요

**Repository secret:**

| 시크릿 이름 | 설명 |
|------------|------|
| `SENTRY_AUTH_TOKEN` | Sentry 소스 업로드용 토큰 (빌드 시 사용) |

**Environment(dev) secret:**

| 시크릿 이름 | 설명 |
|------------|------|
| `DEV_SSH_HOST` | 서버 IP |
| `DEV_SSH_USER` | SSH 유저명 |
| `DEV_SSH_PRIVATE_KEY` | SSH 개인키 |
| `DEV_SSH_PORT` | SSH 포트 (기본 22) |
| `FIREBASE_SERVICE_ACCOUNT_JSON` | Firebase 서비스 계정 JSON 전체 내용 |
| `DB_USERNAME` | PostgreSQL 유저명 |
| `DB_PASSWORD` | PostgreSQL 패스워드 |
| `SENTRY_DSN` | Sentry DSN (런타임 에러 수집) |
 