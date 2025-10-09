# Globoo Backend

Spring Boot 기반 HUFS 언어교류 서비스 백엔드

## Tech Stack

* **Language**: Java 17
* **Framework**: Spring Boot 3.5.6
* **Build**: Gradle (Wrapper)
* **DB**: MySQL
* **Docs**: Springdoc OpenAPI (Swagger UI)

## Prerequisites

* JDK 17 (Temurin 권장)
* MySQL 8.x
* Git

## Project Structure

```
com.Globoo
 ├─ auth       # 로그인/회원가입, 토큰, 이메일 인증
 ├─ user       # 마이페이지, 프로필 조회
 ├─ message    # 쪽지(1:1 DM)
 ├─ matching   # 랜덤매칭
 ├─ chat       # WebSocket/STOMP 채팅
 ├─ study      # 스터디 모집(게시글/댓글)
 ├─ storage    # 파일/이미지 업로드
 └─ common     # 예외/응답/JWT/Swagger/유틸
```

## Ownership (Module → Owner)

* `auth` – 혜송
* `user` – 혜송 / (프로필 조회: 예은)
* `message` – 예은
* `matching` – 동한
* `chat` – 진수
* `study` – 게시글: 진수 / 댓글: 동한
* `common` – 혜송
* `storage` – 혜송

## Environment

* 기본 포트: `8080`
* 프로필: `dev` / `prod` (공통 `application.yml`에서 `active=dev`)

MySQL DB 생성:

```sql
CREATE DATABASE IF NOT EXISTS globoo
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
```

## Configuration

* 개발:

  * `spring.jpa.hibernate.ddl-auto=update`
  * `spring.sql.init.mode=never` (SQL 스크립트 자동 실행 끔)
* 운영:

  * `spring.jpa.hibernate.ddl-auto=validate`
  * (권장) Flyway로 마이그레이션 관리

## How to Run (Local)

### IntelliJ

1. 프로젝트 열기 → Gradle 자동 동기화
2. **Gradle 설정**

   * Build and run using: **Gradle**
   * Run tests using: **Gradle**
   * Distribution: **Wrapper**
   * Gradle JVM: **JDK 17**
3. `GlobooApplication` 실행

## Git Workflow

* 기본 브랜치: `main`
* 작업 브랜치 규칙: `feature/<기능>-<이름>`

  * 예) `feature/login-hyesong`, `feature/matching-donghan`

시작 예시:

```bash
git checkout -b feature/login-hyesong
# 작업
git add .
git commit -m "[ADD] 로그인 API 추가"
./gradlew clean build
git push -u origin feature/login-hyesong
```

PR 규칙:

* main에 직접 push 금지 → PR로만 병합     ( branck를 파서 작업하셔야 합니다 )
* 또한, push 하실때 민감한 파일들 ex) yml 이런 친구들을 꼭 gitignore 에 추가해주세용~
* PR 제목 예시:

  * `feat(auth): implement login API`
  * `fix(matching): null check on matcher`
* PR 본문에 변경 요약 / 테스트 방법 / 관련 이슈 포함
* CI에서 `./gradlew clean build` 통과 필수

원격 main과 충돌 시:

```bash
git pull origin main --allow-unrelated-histories
# 충돌 해결 후
git add .
git commit -m "merge: integrate remote main into local main"
git push origin main
```

## Commit Convention

* `[INIT]` 초기 세팅
* `[HOTFIX]` 배포 중 긴급 수정
* `[ADD]` 기능 추가
* `[FIX]` 버그 수정
* `[REFACTOR]` 리팩토링

예)
`[ADD] 회원가입 API 추가`
`[FIX] 로그인 토큰 만료 오류 수정`

## DTO Naming

> 엔티티명 + 행위(CRUD/Get) + 형태(Req/Res) + Dto

* 회원가입: `UserCreateReqDto`, `UserCreateResDto`
* 달력 조회: `CalendarGetReqDto`, `CalendarGetResDto`
* 비CRUD(로그인): `AuthLoginReqDto`, `AuthLoginResDto`

