# config 패키지 — 애플리케이션 설정

## FirebaseConfig

Firebase Admin SDK 초기화를 담당합니다.

### 자격증명 우선순위

1. `GOOGLE_APPLICATION_CREDENTIALS` 환경변수 → 해당 경로의 JSON 파일 사용
2. 환경변수 미설정 시 → `classpath:firebase-service-account.json` 폴백

| 환경 | 자격증명 방식 |
|------|-------------|
| 로컬 | `src/main/resources/firebase-service-account.json` 직접 배치 |
| dev (Docker) | `/app/config/firebase-service-account.json` 볼륨 마운트 + `GOOGLE_APPLICATION_CREDENTIALS` 환경변수 |

> 서비스 계정 키 발급 및 관리 방법 → 루트 `README.md` Firebase 섹션 참고

---

## SwaggerConfig

OpenAPI 3.0 문서를 설정합니다.

### 프로파일별 활성화 여부

| 프로파일 | Swagger UI | API Docs |
|----------|-----------|----------|
| `local` | ✅ `/swagger-ui.html` | ✅ `/v3/api-docs` |
| `dev` | ❌ 비활성 | ❌ 비활성 |

> dev 환경에서는 `application-dev.yaml`의 `springdoc.swagger-ui.enabled: false` 설정으로 비활성화됩니다.
