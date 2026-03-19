# Hotdeal Workspace

프로젝트는 프론트/백엔드를 분리한 구조입니다.

## 디렉토리 구조
- `frontend/`: React + TypeScript + Tailwind
- `backend/`: Spring Boot + JPA + 크롤러

## 실행 방법
### Backend
```bash
cd backend
./gradlew bootRun
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

## 참고 문서
- 백엔드 안내: [`backend/README.md`](./backend/README.md)
- 백엔드 기능 명세: [`backend/docs/FEATURE_SPEC.md`](./backend/docs/FEATURE_SPEC.md)
