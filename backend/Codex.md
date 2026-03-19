# Codex Session Notes

## 프로젝트 개요
- 목적: 뽐뿌, 에펨코리아, 어미새, 퀘이사존 핫딜을 수집해 웹에서 통합 조회
- 백엔드: Spring Boot + JPA
- 프론트: React + TypeScript + Tailwind (`frontend/`)

## 이번 세션에서 한 작업
1. 크롤러 기반 수집 파이프라인 구현
- 사이트별 크롤러:
  - `src/main/java/com/web/hotdeal/crawler/impl/PpomppuCrawler.java`
  - `src/main/java/com/web/hotdeal/crawler/impl/FmKoreaCrawler.java`
  - `src/main/java/com/web/hotdeal/crawler/impl/EomisaeCrawler.java`
  - `src/main/java/com/web/hotdeal/crawler/impl/QuasarzoneCrawler.java`
- 실행 오케스트레이션:
  - `CrawlCoordinator`, `CrawlScheduler`, `DealIngestionService`, `CrawlController`

2. 조회 API 구현
- `GET /api/deals`
- `GET /api/sources`
- `POST /api/admin/crawl`

3. 프론트 추가
- `frontend/`에 React + TS + Tailwind 프로젝트 구성
- 검색/소스필터/페이징/자동새로고침 UI 구현

## 현재 코드 규칙(중요)
요청사항 반영:
- Entity에 Setter 사용 금지
- Entity 기본 생성자:
  - `@NoArgsConstructor(access = AccessLevel.PROTECTED)`
- DTO는 `record` 사용
- DTO <-> Entity 변환 책임은 DTO 메서드에서 처리
  - 예: `CrawledDeal.toEntity(...)`, `CrawledDeal.applyTo(...)`
  - 예: `DealItemResponse.from(...)`, `DealPageResponse.from(...)`
- Repository는 가능한 한 `@Query` 지양하고 derived query 사용

## 핵심 파일
- Entity:
  - `src/main/java/com/web/hotdeal/deal/Deal.java`
- Repository:
  - `src/main/java/com/web/hotdeal/deal/DealRepository.java`
- DTO:
  - `src/main/java/com/web/hotdeal/crawler/CrawledDeal.java`
  - `src/main/java/com/web/hotdeal/deal/dto/DealItemResponse.java`
  - `src/main/java/com/web/hotdeal/deal/dto/DealPageResponse.java`
  - `src/main/java/com/web/hotdeal/deal/dto/SourceSummaryResponse.java`

## 실행 방법
- 백엔드:
  - `./gradlew bootRun`
- 프론트:
  - `cd frontend`
  - `npm install`
  - `npm run dev`

## 다음 세션 우선 작업 제안
1. 크롤러별 파싱 안정화(셀렉터 변경 대응, 실패 재시도 강화)
2. 크롤링 로그 테이블/모니터링 추가
3. 프론트 정렬 옵션/상세 필터(가격, 카테고리) 추가
4. 테스트 보강(크롤러 파싱 단위 테스트, 서비스 테스트)

