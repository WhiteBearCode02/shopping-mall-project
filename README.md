🛒 E-Commerce Core Engine Project
Spring Boot 기반의 쇼핑몰 핵심 도메인 비즈니스 로직과 데이터 정합성 및 조회 성능 최적화를 고려하여 설계한 백엔드 코어 엔진 프로젝트입니다. 단순 기능 구현을 넘어 대규모 트래픽과 동시성 이슈 상황을 가정하고 아키텍처를 고도화하는 데 집중했습니다.

🚀 Key Features
도메인 주도 패키지 아키텍처 (Domain-Driven Structure): 유지보수성과 확장성을 극대화하기 위해 레이어 기반이 아닌 비즈니스 도메인(User, Product, Order) 단위로 패키지를 격리했습니다.

비관적 락(Pessimistic Lock) 기반 동시성 제어: 선착순 구매 및 한정판 상품 주문 시 발생할 수 있는 재고 불일치 문제를 해결하기 위해 DB 수준의 SELECT ... FOR UPDATE 메커니즘을 도입했습니다.

조회 성능 최적화 (Composite Index): 리뷰 데이터 대용량화 시 발생할 수 있는 Full Table Scan을 방지하기 위해 복합 인덱스를 설계하여 정렬 및 필터링 속도를 개선했습니다.

글로벌 예외 처리 (AOP 기반): 전역 예외 핸들러(@RestControllerAdvice)를 구축하여 비즈니스 예외와 시스템 예외를 중앙 집중식으로 제어하고 일관된 에러 응답 객체를 반환합니다.

배포 환경 격리 (Multi-Profile): 개발 환경(local)과 가상 운영 환경(prod)의 설정을 완벽히 분리하고, 실서버의 안정성을 위해 JPA ddl-auto 규칙을 안전하게 제어했습니다.

📁 Tech Stack & 패키지 구조
Backend: Java 17, Spring Boot 3.x, Spring Data JPA

Database: MySQL

Build Tool: Gradle

### 📁 패키지 구조
```text
src/main/java/com/example/shoppingmall
├── domain
│   ├── user              # 회원 도메인
│   │   ├── controller
│   │   ├── entity
│   │   ├── repository
│   │   └── service
│   ├── product           # 상품 및 리뷰 도메인
│   │   ├── controller
│   │   ├── entity
│   │   ├── repository
│   │   └── service
│   └── order             # 주문 도메인
│       ├── controller
│       ├── entity
│       ├── repository
│       └── service
└── global
    ├── config           # 인프라 및 시스템 설정
    └── exception        # 전역 예외 처리
```

### 📊 Data Architecture (ERD)
주문(Orders)과 상품(Products) 간의 다대다(N:M) 관계를 해소하고 주문 당시의 스냅샷 데이터를 보존하기 위해 중간 매핑 엔티티인 주문 상품(Order_Items) 테이블을 설계했습니다.

Users (1) : (N) Orders

### ▶️ 실행 방법
```bash
cd demo
$env:JAVA_HOME='C:\Program Files\Java\jdk-17.0.2'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
./gradlew.bat bootRun
```

브라우저에서 다음 주소로 접속할 수 있습니다.
- http://localhost:8081
- http://localhost:8081/api/products

Orders (1) : (N) Order_Items

Products (1) : (N) Order_Items

Products (1) : (N) Reviews

🛠️ Troubleshooting & Architecture Deep Dive
1. 빈번한 충돌 상황에서의 재고 동기화 (동시성 제어)
문제 정의: 인기 상품 주문 시 여러 사용자가 동시에 재고 차감을 요청할 경우, 데이터 갱신 분실(Lost Update) 현상이 발생하여 실제 재고보다 더 많은 수량이 주문되는 정합성 오류 리스크 발견.

해결 알고리즘: 애플리케이션 레벨의 낙관적 락은 충돌 발생 시 무수한 롤백 및 재시도 오버헤드를 유발하므로, 트래픽 밀집도가 높은 주문 도메인의 특성을 고려하여 DB 레벨에서 Lock을 획득하는 비관적 쓰기 락(Pessimistic Write Lock)을 채택.

결과: 안정적인 순차 제어를 통해 대용량 동시 요청 상황에서도 마이너스 재고가 발생하지 않는 데이터 정합성 달성.

2. Gradle 빌드 타겟 런타임 버전 불일치
문제 정의: Gradle 9.x 엔진 빌드 과정에서 로컬 환경의 가리키는 자바 버전 서식과 프레임워크 요구 스펙이 맞지 않아 Gradle requires JVM 17 or later 빌드 크래시 발생.

해결 방법: 에디터의 작업 영역(Workspace) 컨텍스트 설정을 제어하는 .vscode/settings.json 내부에 java.jdt.ls.java.home 및 runtime 타겟 경로를 JDK 17로 명시적으로 정정하여 빌드 정상화 완료.

3. 실서버 데이터 보호를 위한 배포 프로필 분리
문제 정의: 개발 편의성을 위해 사용하던 ddl-auto=create 또는 update 설정이 실서버 환경에 그대로 적용될 경우, 서비스 재구동 시 테이블 드롭 및 데이터 유실이라는 치명적인 인프라 장애 유발 가능성 존재.

해결 방법: application-prod.properties 설정을 별도로 분리하여 격리하고, 데이터 구조적 정합성만 검증하는 update 또는 validate 체제로 옵션을 제어하여 언제든 즉시 배포 가능한 안전한 인프라 환경 구축.

📝 Git Commit & Branch Strategy
본 프로젝트는 실무 형상 관리 흐름을 반영하여 모든 기능 개발을 main 브랜치에 직접 커밋하지 않고, 기능 단위의 Feature Branch 전략(feat/기능명, chore/설정명)을 수립하여 Pull Request 기반의 병합을 진행함으로써 소스코드의 변경 이력을 투명하게 관리했습니다.