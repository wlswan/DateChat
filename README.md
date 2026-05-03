# Date Server

한국-일본 데이팅 앱 백엔드 서버

시맨틱 캐싱을 적용한 번역 채팅 서버입니다.

---

## 시스템 아키텍처

```
┌──────────────────────────────────────────────────────────┐
│                         Client                            │
└────────────────────────┬─────────────────────────────────┘
                         │ REST API / WebSocket (STOMP over SockJS)
                         ▼
┌──────────────────────────────────────────────────────────┐
│                   Spring Boot Server                      │
│                                                           │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────┐  │
│  │  Auth (JWT) │  │  Like/Match  │  │  Chat (STOMP)   │  │
│  └──────┬──────┘  └──────┬───────┘  └────────┬────────┘  │
└─────────┼───────────────-┼──────────────────-┼───────────┘
          │                │                   │
    ┌─────▼──────┐   ┌─────▼──────┐     ┌──────┴─────────────────────────┐
    │   Redis    │   │   MySQL    │     │            RabbitMQ             │
    │ - Refresh  │   │ - User     │     │  ┌────────────────────────────┐ │
    │ - BlackList│   │ - Match    │     │  │ Translation Queue + DLQ    │ │
    └────────────┘   │ - ChatRoom │     │  └─────────────┬──────────────┘ │
                     └────────────┘     │  ┌─────────────▼──────────────┐ │
                                        │  │ STOMP Relay (/topic)       │ │
                                        │  └────────────────────────────┘ │
                          ┌─────────────┘  └──────────────┬───────────────┘
                          │                               │
                    ┌─────▼──────┐               ┌────────▼───────────┐
                    │  MongoDB   │               │ Translation Worker  │
                    │ - ChatMsg  │               │ OpenAI + Pinecone   │
                    └────────────┘               └────────────────────┘
```

## 기술 스택

| 분류 | 기술 |
|------|------|
| **Framework** | Spring Boot 4.0.3, Java 21 |
| **Database** | MySQL (사용자/매칭), MongoDB (채팅 메시지) |
| **Cache** | Redis (JWT 세션, 토큰 블랙리스트) |
| **Message Queue** | RabbitMQ (비동기 번역 처리 + STOMP Relay) |
| **Real-time** | WebSocket + STOMP |
| **AI/ML** | OpenAI API (번역), Pinecone (벡터 검색) |

---

## 주요 기능

### 1. Auth (인증)

JWT 기반 인증 시스템

- **Access Token**: 30분 만료
- **Refresh Token**: 7일 만료, HttpOnly 쿠키 저장
- **언어 설정**: 가입 시 KR/JP 선택 → 번역 방향 결정

### 2. Like & Matching (매칭)

스와이프 매칭

- **Discover**: 스와이프 대상 유저 조회 (이미 스와이프한 유저 제외)
- **스와이프**: LIKE / PASS 선택
- **자동 매칭**: 양쪽 모두 LIKE 시 Match 생성 → 채팅방 자동 생성
- **중복 방지**: user1_id + user2_id 유니크 제약

### 3. ChatRoom (채팅방)

Match와 ChatRoom을 별도 엔티티로 분리

- **Match**: 두 유저 간의 매칭 관계만 담당
- **ChatRoom**: 채팅 상태(ACTIVE/CLOSED), 나가기 등 채팅방 라이프사이클 담당
- **채팅방 생성**: 매칭 성공 시 자동으로 ChatRoom 생성
- **나가기**: ChatRoom status를 CLOSED로 변경, 이후 메시지 전송 불가

### 4. Chat (실시간 채팅)

WebSocket 기반 실시간 메시지 전송

- **프로토콜**: STOMP over WebSocket 
- **브로커**: RabbitMQ STOMP Relay — `/topic`, `/queue` 구독을 RabbitMQ가 중계하여 다중 서버 지원
- **인증**: STOMP CONNECT 시 JWT 검증, 세션에 userId/언어 저장
- **구독 권한 검증**: SUBSCRIBE 단계에서 채팅방 멤버 여부 및 CLOSED 상태 확인
- **예외 처리**: `StompErrorHandler`로 에러 프레임 변환 후 클라이언트 전달, `@Valid`로 입력값 검증
- **저장소**: MongoDB
- **커서 페이징**: ObjectId(String) 기반 무한 스크롤 (createdAt보다 정확한 순서 보장)
- **읽음 처리**: `MongoTemplate.updateMulti` 단일 쿼리로 일괄 처리

```
클라이언트 → WebSocket (STOMP) → JWT 인증 → ChatService → MongoDB 저장
                                                        → RabbitMQ STOMP Relay → 상대방에게 전송
                                                        → 번역 요청
```

### 4. Translation (자동 번역)

메시지 전송 시 상대방 언어로 자동 번역

#### 언어 정보 관리

- WebSocket 세션에 사용자 언어(userLang)와 상대방 언어(targetLang) 저장
- 메시지 전송 시 세션에서 언어 정보 조회하여 번역 방향 결정

#### 번역 흐름

```
1. 메시지 저장
2. 세션에서 언어 정보 조회 (userLang → targetLang)
3. 시맨틱 캐시 확인 (Pinecone)
   ├─ 캐시 히트 → 즉시 번역 결과 전송
   └─ 캐시 미스 → RabbitMQ로 번역 요청 발행
4. 워커 서버가 OpenAI API로 번역 후 RabbitMQ로 번역 내용 발행
5. 번역 결과를 받아 WebSocket으로 전송
```

#### 시맨틱 캐시 (Pinecone)

유사한 문장은 캐시에서 바로 번역 결과를 가져옵니다.

- **임베딩**: OpenAI text-embedding-3-small
- **벡터 DB**: Pinecone (코사인 유사도)
- **유사도 임계값**: 0.90 이상이면 캐시 히트

#### 번역 상태 (TranslationStatus)

| 상태 | 설명 |
|------|------|
| `PENDING` | 번역 요청 발행, 결과 대기 중 |
| `SUCCESS` | 번역 완료 |
| `FAILED` | 30초 경과한 PENDING 메시지를 스케줄러가 FAILED 처리 |

#### 번역 실패 처리

- **DB 폴링 스케줄러**: 5초 간격으로 PENDING 상태가 30초 이상 경과한 메시지를 조회해 FAILED로 변경
- **분산 락**: Redis `setIfAbsent`로 다중 서버 환경에서 스케줄러가 한 인스턴스에서만 실행되도록 보장
- **WebSocket 알림**: FAILED 상태는 DB에 저장되기 때문에 실시간 알림을 못 받아도 채팅방 재진입 시 확인 가능
- **재시도**: 클라이언트에서 실패한 메시지 재전송 요청 가능

---

## 환경 설정


#### 필수 환경 변수

```bash
PINECONE_API_KEY=your-pinecone-api-key
PINECONE_INDEX_NAME =your-pinecone-index-name
OPENAI_API_KEY=your-openai-api-key
```
#### 저장소

| 저장소 | 설명 |
|--------|------|
| [translation-worker](https://github.com/wlswan/translation-worker) | RabbitMQ 기반 번역 워커 서버 |


#### 초기 데이터 캐싱 데이터 추가 

```bash
curl -X POST http://localhost:8080/api/init/push \
  -H "Content-Type: application/json" \
  -d '[{"original": "안녕", "translated": "こんにちは", "sourceLang": "KR", "targetLang": "JP"}]'
```
