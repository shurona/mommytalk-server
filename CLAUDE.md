# CLAUDE.md

이 파일은 이 저장소에서 코드 작업을 할 때 Claude Code (claude.ai/code)에게 가이드를 제공합니다.

Behavioral guidelines to reduce common LLM coding mistakes. Merge with project-specific instructions as needed.

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

## 빌드 및 개발 명령어

이 프로젝트는 Java 21과 Spring Boot 3.4.5를 사용하는 Gradle 빌드 도구를 사용합니다.

### 빌드 및 실행

- 빌드: `./gradlew build`
- 클린 빌드: `./gradlew clean build` (clean 시 QueryDSL 생성 파일 `src/main/generated` 삭제)
- 로컬 실행: `./gradlew bootRun --args='--spring.profiles.active=local'`
- 테스트 실행: `./gradlew test`
- 특정 테스트 클래스 실행: `./gradlew test --tests "com.shrona.mommytalk.SomeTest"`

### 개발 환경 설정

두 가지 배포 옵션이 가능합니다:

- Docker: `.env` 파일 설정 후 `docker-compose up` 실행
- 로컬 IDE: `.env.local` 파일 설정 후 IDE에서 local 프로필로 실행
- 기본 로컬 포트: 19100

### 환경 설정 파일

- 운영 프로필 (인스턴스별 분리):
    - `application-prod-admin.yaml`: 어드민 인스턴스 (TaskScheduler **활성화**)
    - `application-prod-webapp.yaml`: 웹앱 인스턴스 (TaskScheduler **비활성화**)
    - 예약 메시지 스케줄러는 admin 인스턴스에서만 실행됨 (`spring.task.scheduling.enabled`로 제어)
- 로컬 프로필: `application-local.yaml`
- 테스트 프로필: `src/test/resources/application.yaml`
- 모니터링: Spring Actuator + Prometheus (별도 포트로 분리)

## 아키텍처 개요

LINE 메신저 통합 기능과 관리자 대시보드를 제공하는 Spring Boot 웹 애플리케이션입니다.

### 핵심 도메인 모듈

- **admin**: 관리자 사용자 관리 및 인증 (JWT 기반 세션 사용)
- **user**: 사용자 및 그룹 관리 (한국/미국/영국 전화번호 지원)
- **group**: 그룹 관리 (사용자 그룹화 및 메시지 타겟팅)
- **message**: 플랫폼 독립적 메시지 관리 (레벨링, 예약, 주제 관리)
- **channel**: 멀티채널 관리 (LINE, Kakao 채널별 설정 및 사용자 관리)
- **line**: LINE 플랫폼 통합 (사용자, 메시지 전송)
- **linehook**: 수신되는 LINE 메시지 웹훅 처리
- **kakao**: 카카오톡 통합 모듈 (NHN Cloud 브랜드 메시지 API 사용)
- **kakaohook**: 카카오톡 웹훅 처리 (구현 예정)
- **auth**: 인증 및 권한 부여 서비스
- **openai**: OpenAI API 통합 및 프롬프트 관리 (채널별 AI 메시지 생성)
- **elevenlabs**: ElevenLabs 음성 생성 API 통합
- **cloudflare**: Cloudflare R2 스토리지 통합 (오디오 파일 저장)
- **entitlement**: 권한 및 기능 타입 관리 (MOMMYTALK, MOMMYVOCA 등)
- **common**: 공유 유틸리티, 설정 및 기본 엔티티

### 주요 아키텍처 패턴

- **도메인 주도 설계(DDD)**: 각 모듈은 domain/application/infrastructure/presentation 레이어 구조를 따름
- **JPA + QueryDSL**: 타입 안전한 쿼리로 데이터베이스 접근
- **MVC + Thymeleaf**: 관리자 인터페이스용 서버사이드 렌더링
- **TaskScheduler 통합**: 트랜잭션 동기화를 통한 예약 메시지 배송
- **세션 기반 인증**: Spring Security 없이 커스텀 LoginInterceptor 사용

### 데이터베이스 및 영속성

- 운영환경: PostgreSQL, 테스트: H2
- JPA 엔티티는 감사를 위해 BaseEntity 확장
- 복잡한 쿼리용 QueryDSL (생성된 클래스는 `src/main/generated`)
- 커스텀 PhoneNumberConverter(`user/domain/converter`)를 사용한 전화번호 저장 (한국/미국/영국 형식 지원)
- Google libphonenumber를 사용한 전화번호 파싱 및 국가 코드 자동 감지 (예: KakaoAuthService)

### Message 도메인 아키텍처

