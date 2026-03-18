# Date Server

한국-일본 데이팅 앱 백엔드 서버

시멘틱 캐싱을 적용한 번역 채팅 서버입니다. 

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| **Framework** | Spring Boot 4.0.3, Java 21 |
| **Database** | MySQL (사용자/매칭), MongoDB (채팅 메시지) |
| **Cache** | Redis (세션, Pub/Sub) |
| **Message Queue** | RabbitMQ (비동기 번역 처리) |
| **Real-time** | WebSocket + STOMP |
| **AI/ML** | OpenAI API (번역), Pinecone (벡터 검색) |

---

## 주요 기능

### 1. Auth (인증)

JWT 기반 인증 시스템

- **Access Token**: 30분 만료
- **Refresh Token**: 7일 만료, Redis 저장
- **언어 설정**: 가입 시 KR/JP 선택 → 번역 방향 결정

### 2. Like & Matching (매칭)

Tinder 스타일 스와이프 매칭

- **스와이프**: LIKE / PASS 선택
- **자동 매칭**: 양쪽 모두 LIKE 시 Match 생성 → 채팅방 자동 생성
- **중복 방지**: user1_id + user2_id 유니크 제약

### 3. Chat (실시간 채팅)

WebSocket 기반 실시간 메시지 전송

- **프로토콜**: STOMP over WebSocket
- **메시지 전파**: Redis Pub/Sub (다중 서버 대응)
- **저장소**: MongoDB (메시지 히스토리)
- **커서 페이징**: createdAt 기반 무한 스크롤 (복합 인덱스 사용)


```
클라이언트 → WebSocket → ChatService → MongoDB 저장
                                     → Redis Pub/Sub → 상대방에게 전송
                                     → 번역 요청
```

### 4. Translation (자동 번역)

메시지 전송 시 상대방 언어로 자동 번역

#### 번역 흐름

```
1. 메시지 저장
2. 시맨틱 캐시 확인 (Pinecone)
   ├─ 캐시 히트 → 즉시 번역 결과 전송
   └─ 캐시 미스 → RabbitMQ로 번역 요청 발행
3. 워커 서버가 OpenAI API로 번역 후 RabbitMQ로 번역 내용 발행
4. 번역 결과를 받아 WebSocket으로 전송
```

#### 시맨틱 캐시 (Pinecone)

유사한 문장은 캐시에서 바로 번역 결과를 가져옵니다.

- **임베딩**: OpenAI text-embedding-3-small
- **벡터 DB**: Pinecone (코사인 유사도)
- **유사도 임계값**: 0.90 이상이면 캐시 히트

---

## 환경 설정


### 필수 환경 변수

```bash
PINECONE_API_KEY=your-pinecone-api-key
PINECONE_INDEX_NAME =your-pinecone-index-name
OPENAI_API_KEY=your-openai-api-key
```
### 저장소

| 저장소 | 설명 |
|--------|------|
| [translation-worker](https://github.com/wlswan/translation-worker) | RabbitMQ 기반 번역 워커 서버 |


