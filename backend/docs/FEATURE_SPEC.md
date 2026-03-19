# Hotdeal Backend Feature Spec

## 1. 목적
- 뽐뿌, 에펨코리아, 어미새, 퀘이사존의 핫딜을 수집한다.
- 단일 API에서 통합 조회와 검색을 제공한다.
- 수집 실행 이력을 저장해 운영 상태를 확인할 수 있게 한다.

## 2. 도메인 기능
### 2.1 Deal 수집/중복 처리
- 식별키: `source_type + source_post_id`
- 동일 식별키가 없으면 신규 insert
- 동일 식별키가 있으면 변경 필드만 update (JPA dirty checking)
- 동시성 경합으로 중복 insert 시도 시:
  - DB unique constraint 위반을 잡아 재조회 후 update로 병합

### 2.2 Crawl 실행
- 수동 실행: `POST /api/admin/crawl`
  - `source` 미지정: 전체 소스 순회
  - `source` 지정: 단일 소스만 실행
- 스케줄 실행:
  - `app.crawler.enabled=true`일 때 고정 간격 실행
  - 이미 실행 중이면 다음 사이클 skip

### 2.3 Crawl 이력 저장
- 테이블: `crawl_runs`
- 저장 시점: 수동 실행/스케줄 실행 모두
- 저장 항목:
  - `source`, `requested_source`, `trigger_type`
  - `started_at`, `ended_at`
  - `fetched_count`, `inserted_count`, `updated_count`
  - `status`, `success`, `message`

## 3. API 명세
### 3.1 Deal 조회 API
- `GET /api/deals`
  - query:
    - `source` (optional)
    - `q` (optional)
    - `page` (default: 0, min: 0)
    - `size` (default: 20, min: 1, max: 100)

### 3.2 소스 요약 API
- `GET /api/sources`
  - 소스별 누적 건수 반환

### 3.3 수동 크롤링 API
- `POST /api/admin/crawl`
  - query:
    - `source` (optional)

### 3.4 크롤링 이력 조회 API
- `GET /api/admin/crawl/runs`
  - query:
    - `source` (optional)
    - `triggerType` (`MANUAL` | `SCHEDULED`, optional)
    - `page` (default: 0, min: 0)
    - `size` (default: 20, min: 1, max: 100)

## 4. Swagger/OpenAPI
- Swagger UI: `/swagger`
- OpenAPI JSON: `/v3/api-docs`
- 주요 컨트롤러에 `@Tag`, `@Operation` 적용

## 5. 환경 변수
- `DB_URL` (default: `jdbc:postgresql://localhost:5432/hotdeal`)
- `DB_USERNAME` (default: `postgres`)
- `DB_PASSWORD` (default: empty)
- `SERVER_PORT` (default: `8080`)
- `CRAWLER_ENABLED` (default: `true`)
- `CRAWLER_FIXED_DELAY_MS` (default: `180000`)
- `CRAWLER_INITIAL_DELAY_MS` (default: `10000`)
- `CRAWLER_TIMEOUT_MS` (default: `10000`)
- `CRAWLER_MAX_ITEMS_PER_SOURCE` (default: `60`)