- **주제 기반 배송**: 일일 하나의 주제(`MessageType`)로 구성된 메시지 시스템
- **레벨 기반 개인화**: childLevel(1-3) × userLevel(1-3) = 9가지 조합으로 개인화된 메시지 전송
- **예약 배송**: `LocalDate` 기반 일정 관리 및 자동 배송
- **링크 지원**: `headerOneLink`, `headerTwoLink`를 통한 컨텐츠 연결
- **플랫폼 독립성**: LINE, 카카오톡 등 다양한 플랫폼에서 사용 가능한 추상화된 메시지 관리

### LINE 통합 아키텍처

- **웹훅 처리**: `/linehook` 엔드포인트를 통한 LINE 이벤트 수신
- **메시지 전송**: RestClient 기반 LINE API 통합
- **채널 관리**: 채널별 사용자가 있는 멀티채널 지원
- **Message 도메인 연동**: 플랫폼 독립적 메시지를 LINE 포맷으로 변환 및 전송

### Kakao 통합 아키텍처

- **NHN Cloud 알림톡 단일 발송 경로**: `KakaoAlimtalkSenderImpl` (브랜드 메시지 구현체는 미사용으로 삭제됨 — 광고성 메시지가 필요해지면 git 히스토리에서 참고)
- **전화번호 기반**: LINE과 달리 전화번호로 사용자 식별 및 메시지 전송 (한국/미국/영국 지원)
- **유저별 선호 발송 시간**: `User.preferredSendTime`(KST, 기본 10:00, 24시간 제한 없음)에 맞춰 발송
    - `KakaoMessageScheduler`가 매시 25분/55분(KST) 폴링 → 다음 30분 윈도우 내 대상만 NHN에 예약 접수 (`requestDate`)
    - 실제 발송 시각은 NHN API 예약이 보장, 놓친 건은 다음 폴링에서 자동 처리 (상태 기반 중복 방지)
    - 접수 결과는 청크 단위로 새 트랜잭션 커밋 (장애 중단 시 중복 접수 방지)
    - 킬 스위치: `kakao.polling-scheduler.enabled=false`
    - LINE과 달리 TaskScheduler 개별 태스크를 사용하지 않음 (LINE은 기존 방식 유지)
- **버튼 메시지**: EntitlementType에 따라 템플릿/링크 버튼 자동 선택 (일요일은 리뷰 템플릿)
- **개인화 메시지**: {아이이름} 템플릿 변수를 통한 수신자별 메시지 개인화
- **야간 전송 제한 없음**: 알림톡(정보성)은 NHN 야간 제한이 없음. 20:50-08:00 제한은 브랜드 메시지/친구톡(광고성)에만 적용
- **상세 가이드**: [KAKAO_SETUP_GUIDE.md](docs/legacy/KAKAO_SETUP_GUIDE.md) 참조

### ElevenLabs 통합 아키텍처

- **음성 생성 API**: ElevenLabs API를 통한 텍스트-음성 변환
- **채널별 Voice ID**: Channel 엔티티에 ElevenLabs voiceId 저장
- **미디어 관리**: ElevenLabsMedia 엔티티로 생성된 오디오 메타데이터 관리
- **Cloudflare R2 연동**: 생성된 오디오 파일을 R2 스토리지에 업로드
- **DDD 아키텍처**: domain/application/infrastructure/presentation 레이어 분리

### Cloudflare R2 스토리지 아키텍처

- **AWS S3 호환 API**: S3 SDK를 사용한 R2 스토리지 통합
- **오디오 파일 저장**: ElevenLabs 생성 음성 파일 영구 저장
- **자동 경로 생성**: `audio/YYYY/MM/filename.mp3` 형식으로 자동 정리
- **공개 URL 제공**: 업로드된 파일의 공개 접근 URL 반환
- **상세 가이드**: [CLOUDFLARE_R2_SETUP.md](docs/legacy/CLOUDFLARE_R2_SETUP.md) 참조

### Entitlement 시스템

- **기능 타입 관리**: 서비스별 권한 및 기능 분류 (EntitlementType Enum)
- **주요 타입**:
    - `MOMMYTALK`: 일반 마미톡 메시지 (텍스트 메시지)
    - `MOMMYVOCA`: 마미톡 보카 메시지 (버튼 링크 포함)
- **메시지 형식 분기**: EntitlementType에 따라 메시지 형식 자동 변경
    - MOMMYTALK → 단순 텍스트 메시지
    - MOMMYVOCA → 링크 버튼 포함 메시지 (headerOneLink, headerTwoLink 사용)

### OpenAI 통합 아키텍처

- **AI 메시지 생성**: 마미톡잉글리시 전용 프롬프트를 통한 레벨별 영어 학습 컨텐츠 생성
- **채널별 프롬프트 관리**: MessagePrompt 엔티티와 Channel의 N:1 관계로 채널별 맞춤 프롬프트 지원
- **RestClient 통합**: Spring Web Service 기반 OpenAI API 클라이언트 구현
- **DDD 아키텍처**: domain/application/infrastructure/presentation 레이어 분리
- **프롬프트 템플릿**: 부모/아이 레벨(1-3)에 따른 9가지 조합의 영어 스크립트 자동 생성

