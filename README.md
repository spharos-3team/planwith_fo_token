# Release Note / RM — planwith-fo-token

## 1. 서비스 개요

| 항목 | 내용 |
|------|------|
| 서비스명 | `planwith-fo-token` |
| 레포 | `planwith_fo_token` |
| 포트 | `8084` |
| Eureka / Compose / ECR | `planwith-fo-token` |
| 패키지 | `com.planwith.planwith_fo_token` |
| DB | `token_db` |
| 역할 | 토큰 지갑·원장, 카드/결제(PortOne), 충전·지급·사용·만료, 등급 FREE·최초 BONUS 연동, Outbox/Kafka |

토큰과 실제 금액을 다루는 도메인으로, **Wallet/Ledger 안전 구조 → 결제 → 등급/이벤트** 순으로 구현되었습니다.

---

## 2. 도메인 범위

### 2.1 Token Wallet / Ledger

- 토큰 종류: `PAID` / `FREE` / `BONUS`
- 거래 유형: `CHARGE` / `USE` / `REWARD` / `EXPIRE`
- 사용처: `PAYMENT`, `AI_SCHEDULE`, `IMPORT_SCHEDULE`, `PDF_DOWNLOAD`, `GRADE_REWARD`
- 차감 우선순위: **FREE → BONUS → PAID**
- FREE: 월간 등급 지급 전 EXPIRE
- BONUS: Stage1 자동 만료 없음
- PAID: 만료 없음
- append-only Ledger + 회원 단위 Wallet Lock(멱등 `transactionUuid`)

### 2.2 Payment Method (카드)

- PortOne BillingKey 발급·등록
- 다중 카드, 기본카드 설정/변경, soft delete
- 삭제(비활성) 카드 결제 차단

### 2.3 Token Product / Charge / Payment

- 상품(서버 정책 고정): `TRIAL` / `BASIC` / `POPULAR` / `LARGE`
- 결제 유형: `ONE_TIME` / `BILLING_KEY`
- 충전 상태: `READY` → `PAID` / `FAILED` / `CANCELED`
- 흐름: 상품 조회 → 충전 요청 → pay / confirm → PAID 토큰 지급
- PG 재조회 검증 후 지급, 금액 불일치·중복 요청 처리

### 2.4 Grade Token

- 월간 FREE: Kafka `planwith.grade.reward-granted` 구독
- FREE는 `eventUuid` / 회원·월(`yyyy-MM`) 멱등 지급
- FREE 지급 전 기존 FREE EXPIRE → 신규 FREE REWARD
- 최초 등급 BONUS: Kafka `planwith.grade.initial-bonus-granted` 구독
- BONUS는 회원 기준 결정적 `transactionUuid`로 최초 1회만 지급
- BONUS 필수 payload: `eventUuid`, `memberUuid`, 양수 `tokenAmount`
- BONUS 선택 payload: `gradeCode`, ISO-8601 `grantedAt`
- Grade DB 직접 조회 없음(이벤트만)

### 2.5 Outbox / Kafka

- Wallet·Ledger·Outbox 동일 트랜잭션
- Relay → Kafka → `publishedAt`
- 발행 이벤트: `TokenCharged` / `TokenUsed` / `TokenRewarded` / `TokenExpired` / `TokenChargeFailed`
- 수신: `GradeRewardGranted`, `GradeInitialBonusGranted`, `PaymentCompleted`

### 2.6 실패 복구 / 정합성

- READY 미지급 충전 reconcile (스케줄러 + API)
- Wallet vs Ledger 정합성 검증 API

---

## 3. API 그룹

| 구분 | Prefix |
|------|--------|
| Deploy / Login | `/api/planwith-fo-token` |
| Token Command | `/api/planwith-fo-token/members/{memberUuid}/tokens` |
| Token Query | balance / ledger / charges |
| Payment Method | `.../payment-methods` |
| Charge Lifecycle | products / charges / pay / confirm / reconcile / consistency |
| Internal | `/internal/planwith-fo-token/v1/.../balance` |

---

## 4. 외부 연동

| 시스템 | 내용 |
|--------|------|
| PortOne | BillingKey 발급, 결제, 조회, 취소 (`stub` 지원) |
| Kafka | 등급/결제 수신, 토큰 상태 이벤트 발행 |
| Grade | `GradeRewardGranted`, `GradeInitialBonusGranted` 소비 |
| Gateway | 진입점 `:8000` (JWT는 Gateway 담당 예정, Token에는 미구현) |

---

## 5. 비기능 / 품질

- Hexagonal(Domain / Application Port / Adapter)
- Transactional Outbox + Relay retry/backoff
- 회원 Wallet Pessimistic Lock, Ledger·이벤트 멱등
- Domain / Integration / Concurrency / Outbox / E2E 테스트 구성
- 예외 코드: `TOKEN_INSUFFICIENT`, `PAYMENT_FAILED`, `PAYMENT_AMOUNT_MISMATCH` 등

---

## 6. 배포 설정 요약

| 항목 | 기본값 | 비고 |
|------|--------|------|
| Kafka Consumer | OFF | env로 ON |
| Outbox Relay | OFF | env로 ON |
| Charge Reconcile | OFF | env로 ON |
| PortOne Stub | OFF | local은 stub ON |
| Eureka | ON | local OFF |
| DDL | `update` | local `validate` |

서버 env: `planwith-infra/env/token.env` (+ `common.env`)

---

## 7. 운영 주의사항

1. 기존 DB Outbox payload가 TINYTEXT인 경우:

```sql
ALTER TABLE token_outbox MODIFY COLUMN payload TEXT NOT NULL;
```

2. Kafka 사용 시 `KAFKA_BOOTSTRAP_SERVERS`를 Docker 호스트명으로 설정 (컨테이너에서 `localhost` 금지)
3. 외부 호출은 Gateway `:8000` 경유, 서비스 포트 직접 노출 금지
4. JWT 검증은 Token이 아닌 Gateway에서 수행 예정

---

## 8. 개발 완료 범위 (단계 요약)

```
01~09  Token Domain / Persistence / 지급·차감 / 동시성 / Outbox
10~15  PaymentMethod / 상품·Charge / ONE_TIME·BILLING_KEY / 검증·PAID 지급
16~19  Grade FREE / FREE·BONUS 정책 / Kafka / 실패 복구·정합성
20     단위·인프라·통합·E2E 테스트
+      테스트 후 Outbox TEXT 매핑 / 선택 인프라 기본 OFF / Clock 생성자 정리
```

---

## 9. 검증 상태

- [x] Domain / Unit Test
- [x] Integration Test (결제·동시성·Outbox·등급·정합)
- [x] E2E Lifecycle Test (카드→결제→지급→사용→만료·등급)
- [ ] Gateway JWT 연동 (별도 과제)
- [ ] 운영 Kafka/Outbox 활성화 (인프라 준비 후 env)

---

**RM 결론:** `planwith-fo-token`은 토큰 원장·결제·등급 FREE·이벤트 발행까지 기능 개발이 완료된 상태이며, 운영에서는 PortOne 실키·DB ALTER·(필요 시) Kafka/Outbox env ON·Gateway 라우팅만 맞추면 됩니다.
