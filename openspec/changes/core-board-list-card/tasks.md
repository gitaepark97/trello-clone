## 1. 프로젝트 셋업

- [x] 1.1 루트에 `backend/`(Spring Boot 4, Gradle, Java 17+) 프로젝트를 생성하고 `./gradlew build`가 성공하는지 확인한다 (언어는 별도 논의 전까지 Java로 가정 - Kotlin 전환은 이후 별도 결정 가능)
- [x] 1.2 루트에 `frontend/`(React + TypeScript, Vite 등) 프로젝트를 생성하고 `npm run build`(또는 동등 명령)가 성공하는지 확인한다
- [x] 1.3 로컬 개발용 PostgreSQL 실행 방법(docker-compose 등)을 구성하고, 컨테이너가 기동되어 접속 가능한지 확인한다
- [x] 1.4 backend에서 PostgreSQL 연결 설정(`application.yml` 등)을 구성하고 애플리케이션 기동 시 DB 연결에 성공하는지 확인한다

## 2. 데이터 모델 및 마이그레이션

- [x] 2.1 마이그레이션 도구(Flyway 등)를 backend에 추가하고 빈 마이그레이션이 정상 적용되는지 확인한다
- [x] 2.2 `board` 테이블 마이그레이션(`id`, `title`, `description`, `owner_id`(nullable), `created_at`, `updated_at`)을 작성하고 적용 후 스키마를 확인한다
- [x] 2.3 `list` 테이블 마이그레이션(`id`, `board_id`(FK, ON DELETE CASCADE), `title`, `position`(double), `created_at`, `updated_at`)을 작성하고 적용 후 스키마를 확인한다
- [x] 2.4 `card` 테이블 마이그레이션(`id`, `list_id`(FK, ON DELETE CASCADE), `title`, `description`, `position`(double), `created_at`, `updated_at`)을 작성하고 적용 후 스키마를 확인한다
- [x] 2.5 각 엔티티(JPA Entity 등)와 리포지토리를 구현하고, 저장/조회 단위 테스트가 통과하는지 확인한다

## 3. Board API 구현

- [x] 3.1 `POST /api/boards`(생성), `GET /api/boards`(목록 조회)를 구현하고 specs/board의 "보드 생성", "보드 목록 조회" 시나리오에 대응하는 통합 테스트가 통과하는지 확인한다
- [x] 3.2 `GET /api/boards/{boardId}`(단건 조회), `PATCH /api/boards/{boardId}`(수정)를 구현하고 specs/board의 "보드 단건 조회", "보드 수정" 시나리오(정상/실패 케이스 포함) 테스트가 통과하는지 확인한다
- [x] 3.3 `DELETE /api/boards/{boardId}`를 구현하고 specs/board의 "보드 삭제 시 하위 리스트/카드 함께 삭제" 시나리오가 통합 테스트로 검증되는지 확인한다 (cascade 동작 확인)

## 4. List API 구현

- [x] 4.1 `POST /api/boards/{boardId}/lists`(생성), `GET /api/boards/{boardId}/lists`(순서대로 목록 조회)를 구현하고 specs/list의 관련 시나리오 테스트가 통과하는지 확인한다
- [x] 4.2 `PATCH /api/lists/{listId}`(수정), `DELETE /api/lists/{listId}`(삭제, 하위 카드 cascade 포함)를 구현하고 specs/list의 관련 시나리오 테스트가 통과하는지 확인한다
- [x] 4.3 `PATCH /api/lists/{listId}/position`(같은 보드 내 순서 변경, gap-position 방식)을 구현하고 specs/list의 "리스트 순서 변경" 시나리오 테스트가 통과하는지 확인한다

## 5. Card API 구현

- [x] 5.1 `POST /api/lists/{listId}/cards`(생성), `GET /api/lists/{listId}/cards`(순서대로 목록 조회), `GET /api/cards/{cardId}`(단건 조회)를 구현하고 specs/card의 관련 시나리오 테스트가 통과하는지 확인한다
- [x] 5.2 `PATCH /api/cards/{cardId}`(수정), `DELETE /api/cards/{cardId}`(삭제)를 구현하고 specs/card의 관련 시나리오 테스트가 통과하는지 확인한다
- [x] 5.3 `PATCH /api/cards/{cardId}/position`(같은 리스트 내 순서 변경 및 다른 리스트로 이동, 요청 바디에 목적 리스트 id/목표 position 포함)을 구현하고 specs/card의 "같은 리스트 내 카드 순서 변경", "카드를 다른 리스트로 이동" 시나리오 테스트(원본/대상 리스트 양쪽 순서 일관성 포함)가 통과하는지 확인한다

## 6. 프론트엔드 - 보드 화면

- [ ] 6.1 보드 목록 페이지를 구현하고, 백엔드 `GET /api/boards`를 호출해 목록이 화면에 표시되는지 브라우저에서 확인한다
- [ ] 6.2 보드 생성/수정/삭제 UI(모달 또는 인라인 폼)를 구현하고, 각 동작이 API를 호출해 목록이 갱신되는지 확인한다
- [ ] 6.3 보드 상세 페이지(리스트들을 가로로 나열하는 레이아웃)를 구현하고 `GET /api/boards/{boardId}/lists`로 리스트가 순서대로 표시되는지 확인한다

## 7. 프론트엔드 - 리스트/카드 및 순서 변경

- [ ] 7.1 리스트 생성/수정/삭제 UI를 구현하고 API 연동 후 화면이 갱신되는지 확인한다
- [ ] 7.2 카드 생성/수정/삭제 UI를 구현하고 API 연동 후 화면이 갱신되는지 확인한다
- [ ] 7.3 드래그 앤 드롭 라이브러리(dnd-kit 등)를 도입해 같은 리스트 내 카드 순서 변경을 구현하고, 드롭 시 `PATCH /api/cards/{cardId}/position` 호출과 화면 순서 갱신이 정상 동작하는지 브라우저에서 확인한다
- [ ] 7.4 카드를 다른 리스트로 드래그해 이동하는 기능을 구현하고, 원본/대상 리스트 양쪽의 카드 목록이 올바르게 갱신되는지 브라우저에서 확인한다
- [ ] 7.5 리스트 순서 변경(리스트를 좌우로 드래그) 기능을 구현하고 `PATCH /api/lists/{listId}/position` 연동이 정상 동작하는지 브라우저에서 확인한다

## 8. 마무리 검증

- [ ] 8.1 backend 전체 테스트 스위트를 실행해 모두 통과하는지 확인한다
- [ ] 8.2 브라우저에서 보드 생성 → 리스트 생성 → 카드 생성 → 카드 이동 → 카드/리스트/보드 삭제까지 전체 플로우를 수동으로 한 번 실행해 정상 동작을 확인한다