### 중요한 기술적 세부사항

- **시간대 처리**: 서버는 UTC로 실행 (MommyTalkServerApplication.java:17에서 설정)
- **인스턴스 분리 배포**: 운영환경은 admin/webapp 두 인스턴스로 분리 배포되며, 예약 메시지 스케줄러는 admin 인스턴스에서만 동작 (`spring.task.scheduling.enabled`)
- **트랜잭션 동기화**: 데이터 가시성 문제를 피하기 위해 트랜잭션 커밋 후 예약 작업 등록
- **커스텀 인터셉터**: 인증용 LoginInterceptor, 멀티테넌시용 ChannelIdInterceptor
- **요청 본문 캐싱**: 웹훅 서명 검증을 위한 CachedBodyHttpServletRequest

### 테스트 전략

- 서비스 및 리포지토리 단위 테스트
- H2 인메모리 데이터베이스를 사용한 통합 테스트
- 테스트 클래스 명명 규칙: `*Test.java`
- 메인 패키지 구조를 미러링한 `src/test/java` 위치

### 프론트엔드 구조

- `src/main/resources/templates`의 Thymeleaf 템플릿
- `src/main/resources/static`의 정적 자산(CSS/JS)
- 사이드바 네비게이션이 있는 프래그먼트 기반 레이아웃

## 플랫폼 운영 전략

- **채널별 단일 플랫폼**: 하나의 Channel은 LINE 또는 Kakao 중 하나만 지원
- **사용자 플랫폼 감지**: User 엔티티의 lineUser/kakaoUser 존재 여부로 전송 플랫폼 자동 판단
- **Message 도메인 중심**: 플랫폼 독립적 메시지 설계로 확장성 확보
- **레벨 기반 개인화**: childLevel/userLevel 시스템을 모든 플랫폼에서 공통 사용

## 외부 서비스 설정 가이드

상세한 설정 가이드는 `docs/legacy/`에 별도 문서로 제공됩니다 (진행 중인 작업 계획 문서는 `docs/plans/` 참조):

- **[KAKAO_SETUP_GUIDE.md](docs/legacy/KAKAO_SETUP_GUIDE.md)**: NHN Cloud 카카오톡 브랜드 메시지 API 설정, 예약 전송 플로우, 버튼/개인화 메시지, 트러블슈팅
- **[CLOUDFLARE_R2_SETUP.md](docs/legacy/CLOUDFLARE_R2_SETUP.md)**: Cloudflare R2 버킷 생성, API 토큰, 음성 파일 업로드 플로우
- **[LINE_USER_REGISTRATION_LOGIC.md](docs/legacy/LINE_USER_REGISTRATION_LOGIC.md)**: LINE 사용자 등록 로직 설명

### 전화번호 저장 형식

| 국가 | 국가코드 | 저장 형식 | 예시 |
|------|----------|-----------|------|
| 한국 | +82 | `XXX-XXXX-XXXX` | `010-1234-5678` |
| 미국/캐나다 | +1 | `1-XXX-XXX-XXXX` | `1-650-123-4567` |
| 영국 | +44 | `44-XXX-XXX-XXXX` | `44-791-112-3456` |

## Message Content 관리 API

Message 도메인 중심의 AI 기반 콘텐츠 생성/관리 REST API (`/v1/channels/{channelId}/...`):

- **콘텐츠 생성** (`POST .../contents/generate`): OpenAI로 레벨 조합(childLevel × userLevel = 9가지)별 콘텐츠 생성. `regenerate=true` 시 기존 컨텐츠 재생성, `false` 시 기존 반환
- **콘텐츠 수정** (`PUT .../contents/{contentId}`): messageText, diaryUrl 선택적 업데이트
- **콘텐츠 승인** (`PATCH .../contents/{contentId}/approve`): `MessageContent.approve()`는 이미 승인된 경우 `false`를 반환하여 불필요한 DB 업데이트 방지
- **발송 가능 날짜 조회** (`GET .../messages/available-dates`): 9개 레벨 조합이 모두 `approved=true`인 날짜만 반환 (오늘부터 14일, QueryDSL)

주요 엔티티 관계: `MessageType` → `Channel` (N:1), `MessagePrompt` → `Channel` (N:1, 채널별 AI 프롬프트 관리)

OpenAI 설정 요구사항:

```yaml
openai:
  base-url: https://api.openai.com/v1
  openApiKey: ${OPEN_AI_KEY}
```

## 코드 컨벤션

