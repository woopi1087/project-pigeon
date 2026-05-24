# messagelog 패키지 — 발송 로그 DB 설계

## 개요

발송 요청·결과를 `message_logs` 테이블에 영속화한다.  
채널별 성공/실패 이력을 보존하고, 장애 분석·통계 용도로 활용한다.

---

## 테이블 구조

```sql
CREATE TABLE message_logs (
    id          BIGSERIAL PRIMARY KEY,
    channel     VARCHAR(20)  NOT NULL,          -- FCM | SMS | EMAIL | KAKAO
    recipient   VARCHAR(512) NOT NULL,          -- 수신자 식별자 (토큰 앞 20자 등 마스킹)
    title       VARCHAR(255),
    status      VARCHAR(10)  NOT NULL,          -- SUCCESS | FAIL
    message_id  VARCHAR(255),                   -- 채널 발급 messageId (FCM 등)
    error       TEXT,                           -- 실패 시 에러 메시지
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
```

### 인덱스

```sql
CREATE INDEX idx_message_logs_created_at ON message_logs(created_at DESC);
CREATE INDEX idx_message_logs_status     ON message_logs(status);
CREATE INDEX idx_message_logs_channel    ON message_logs(channel);
```

- `created_at DESC` — 최신 로그 조회, TTL 삭제 모두 이 컬럼 기준
- `status` — 실패 건만 필터링하는 모니터링 쿼리
- `channel` — 채널별 통계 집계

---

## 스키마 관리 — Flyway

스키마 변경은 **Flyway**로 관리한다. Hibernate `ddl-auto`는 `none`으로 고정.

### 마이그레이션 파일 위치

```
src/main/resources/db/migration/
  V1__init.sql       ← 최초 테이블 + 인덱스 생성
  V2__xxx.sql        ← 이후 변경사항 순차 추가
```

### 네이밍 규칙

```
V{버전}__{설명}.sql
```
- 버전은 정수 순서 (`V1`, `V2`, ...)
- 한 번 적용된 파일은 절대 수정 금지 → 새 파일로 변경사항 추가

### 프로파일별 동작

| 프로파일 | datasource | Flyway |
|----------|-----------|--------|
| `local`  | H2 in-memory (MODE=PostgreSQL) | `classpath:db/migration` 적용 |
| `dev`    | PostgreSQL (Docker) | `classpath:db/migration` 적용 |

> H2는 `MODE=PostgreSQL` 로 실행하므로 로컬에서도 동일한 SQL 파일로 테스트 가능.

---

## 현재 전략 (소규모)

> 초기 운영 단계. 인덱스 + TTL 배치로 충분히 대응 가능.

| 항목 | 설정 |
|------|------|
| 보존 기간 | **90일** |
| 삭제 방식 | `@Scheduled` 배치 — 매일 새벽 3시 실행 |
| 삭제 쿼리 | `DELETE FROM message_logs WHERE created_at < NOW() - INTERVAL '90 days'` |

---

## 단계별 마이그레이션 전략

### 단계 1 — 수백만 건 (현재)

**적용 중:** 인덱스 3개 + TTL 배치 삭제

추가 고려:
- `VACUUM ANALYZE message_logs` 주기적 실행 (dead tuple 정리)
- 삭제 배치를 작은 청크로 나눠 Lock 최소화

```sql
-- 한 번에 10,000건씩 삭제 (Lock 부담 분산)
DELETE FROM message_logs
WHERE id IN (
    SELECT id FROM message_logs
    WHERE created_at < NOW() - INTERVAL '90 days'
    LIMIT 10000
);
```

---

### 단계 2 — 수천만 건

**적용:** 월별 테이블 파티셔닝 (Range Partitioning)

```sql
-- 기존 테이블을 파티션 테이블로 전환
CREATE TABLE message_logs (
    id         BIGSERIAL,
    ...
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
) PARTITION BY RANGE (created_at);

-- 월별 파티션 생성
CREATE TABLE message_logs_2026_05
    PARTITION OF message_logs
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');

CREATE TABLE message_logs_2026_06
    PARTITION OF message_logs
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');
```

**장점:**
- 오래된 파티션을 `DROP TABLE message_logs_YYYY_MM` 한 방에 제거 → DELETE 수백만 건보다 압도적으로 빠름
- 쿼리 시 PostgreSQL이 해당 파티션만 스캔 (Partition Pruning)

**마이그레이션 방법 (무중단):**

```sql
-- 1. 새 파티션 테이블 생성
-- 2. 기존 데이터 INSERT INTO new_table SELECT * FROM old_table (배치)
-- 3. 트래픽 전환 (application.yaml datasource 변경)
-- 4. 기존 테이블 DROP
```

---

### 단계 3 — 억 건 이상

**옵션 A: 읽기 Replica 분리**

- Primary: 쓰기 전용 (`message_logs` INSERT)
- Replica: 모니터링·통계 조회 전용
- Spring DataSource routing으로 읽기/쓰기 분리

**옵션 B: TimescaleDB**

- PostgreSQL 확장으로 설치, 기존 쿼리 그대로 사용 가능
- `create_hypertable('message_logs', 'created_at')` 한 줄로 자동 청크 파티셔닝
- 자동 압축 정책 적용 시 스토리지 50~90% 절감
- 트레이드오프: 운영 복잡도 증가, Docker 이미지 변경 필요 (`timescale/timescaledb`)

**옵션 C: 외부 로그 플랫폼으로 이관**

- Loki + Grafana 또는 Elasticsearch + Kibana
- RDB 부담을 완전히 제거
- 트레이드오프: 인프라 추가

---

## 전환 판단 기준 (권장)

| 레코드 수 | 권장 전략 |
|-----------|-----------|
| ~ 100만 | 인덱스 + TTL 배치 (현재) |
| 100만 ~ 3,000만 | TTL 배치 + VACUUM 튜닝 |
| 3,000만 ~ 1억 | 월별 파티셔닝 전환 |
| 1억 이상 | TimescaleDB 또는 Replica 분리 |

> 현재 규모에서는 **단계 1** 이상 넘어갈 일이 거의 없다.  
> 파티셔닝은 초기 설계에 넣는 것보다, 필요 시점에 마이그레이션하는 게 오버엔지니어링을 피하는 현실적인 선택이다.
