# Hotdeal Backend

커뮤니티 핫딜(뽐뿌, 에펨코리아, 어미새, 퀘이사존) 수집/조회 백엔드입니다.

## 실행
```bash
./gradlew bootRun
```

기본 포트: `8080`

## Swagger
- Swagger UI: `http://localhost:8080/swagger`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 주요 API
- `GET /api/deals?page=0&size=20&source=PPOMPPU&q=맥북`
- `GET /api/sources`
- `POST /api/admin/crawl`
- `POST /api/admin/crawl?source=FMKOREA`
- `GET /api/admin/crawl/runs?page=0&size=20&source=PPOMPPU&triggerType=SCHEDULED`

## 기능 명세
- [FEATURE_SPEC.md](./docs/FEATURE_SPEC.md)

## 환경 변수
- `DB_URL` (기본: `jdbc:postgresql://localhost:5432/hotdeal`)
- `DB_USERNAME` (기본: `postgres`)
- `DB_PASSWORD`
- `SERVER_PORT` (기본: `8080`)
- `CRAWLER_ENABLED` (`true`/`false`, 기본 `true`)
- `CRAWLER_FIXED_DELAY_MS` (기본 180000)
- `CRAWLER_INITIAL_DELAY_MS` (기본 10000)
- `CRAWLER_TIMEOUT_MS` (기본 20000)
- `CRAWLER_MAX_ITEMS_PER_SOURCE` (기본 60)