이 프로젝트는 일관된 코드 스타일과 구조를 유지하기 위해 다음 규칙을 따릅니다.

### 1. 네이밍 컨벤션

- **Request/Response DTO**: `{Domain}{Action}RequestDto` / `{Domain}{Action}ResponseDto` — REST API용, `record` 타입 (예: `AdminLoginRequestDto`, `MessageLogResponseDto`)
- **Form**: `{Domain}Form` — Thymeleaf 뷰 바인딩용, `record` 타입 (예: `MessageSendForm`)
- **RequestBody/ResponseBody**: `{Action}RequestBody` 등 — 외부 API(LINE, Kakao, OpenAI) 통신용 (예: `LineMessageMulticastRequestBody`)
- **Service 메서드**: 조회 `find~`/`get~`, 생성 `create~`, 수정 `update~`, 삭제 `delete~`
- **Repository**: Spring Data JPA는 `{Entity}JpaRepository`, QueryDSL 동적 쿼리는 `{Entity}QueryRepository` + `{Entity}QueryRepositoryImpl`
- **예외**: `{Domain}Exception` (extends RuntimeException) + `{Domain}ErrorCode` Enum (예: `MessageException`, `MessageErrorCode`)

### 2. DDD 레이어 구조

각 도메인 모듈은 다음 레이어 구조를 **철저히 준수**합니다:

```
{domain}/
├─ domain/              # 핵심 비즈니스 로직
│  ├─ {Entity}.java
│  ├─ vo/              # Value Object
│  └─ type/            # Enum
├─ application/         # 서비스 레이어
│  ├─ {Domain}Service.java
│  └─ {Domain}ServiceImpl.java
├─ infrastructure/      # 외부 시스템 통신
│  ├─ repository/
│  │  ├─ jpa/          # Spring Data JPA
│  │  └─ query/        # QueryDSL
│  ├─ adapter/         # 외부 API Bean 설정
│  ├─ sender/          # 외부 API 클라이언트
│  └─ dao/             # DTO Projection
├─ presentation/        # 컨트롤러 레이어
│  ├─ controller/      # REST API
│  ├─ mvc/             # MVC 컨트롤러
│  ├─ dtos/            # Request/Response DTO
│  │  ├─ request/
│  │  └─ response/
│  └─ form/            # MVC Form
└─ common/              # 도메인별 공통
   └─ exception/
```

### 3. Java Record 타입 사용

- **모든 DTO/Form은 `record` 타입 사용** (Lombok `@Getter` 클래스 방식 지양)
- 필요 시 record 안에 정적 팩토리 메서드(`of(...)`) 정의
- **예외**: 복잡한 로직이 필요한 Domain Entity는 클래스 사용

### 4. Service 패턴

- **인터페이스 + 구현체 분리**: `{Domain}Service` 인터페이스 + `{Domain}ServiceImpl` 구현체 (`@Service`, `@RequiredArgsConstructor`)
- **메서드 파라미터 순서**: ① 도메인 엔티티 (Channel, User 등) → ② 비즈니스 데이터 (ID 목록, 날짜 등) → ③ 옵션/플래그 (content, flag 등)

### 5. 날짜/시간 처리

- **저장/비즈니스 로직**: `LocalDateTime`, `LocalDate`
- **API 통신**: 타임존 정보가 필요하면 `ZonedDateTime`으로 받고, `withZoneSameInstant(ZoneId.systemDefault())`로 서버(UTC) 시간으로 변환 후 `LocalDateTime`으로 처리
- **서버 기본 타임존**: UTC (`MommyTalkServerApplication.java`에서 설정)

### 6. 주석

- 메서드 설명은 한 줄 JavaDoc, 비즈니스 로직 설명은 한글 인라인 주석 허용
- 코드만으로 명확한 경우 주석 생략

### 7. 테스트 컨벤션

- **클래스명**: `{TargetClass}Test` (예: `MessageServiceImplTest`)
- **메서드명**: 한글 허용 (예: `public void 메시지_저장_테스트()`)
- **Given-When-Then 구조** 사용 (`// given`, `// when`, `// then` 주석으로 구분)

### 8. 예외 처리

- 도메인별 Custom Exception 사용: `{Domain}Exception`은 `{Domain}ErrorCode` Enum을 받아 생성 (예: `throw new MessageException(MESSAGE_NOT_SCHEDULED_FOR_DATE)`)

### 9. QueryDSL N+1 방지

- 연관 엔티티 조회 시 `leftJoin(...).fetchJoin()` 적극 활용 (예: `MessageLogQueryRepositoryImpl`)

### 10. 정적 팩토리 메서드

- Entity 생성 시 생성자 대신 정적 팩토리 메서드 사용 (예: `MessageContent.of(type, content, childLevel, userLevel)`)