## Context

그린필드 프로젝트의 첫 변경으로, 이후 모든 기능(인증, 멤버십, 라벨 등)이 이 위에 쌓인다. 동기와 범위는 proposal.md 참고, 요구사항 상세는 specs/board, specs/list, specs/card 참고. 여기서는 세 엔티티(Board-List-Card)의 데이터 모델과 순서(position) 관리 방식, API 형태에 대한 기술적 결정을 다룬다.

백엔드는 Spring Boot 4(Gradle 빌드)로 확정했다. Spring Boot 4는 Spring Framework 7 기반으로 Java 17 이상을 요구하므로, 이후 설계·구현 결정은 이 baseline을 전제로 한다.

## Goals / Non-Goals

**Goals:**
- Board-List-Card 3단 계층을 위한 최소한이지만 확장 가능한 데이터 모델 정의
- 리스트/카드의 순서 변경(재정렬), 카드의 리스트 간 이동을 안정적으로 처리하는 방식 결정
- 이후 인증/멤버십 변경이 자연스럽게 확장할 수 있는 API·엔티티 형태 유지

**Non-Goals:**
- 인증/인가, 사용자 관리 (별도 변경에서 다룸)
- 실시간 동기화(WebSocket 등), 라벨/댓글/첨부파일
- 프론트엔드 상세 컴포넌트 설계 (별도 프론트엔드 변경에서 다룸 — 이번 변경은 API·도메인 계약 확정에 집중)

## Decisions

### 1. 순서(position) 표현 방식: 소수 기반 gap position
정수 인덱스 재정렬 대신 `position`을 double(부동소수점) 컬럼으로 두고, 두 항목 사이에 삽입할 때는 두 값의 중간값을 사용한다 (예: 1.0과 2.0 사이 삽입 시 1.5).
- **이유**: 카드/리스트를 한 칸 옮길 때마다 같은 부모의 모든 형제 항목의 position을 재계산(정수 재인덱싱)하지 않아도 되어, 이동 API가 O(1)로 동작한다. Trello 실제 구현도 유사한 방식을 사용한다.
- **대안 검토**: 정수 순번 + 이동 시 전체 재정렬 → 구현은 단순하지만 형제가 많을수록 매 이동마다 다수의 row를 갱신해야 해서 비효율적이고, 동시 이동 시 충돌 가능성도 커짐. 이번 단계에서는 기각.
- **후속 고려사항**: 소수 기반 방식은 반복적인 삽입으로 정밀도가 소진될 수 있으므로, 필요 시 주기적으로 간격을 재정규화(normalize)하는 로직을 추후 추가할 수 있다 (이번 변경 범위 밖).

### 2. 계층 구조와 참조 무결성: DB 레벨 cascade delete
`list.board_id`, `card.list_id`를 외래키로 두고 `ON DELETE CASCADE`를 적용한다. 보드 삭제 시 리스트→카드까지 DB가 연쇄 삭제를 보장하고, 애플리케이션 코드는 최상위 엔티티 삭제만 호출한다.
- **이유**: 애플리케이션 레벨에서 자식들을 순회하며 개별 삭제하는 것보다 일관성이 높고 구현이 단순하다.
- **대안 검토**: 소프트 삭제(soft delete, `deleted_at` 컬럼) → Trello의 "보관(archive)"과 더 가깝지만, 이번 변경의 스펙은 명시적으로 CRUD(하드 삭제)로 정의했으므로 범위 밖. 소프트 삭제/보관은 후속 변경에서 별도로 다룰 수 있다.

### 3. 소유자(ownerId) 필드는 nullable로 예약
인증이 아직 없으므로 `board` 테이블에 `owner_id` 컬럼을 nullable로 미리 추가해 두되, 이번 변경에서는 어떤 검증도 하지 않고 항상 null로 둔다.
- **이유**: 이후 사용자 인증 변경이 도입될 때 컬럼을 추가하는 마이그레이션 대신 NOT NULL 제약과 기본값만 채우는 마이그레이션으로 끝낼 수 있어, 스키마 변경 비용을 줄인다.
- **대안 검토**: 컬럼을 아예 두지 않고 이후 변경에서 추가 → 더 "순수"하지만, 이후 변경이 필연적으로 이 스키마를 확장할 것이 이미 확정적이므로 지금 예약해 두는 쪽을 선택.

### 4. API 리소스 형태: 중첩 경로 기반 REST
- `POST /api/boards`, `GET /api/boards`, `GET /api/boards/{boardId}`, `PATCH /api/boards/{boardId}`, `DELETE /api/boards/{boardId}`
- `POST /api/boards/{boardId}/lists`, `GET /api/boards/{boardId}/lists`, `PATCH /api/lists/{listId}`, `DELETE /api/lists/{listId}`, `PATCH /api/lists/{listId}/position` (순서 변경)
- `POST /api/lists/{listId}/cards`, `GET /api/lists/{listId}/cards`, `GET/PATCH/DELETE /api/cards/{cardId}`, `PATCH /api/cards/{cardId}/position` (같은 리스트 내 순서 변경 및 다른 리스트로 이동을 함께 처리 — 요청 바디에 목적 리스트 id와 목표 position을 포함)
- **이유**: 생성/목록 조회는 부모 컨텍스트가 필요하므로 중첩 경로를 쓰고, 단건 조회/수정/삭제/이동은 자원 자체의 전역 id로 바로 접근하는 절충안. 순수 중첩 경로(`/boards/{boardId}/lists/{listId}/cards/{cardId}`)보다 클라이언트가 다루기 쉽다.

## Risks / Trade-offs

- [소수 position의 정밀도 소진] → 초기 규모(개인/학습 프로젝트)에서는 실질적 위험이 낮음. 필요 시 재정규화 배치 로직을 후속 변경으로 추가.
- [CASCADE 삭제는 되돌릴 수 없음] → MVP 단계에서는 수용 가능한 트레이드오프로 명시. 실수 삭제 방지가 필요해지면 소프트 삭제/보관 기능을 후속 변경으로 도입.
- [owner_id를 미리 두지만 아직 사용하지 않음] → 데드 컬럼처럼 보일 수 있으나, 다음 변경(사용자 인증)에서 즉시 사용될 것이 확정적이므로 허용.
