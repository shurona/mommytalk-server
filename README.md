# line-message

라인 메신저 채팅을 실시간 수신하고 예약 메시지를 전송하는 Spring Boot 기반 어드민 페이지 프로젝트

## 💻 기술 스택

- Language: Java 21
- Framework: Spring boot 3.x
- Repository: Postgresql
- CI: GitHub Action
- Deploy: ECR, EC2

## ✈️ 실행 방법

### 아래 두 방법 중 하나를 사용한다.

- .env를 설정 후 docker-compose를 활용한다.
- .env.local을 설정해서 IDE를 활용해서 로컬로 실행시킨다.

## 🛵 주요 기능

- 세션을 이용한 인증 기능
- 라인에서 입력되는 메시지를 Hooking해서 등록
- TaskScheduler를 활용해서 예약 메시지 설정
- RestClient를 사용해서 Line에 메시지 전송 시스템 구현
- 주요 서비스 및 JPA에 테스트 설정
- Thymeleaf를 활용해서 mvc 구조로 개발

## 📠 트러블 슈팅

### 트랜잭션 커밋 시점 이전 스케줄 등록 이슈

트랜잭션 커밋과 관계없이 TaskScheduler를 통해 작업을 예약한 결과,  
커밋이 완료되기 전에 스케줄된 Task가 실행되어 DB에 저장되지 않은 데이터를 조회하려다 실패하는 문제가 발생했습니다.

이를 해결하기 위해 `TransactionSynchronizationManager.registerSynchronization`을 사용하여,  
트랜잭션 커밋 이후(afterCommit)에 TaskScheduler에 작업을 등록하도록 수정하였고,  
데이터 가시성 문제를 해결하고 시스템 안정성을 확보하였습니다.

### 서버와 클라이언트의 UTC 불일치 문제 발생

서버에서 LocalDateTime 기반 시간 처리 중 클라이언트-서버 간 TimeZone 차이로 인한 예약 시간 불일치 문제가 발생했습니다.

이를 해결하기 위해 서버 JVM 기본 TimeZone을 UTC로 고정하고, 클라이언트에서는 ZonedDateTime으로 시간대 정보를 포함해 전송하도록 수정했습니다.
서버 수신 후 필요한 경우 ZoneId.systemDefault()로 변환하여 서버 로컬 시간 기준으로 처리함으로써 전 세계 사용자에 대해 정확한 예약 시간 처리를 보장하였습니다.